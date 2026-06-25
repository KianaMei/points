package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDeleteReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDisableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubEnableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubStrongConfirmReqBO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_ENABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.PHYSICAL_DELETE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_CODE_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_DELETE_HAS_REFERENCES;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NAME_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_STRONG_CONFIRM_INVALID;

/**
 * 俱乐部主数据服务实现
 */
@Service
public class ClubPointClubServiceImpl implements ClubPointClubService {

    private static final String BIZ_TYPE_CLUB = "CLUB";
    private static final String STRONG_CONFIRM_PREFIX = "确认删除俱乐部：";

    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubAuditService clubAuditService;

    @Override
    public Long createClub(ClubPointClubSaveReqBO reqBO) {
        validateClubUnique(reqBO.getCode(), reqBO.getName(), null);
        ClubPointClubDO club = buildClub(reqBO)
                .setStatus(ClubPointClubStatusEnum.ENABLED.getStatus())
                .setDisabledTime(null)
                .setDisabledReason(null);
        clubMapper.insert(club);
        return club.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClub(ClubPointClubSaveReqBO reqBO) {
        ClubPointClubDO club = validateClubExists(reqBO.getId());
        validateClubUnique(reqBO.getCode(), reqBO.getName(), club.getId());
        String beforeJson = snapshot(club);
        updateClubFields(club, reqBO);
        clubMapper.updateById(club);
        createAudit(CLUB_UPDATE, club.getId(), reqBO, beforeJson, snapshot(club), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableClub(ClubPointClubDisableReqBO reqBO) {
        ClubPointClubDO club = validateClubExists(reqBO.getId());
        String beforeJson = snapshot(club);
        LocalDateTime operationTime = LocalDateTime.now();
        club.setStatus(ClubPointClubStatusEnum.DISABLED.getStatus())
                .setDisabledTime(operationTime)
                .setDisabledReason(reqBO.getReason());
        clubMapper.updateById(club);
        createAudit(CLUB_DISABLE, club.getId(), reqBO, beforeJson, snapshot(club), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableClub(ClubPointClubEnableReqBO reqBO) {
        ClubPointClubDO club = validateClubExists(reqBO.getId());
        String beforeJson = snapshot(club);
        club.setStatus(ClubPointClubStatusEnum.ENABLED.getStatus())
                .setDisabledTime(null)
                .setDisabledReason(null);
        clubMapper.update(null, new LambdaUpdateWrapper<ClubPointClubDO>()
                .eq(ClubPointClubDO::getId, club.getId())
                .set(ClubPointClubDO::getStatus, ClubPointClubStatusEnum.ENABLED.getStatus())
                .set(ClubPointClubDO::getDisabledTime, null)
                .set(ClubPointClubDO::getDisabledReason, null));
        createAudit(CLUB_ENABLE, club.getId(), reqBO, beforeJson, snapshot(club), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClubPhysically(ClubPointClubDeleteReqBO reqBO) {
        ClubPointClubDO club = validateClubExists(reqBO.getId());
        validateStrongConfirm(club, reqBO.getStrongConfirm());
        if (clubMapper.selectReferenceCountByClubId(club.getId()) > 0) {
            throw exception(CLUB_DELETE_HAS_REFERENCES);
        }
        String snapshotJson = snapshot(club);
        createAudit(PHYSICAL_DELETE, club.getId(), reqBO, snapshotJson, null, snapshotJson);
        int deleted = clubMapper.deletePhysicallyById(club.getId());
        if (deleted != 1) {
            throw exception(CLUB_NOT_FOUND);
        }
    }

    private ClubPointClubDO validateClubExists(Long clubId) {
        ClubPointClubDO club = clubMapper.selectById(clubId);
        if (club == null) {
            throw exception(CLUB_NOT_FOUND);
        }
        return club;
    }

    private void validateClubUnique(String code, String name, Long excludeId) {
        ClubPointClubDO sameCode = StringUtils.hasText(code) ? clubMapper.selectByCode(code) : null;
        if (sameCode != null && (excludeId == null || !excludeId.equals(sameCode.getId()))) {
            throw exception(CLUB_CODE_DUPLICATED);
        }
        ClubPointClubDO sameName = StringUtils.hasText(name) ? clubMapper.selectByName(name) : null;
        if (sameName != null && (excludeId == null || !excludeId.equals(sameName.getId()))) {
            throw exception(CLUB_NAME_DUPLICATED);
        }
    }

    private static void validateStrongConfirm(ClubPointClubDO club, ClubStrongConfirmReqBO strongConfirm) {
        String expectedText = STRONG_CONFIRM_PREFIX + club.getName();
        if (strongConfirm == null || strongConfirm.getConfirmedAt() == null
                || !expectedText.equals(strongConfirm.getConfirmText())) {
            throw exception(CLUB_STRONG_CONFIRM_INVALID);
        }
    }

    private static ClubPointClubDO buildClub(ClubPointClubSaveReqBO reqBO) {
        return updateClubFields(new ClubPointClubDO(), reqBO);
    }

    private static ClubPointClubDO updateClubFields(ClubPointClubDO club, ClubPointClubSaveReqBO reqBO) {
        return club.setCode(reqBO.getCode())
                .setName(reqBO.getName())
                .setDescription(reqBO.getDescription())
                .setContactText(reqBO.getContactText())
                .setCoverFileId(reqBO.getCoverFileId())
                .setSort(reqBO.getSort() == null ? 0 : reqBO.getSort())
                .setRemark(reqBO.getRemark());
    }

    private Long createAudit(String actionType, Long clubId, ClubPointClubOperationReq reqBO,
                             String beforeJson, String afterJson, String targetSnapshotJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(BIZ_TYPE_CLUB)
                .setBizId(clubId)
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(targetSnapshotJson)
                .setSuccess(true));
    }

    private static String snapshot(ClubPointClubDO club) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", club.getId());
        snapshot.put("code", club.getCode());
        snapshot.put("name", club.getName());
        snapshot.put("status", club.getStatus());
        snapshot.put("description", club.getDescription());
        snapshot.put("contactText", club.getContactText());
        snapshot.put("coverFileId", club.getCoverFileId());
        snapshot.put("disabledReason", club.getDisabledReason());
        return JsonUtils.toJsonString(snapshot);
    }

}

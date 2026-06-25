package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderAssignReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderRemoveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_LEADER_ASSIGN;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CLUB_LEADER_REMOVE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEADER_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEADER_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_NOT_FOUND;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

/**
 * 俱乐部负责人服务实现
 */
@Service
public class ClubPointLeaderServiceImpl implements ClubPointLeaderService {

    private static final String BIZ_TYPE_LEADER = "CLUB_LEADER";

    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubScopeService clubScopeService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assignLeader(ClubPointLeaderAssignReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointClubDO club = validateClubExists(reqBO.getClubId());
        AdminUserRespDTO user = validateLeaderUser(reqBO.getUserId());
        if (leaderMapper.selectByUserIdAndClubIdAndStatus(reqBO.getUserId(), reqBO.getClubId(),
                ClubPointLeaderStatusEnum.ACTIVE.getStatus()) != null) {
            throw exception(CLUB_LEADER_ALREADY_EXISTS);
        }
        ClubLeaderDO leader = new ClubLeaderDO()
                .setClubId(club.getId())
                .setUserId(reqBO.getUserId())
                .setStatus(ClubPointLeaderStatusEnum.ACTIVE.getStatus())
                .setAssignedTime(LocalDateTime.now())
                .setAssignedBy(reqBO.getOperatorUserId())
                .setReason(reqBO.getReason())
                .setClubNameSnapshot(club.getName())
                .setUserNameSnapshot(user.getNickname())
                .setActiveUniqueKey(buildActiveUniqueKey(club.getId(), reqBO.getUserId()));
        leaderMapper.insert(leader);
        createAudit(CLUB_LEADER_ASSIGN, leader, reqBO, null, snapshot(leader));
        return leader.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeLeader(ClubPointLeaderRemoveReqBO reqBO) {
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubLeaderDO leader = leaderMapper.selectByUserIdAndClubIdAndStatus(reqBO.getUserId(), reqBO.getClubId(),
                ClubPointLeaderStatusEnum.ACTIVE.getStatus());
        if (leader == null) {
            throw exception(CLUB_LEADER_NOT_EXISTS);
        }
        String beforeJson = snapshot(leader);
        LocalDateTime removedTime = LocalDateTime.now();
        int updated = leaderMapper.update(null, new LambdaUpdateWrapper<ClubLeaderDO>()
                .eq(ClubLeaderDO::getId, leader.getId())
                .eq(ClubLeaderDO::getStatus, ClubPointLeaderStatusEnum.ACTIVE.getStatus())
                .set(ClubLeaderDO::getStatus, ClubPointLeaderStatusEnum.REMOVED.getStatus())
                .set(ClubLeaderDO::getRemovedTime, removedTime)
                .set(ClubLeaderDO::getRemovedBy, reqBO.getOperatorUserId())
                .set(ClubLeaderDO::getReason, reqBO.getReason())
                .set(ClubLeaderDO::getActiveUniqueKey, null));
        if (updated != 1) {
            throw exception(CLUB_LEADER_NOT_EXISTS);
        }
        leader.setStatus(ClubPointLeaderStatusEnum.REMOVED.getStatus())
                .setRemovedTime(removedTime)
                .setRemovedBy(reqBO.getOperatorUserId())
                .setReason(reqBO.getReason())
                .setActiveUniqueKey(null);
        createAudit(CLUB_LEADER_REMOVE, leader, reqBO, beforeJson, snapshot(leader));
    }

    private ClubPointClubDO validateClubExists(Long clubId) {
        ClubPointClubDO club = clubMapper.selectById(clubId);
        if (club == null) {
            throw exception(CLUB_NOT_FOUND);
        }
        return club;
    }

    private AdminUserRespDTO validateLeaderUser(Long userId) {
        adminUserApi.validateUser(userId);
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

    private Long createAudit(String actionType, ClubLeaderDO leader, ClubPointClubOperationReq reqBO,
                             String beforeJson, String afterJson) {
        return clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(actionType)
                .setBizType(BIZ_TYPE_LEADER)
                .setBizId(leader.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(snapshot(leader))
                .setSuccess(true));
    }

    private static String snapshot(ClubLeaderDO leader) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", leader.getId());
        snapshot.put("clubId", leader.getClubId());
        snapshot.put("userId", leader.getUserId());
        snapshot.put("status", leader.getStatus());
        snapshot.put("assignedTime", leader.getAssignedTime());
        snapshot.put("removedTime", leader.getRemovedTime());
        snapshot.put("assignedBy", leader.getAssignedBy());
        snapshot.put("removedBy", leader.getRemovedBy());
        snapshot.put("reason", leader.getReason());
        snapshot.put("clubNameSnapshot", leader.getClubNameSnapshot());
        snapshot.put("userNameSnapshot", leader.getUserNameSnapshot());
        snapshot.put("activeUniqueKey", leader.getActiveUniqueKey());
        return JsonUtils.toJsonString(snapshot);
    }

    private static String buildActiveUniqueKey(Long clubId, Long userId) {
        return clubId + ":" + userId;
    }

}

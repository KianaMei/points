package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointIncentiveRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointIncentiveRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveSuggestReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.INCENTIVE_CANCEL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.INCENTIVE_CONFIRM;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_INCENTIVE_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_INCENTIVE_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_INCENTIVE_STATUS_INVALID;

/**
 * 运营激励服务实现
 */
@Service
public class ClubPointIncentiveServiceImpl implements ClubPointIncentiveService {

    private static final String BIZ_TYPE_INCENTIVE_RECORD = "INCENTIVE_RECORD";

    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubPointIncentiveRecordMapper incentiveRecordMapper;
    @Resource
    private ClubAuditService clubAuditService;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateRankingIncentives(ClubPointIncentiveSuggestReqBO reqBO) {
        validateSuggestReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        int created = 0;
        List<ClubPointAnnualRankingRecordDO> rankings = rankingRecordMapper.selectListByYear(reqBO.getYear());
        for (ClubPointAnnualRankingRecordDO ranking : rankings) {
            if (ranking.getIncentiveAmountCent() == null || ranking.getIncentiveAmountCent() <= 0) {
                continue;
            }
            if (incentiveRecordMapper.selectBySourceTypeAndSourceId(
                    ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType(), ranking.getId()) != null) {
                continue;
            }
            incentiveRecordMapper.insert(buildRankingIncentive(ranking));
            created++;
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmIncentive(ClubPointIncentiveOperationReqBO reqBO) {
        updateIncentiveStatus(reqBO, ClubPointIncentiveStatusEnum.CONFIRMED, INCENTIVE_CONFIRM);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelIncentive(ClubPointIncentiveOperationReqBO reqBO) {
        updateIncentiveStatus(reqBO, ClubPointIncentiveStatusEnum.CANCELED, INCENTIVE_CANCEL);
    }

    private void updateIncentiveStatus(ClubPointIncentiveOperationReqBO reqBO,
                                       ClubPointIncentiveStatusEnum targetStatus,
                                       String auditAction) {
        validateOperationReq(reqBO);
        clubScopeService.validateGlobal(Boolean.TRUE.equals(reqBO.getOperatorGlobalScope()));
        ClubPointIncentiveRecordDO incentive = incentiveRecordMapper.selectByIdForUpdate(reqBO.getId());
        if (incentive == null) {
            throw exception(CLUB_INCENTIVE_NOT_EXISTS);
        }
        if (!ClubPointIncentiveStatusEnum.SUGGESTED.getStatus().equals(incentive.getStatus())) {
            throw exception(CLUB_INCENTIVE_STATUS_INVALID);
        }
        String beforeJson = snapshot(incentive);
        LocalDateTime operationTime = reqBO.getOperationTime() == null ? LocalDateTime.now() : reqBO.getOperationTime();
        applyStatus(incentive, reqBO, targetStatus, operationTime);
        String afterJson = snapshot(incentive);
        createOperationAudit(reqBO, incentive, auditAction, operationTime, beforeJson, afterJson);
        incentiveRecordMapper.updateById(incentive);
    }

    private void createOperationAudit(ClubPointIncentiveOperationReqBO reqBO,
                                      ClubPointIncentiveRecordDO incentive,
                                      String auditAction,
                                      LocalDateTime operationTime,
                                      String beforeJson,
                                      String afterJson) {
        clubAuditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(auditAction)
                .setBizType(BIZ_TYPE_INCENTIVE_RECORD)
                .setBizId(incentive.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(operationTime)
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson(beforeJson)
                .setAfterJson(afterJson)
                .setTargetSnapshotJson(afterJson)
                .setSuccess(true));
    }

    private static void applyStatus(ClubPointIncentiveRecordDO incentive,
                                    ClubPointIncentiveOperationReqBO reqBO,
                                    ClubPointIncentiveStatusEnum targetStatus,
                                    LocalDateTime operationTime) {
        incentive.setStatus(targetStatus.getStatus())
                .setRemark(reqBO.getReason());
        if (targetStatus == ClubPointIncentiveStatusEnum.CONFIRMED) {
            incentive.setConfirmedBy(reqBO.getOperatorUserId())
                    .setConfirmedTime(operationTime);
        }
    }

    private static ClubPointIncentiveRecordDO buildRankingIncentive(ClubPointAnnualRankingRecordDO ranking) {
        return new ClubPointIncentiveRecordDO()
                .setYear(ranking.getYear())
                .setType(ClubPointIncentiveTypeEnum.RANKING.getType())
                .setClubId(ranking.getClubId())
                .setClubNameSnapshot(ranking.getClubNameSnapshot())
                .setTitle(ranking.getYear() + "年度俱乐部排名第" + ranking.getRankNo() + "名激励")
                .setAmountCent(ranking.getIncentiveAmountCent())
                .setStatus(ClubPointIncentiveStatusEnum.SUGGESTED.getStatus())
                .setSourceType(ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType())
                .setSourceId(ranking.getId())
                .setBudgetRecordId(null)
                .setRemark("由年度排名生成");
    }

    private static void validateSuggestReq(ClubPointIncentiveSuggestReqBO reqBO) {
        if (reqBO == null || reqBO.getYear() == null) {
            throw exception(CLUB_INCENTIVE_INVALID);
        }
    }

    private static void validateOperationReq(ClubPointIncentiveOperationReqBO reqBO) {
        if (reqBO == null || reqBO.getId() == null || reqBO.getOperatorUserId() == null) {
            throw exception(CLUB_INCENTIVE_INVALID);
        }
    }

    private static String snapshot(ClubPointIncentiveRecordDO incentive) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", incentive.getId());
        snapshot.put("year", incentive.getYear());
        snapshot.put("type", incentive.getType());
        snapshot.put("clubId", incentive.getClubId());
        snapshot.put("clubNameSnapshot", incentive.getClubNameSnapshot());
        snapshot.put("userId", incentive.getUserId());
        snapshot.put("userNameSnapshot", incentive.getUserNameSnapshot());
        snapshot.put("title", incentive.getTitle());
        snapshot.put("amountCent", incentive.getAmountCent());
        snapshot.put("status", incentive.getStatus());
        snapshot.put("sourceType", incentive.getSourceType());
        snapshot.put("sourceId", incentive.getSourceId());
        snapshot.put("budgetRecordId", incentive.getBudgetRecordId());
        snapshot.put("confirmedBy", incentive.getConfirmedBy());
        snapshot.put("confirmedTime", incentive.getConfirmedTime());
        snapshot.put("remark", incentive.getRemark());
        return JsonUtils.toJsonString(snapshot);
    }

}

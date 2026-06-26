package cn.iocoder.yudao.module.clubpoints.service.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.settlement.ClubPointActivitySettlementRunMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementTriggerSourceEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementManualRunReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementPendingActivityPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementRunPageReqBO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 管理员活动结算服务
 */
@Service
public class ClubPointActivitySettlementAdminServiceImpl implements ClubPointActivitySettlementAdminService {

    private static final String AUDIT_BIZ_TYPE_ACTIVITY_SETTLEMENT = "ACTIVITY_SETTLEMENT";

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivitySettlementRunMapper settlementRunMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubAuditService auditService;
    @Resource
    private ClubPointActivitySettlementJobService jobService;

    @Override
    public PageResult<ClubPointActivityDO> getPendingActivityPage(ClubPointSettlementPendingActivityPageReqBO reqBO) {
        return activityMapper.selectPage(reqBO, null, reqBO.getClubId(), reqBO.getKeyword(),
                ClubPointActivityStatusEnum.ENDED.getStatus(), null, null, null);
    }

    @Override
    public String runSettlement(ClubPointSettlementManualRunReqBO reqBO) throws Exception {
        auditService.createAuditLog(buildAuditReq(reqBO));
        return jobService.run(new ClubPointActivitySettlementJobReqBO()
                .setRunKey(buildManualRunKey(reqBO.getActivityId()))
                .setActivityId(reqBO.getActivityId())
                .setTriggerSource(ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource())
                .setRetryCount(0)
                .setHandlerUserId(reqBO.getOperatorUserId())
                .setPlannedTime(LocalDateTime.now())
                .setSettlementTime(LocalDateTime.now()));
    }

    @Override
    public PageResult<ClubPointActivitySettlementRunDO> getRunPage(ClubPointSettlementRunPageReqBO reqBO) {
        return settlementRunMapper.selectPage(reqBO, reqBO.getActivityId(), reqBO.getStatus());
    }

    @Override
    public ClubPointSettlementDetailBO getDetail(Long id) {
        ClubPointActivitySettlementRunDO run = settlementRunMapper.selectById(id);
        if (run == null) {
            throw new IllegalArgumentException("Settlement run not found: " + id);
        }
        return new ClubPointSettlementDetailBO()
                .setRun(run)
                .setTransactions(transactionMapper.selectListByActivityIdAndSourceType(run.getActivityId(),
                        ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType()));
    }

    private static ClubAuditCreateReqBO buildAuditReq(ClubPointSettlementManualRunReqBO reqBO) {
        return new ClubAuditCreateReqBO()
                .setActionType(ClubAuditActionTypeConstants.ACTIVITY_SETTLEMENT_MANUAL)
                .setBizType(AUDIT_BIZ_TYPE_ACTIVITY_SETTLEMENT)
                .setBizId(reqBO.getActivityId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setBeforeJson("{\"activityId\":" + reqBO.getActivityId() + "}")
                .setTargetSnapshotJson("{\"force\":" + Boolean.TRUE.equals(reqBO.getForce()) + "}")
                .setSuccess(true);
    }

    private static String buildManualRunKey(Long activityId) {
        return "ADMIN_MANUAL:" + activityId + ":" + UUID.randomUUID();
    }

}

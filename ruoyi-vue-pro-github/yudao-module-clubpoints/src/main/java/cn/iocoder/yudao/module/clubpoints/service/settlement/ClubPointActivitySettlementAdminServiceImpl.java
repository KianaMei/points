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
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理员活动积分发放服务
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
        return activityMapper.selectSettlementPendingPage(reqBO, reqBO.getClubId(), reqBO.getClubName(),
                resolveActivityTitle(reqBO.getActivityTitle(), reqBO.getKeyword()),
                reqBO.getStartTime(), reqBO.getEndTime());
    }

    @Override
    public String runSettlement(ClubPointSettlementManualRunReqBO reqBO) throws Exception {
        auditService.createAuditLog(buildAuditReq(reqBO));
        closePublishedActivityIfForced(reqBO);
        return jobService.run(new ClubPointActivitySettlementJobReqBO()
                .setRunKey(buildManualRunKey(reqBO.getActivityId()))
                .setActivityId(reqBO.getActivityId())
                .setTriggerSource(ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource())
                .setRetryCount(0)
                .setHandlerUserId(reqBO.getOperatorUserId())
                .setPlannedTime(LocalDateTime.now())
                .setSettlementTime(LocalDateTime.now()));
    }

    private void closePublishedActivityIfForced(ClubPointSettlementManualRunReqBO reqBO) {
        if (!Boolean.TRUE.equals(reqBO.getForce())) {
            return;
        }
        ClubPointActivityDO activity = activityMapper.selectById(reqBO.getActivityId());
        if (activity == null || !ClubPointActivityStatusEnum.PUBLISHED.getStatus().equals(activity.getStatus())) {
            return;
        }
        activity.setStatus(ClubPointActivityStatusEnum.ENDED.getStatus());
        activityMapper.updateById(activity);
    }

    @Override
    public PageResult<ClubPointActivitySettlementRunDO> getRunPage(ClubPointSettlementRunPageReqBO reqBO) {
        Collection<Long> activityIds = resolveActivityIds(reqBO);
        PageResult<ClubPointActivitySettlementRunDO> pageResult = settlementRunMapper.selectPage(reqBO,
                reqBO.getActivityId(), activityIds, reqBO.getStatus(), null, null);
        enrichActivityInfo(pageResult.getList());
        return pageResult;
    }

    @Override
    public ClubPointSettlementDetailBO getDetail(Long id) {
        ClubPointActivitySettlementRunDO run = settlementRunMapper.selectById(id);
        if (run == null) {
            throw new IllegalArgumentException("Settlement run not found: " + id);
        }
        enrichActivityInfo(Collections.singletonList(run));
        return new ClubPointSettlementDetailBO()
                .setRun(run)
                .setTransactions(transactionMapper.selectListByActivityIdAndSourceType(run.getActivityId(),
                        ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType()));
    }

    private Collection<Long> resolveActivityIds(ClubPointSettlementRunPageReqBO reqBO) {
        if (!hasActivityBusinessFilter(reqBO)) {
            return null;
        }
        return activityMapper.selectListBySettlementBusinessFilter(reqBO.getClubName(), reqBO.getActivityTitle(),
                        reqBO.getStartTime(), reqBO.getEndTime())
                .stream()
                .map(ClubPointActivityDO::getId)
                .collect(Collectors.toSet());
    }

    private void enrichActivityInfo(List<ClubPointActivitySettlementRunDO> runs) {
        if (runs == null || runs.isEmpty()) {
            return;
        }
        List<Long> activityIds = runs.stream()
                .map(ClubPointActivitySettlementRunDO::getActivityId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (activityIds.isEmpty()) {
            return;
        }
        Map<Long, ClubPointActivityDO> activityMap = activityMapper.selectBatchIds(activityIds).stream()
                .collect(Collectors.toMap(ClubPointActivityDO::getId, Function.identity(), (left, right) -> left));
        for (ClubPointActivitySettlementRunDO run : runs) {
            ClubPointActivityDO activity = activityMap.get(run.getActivityId());
            if (activity == null) {
                continue;
            }
            run.setClubId(activity.getClubId())
                    .setClubName(activity.getClubNameSnapshot())
                    .setActivityTitle(activity.getTitle())
                    .setActivityStartTime(activity.getStartTime())
                    .setActivityEndTime(activity.getEndTime());
        }
    }

    private static boolean hasActivityBusinessFilter(ClubPointSettlementRunPageReqBO reqBO) {
        return StringUtils.hasText(reqBO.getClubName()) || StringUtils.hasText(reqBO.getActivityTitle())
                || reqBO.getStartTime() != null || reqBO.getEndTime() != null;
    }

    private static String resolveActivityTitle(String activityTitle, String keyword) {
        return StringUtils.hasText(activityTitle) ? activityTitle : keyword;
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

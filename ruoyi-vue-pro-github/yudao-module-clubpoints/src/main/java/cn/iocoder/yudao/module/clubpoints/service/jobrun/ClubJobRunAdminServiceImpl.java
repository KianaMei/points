package cn.iocoder.yudao.module.clubpoints.service.jobrun;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_JOB_RUN_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_JOB_RUN_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_JOB_RUN_TASK_TYPE_INVALID;

@Service
public class ClubJobRunAdminServiceImpl implements ClubJobRunAdminService {

    private static final String TASK_TYPE_ACTIVITY_SETTLEMENT = "ACTIVITY_SETTLEMENT";
    private static final String TASK_TYPE_ANNUAL_CLEARING = "ANNUAL_CLEARING";
    private static final String BIZ_TYPE_JOB_RUN = "JOB_RUN";
    private static final int TRIGGER_RETRY = 3;

    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubAuditService auditService;
    @Resource
    private ClubJobRunRetryDispatcher retryDispatcher;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubJobRunDO> getJobRunPage(ClubJobRunPageReqBO reqBO) {
        return jobRunMapper.selectPage(reqBO);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubJobRunDO getJobRunDetail(Long id) {
        ClubJobRunDO jobRun = jobRunMapper.selectById(id);
        if (jobRun == null) {
            throw exception(CLUB_JOB_RUN_NOT_EXISTS);
        }
        return jobRun;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleJobRun(ClubJobRunHandleReqBO reqBO) throws Exception {
        ClubJobRunDO failedRun = getJobRunDetail(reqBO.getId());
        if (!ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus().equals(failedRun.getStatus())
                && !ClubPointSettlementRunStatusEnum.FINAL_FAILED.getStatus().equals(failedRun.getStatus())) {
            throw exception(CLUB_JOB_RUN_STATUS_INVALID);
        }
        int nextRetryCount = failedRun.getRetryCount() == null ? 1 : failedRun.getRetryCount() + 1;
        createRetryAudit(reqBO, failedRun, nextRetryCount);
        if (TASK_TYPE_ACTIVITY_SETTLEMENT.equals(failedRun.getTaskType())) {
            return retryDispatcher.retryActivitySettlement(buildActivityRetryReq(reqBO, failedRun, nextRetryCount));
        }
        if (TASK_TYPE_ANNUAL_CLEARING.equals(failedRun.getTaskType())) {
            return retryDispatcher.retryAnnualClearing(buildAnnualRetryReq(reqBO, failedRun, nextRetryCount));
        }
        throw exception(CLUB_JOB_RUN_TASK_TYPE_INVALID);
    }

    private void createRetryAudit(ClubJobRunHandleReqBO reqBO, ClubJobRunDO failedRun, int nextRetryCount) {
        auditService.createAuditLog(new ClubAuditCreateReqBO()
                .setActionType(ClubAuditActionTypeConstants.JOB_RUN_RETRY)
                .setBizType(BIZ_TYPE_JOB_RUN)
                .setBizId(failedRun.getId())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(reqBO.getReason())
                .setTargetSnapshotJson(buildRetrySnapshot(failedRun, nextRetryCount))
                .setSuccess(true));
    }

    private static ClubPointActivitySettlementJobReqBO buildActivityRetryReq(ClubJobRunHandleReqBO reqBO,
                                                                            ClubJobRunDO failedRun,
                                                                            int nextRetryCount) {
        return new ClubPointActivitySettlementJobReqBO()
                .setRunKey(failedRun.getRunKey())
                .setActivityId(failedRun.getBizId())
                .setTriggerSource(TRIGGER_RETRY)
                .setRetryCount(nextRetryCount)
                .setHandlerUserId(reqBO.getOperatorUserId())
                .setPlannedTime(LocalDateTime.now())
                .setSettlementTime(LocalDateTime.now());
    }

    private static ClubPointAnnualClearingJobReqBO buildAnnualRetryReq(ClubJobRunHandleReqBO reqBO,
                                                                       ClubJobRunDO failedRun,
                                                                       int nextRetryCount) {
        return new ClubPointAnnualClearingJobReqBO()
                .setYear(failedRun.getBizId().intValue())
                .setRunKey(failedRun.getRunKey())
                .setUserIds(extractFailedUserIds(failedRun.getResultJson()))
                .setTriggerSource(TRIGGER_RETRY)
                .setRetryCount(nextRetryCount)
                .setHandlerUserId(reqBO.getOperatorUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setPlannedTime(LocalDateTime.now())
                .setClearTime(LocalDateTime.now())
                .setManualHandleReason(reqBO.getReason())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent());
    }

    private static List<Long> extractFailedUserIds(String resultJson) {
        Map<String, Object> result = JsonUtils.parseObjectQuietly(resultJson,
                new TypeReference<Map<String, Object>>() {});
        if (result == null || result.get("failedUserIds") == null) {
            return new ArrayList<>();
        }
        return JsonUtils.convertList(result.get("failedUserIds"), Long.class);
    }

    private static String buildRetrySnapshot(ClubJobRunDO failedRun, int nextRetryCount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("jobRunId", failedRun.getId());
        snapshot.put("taskType", failedRun.getTaskType());
        snapshot.put("bizType", failedRun.getBizType());
        snapshot.put("bizId", failedRun.getBizId());
        snapshot.put("runKey", failedRun.getRunKey());
        snapshot.put("status", failedRun.getStatus());
        snapshot.put("retryCount", nextRetryCount);
        snapshot.put("sourceRetryCount", failedRun.getRetryCount());
        return JsonUtils.toJsonString(snapshot);
    }

}

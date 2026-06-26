package cn.iocoder.yudao.module.clubpoints.service.settlement;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementTriggerSourceEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementRunReqBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 活动结算 Job 编排服务
 */
@Service
public class ClubPointActivitySettlementJobService {

    private static final String TASK_TYPE_ACTIVITY_SETTLEMENT = "ACTIVITY_SETTLEMENT";
    private static final String BIZ_TYPE_ACTIVITY = "ACTIVITY";
    private static final int DEFAULT_RETRY_COUNT = 0;
    private static final int ERROR_MESSAGE_MAX_LENGTH = 2048;

    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubPointActivitySettlementService settlementService;

    public String run(ClubPointActivitySettlementJobReqBO reqBO) throws Exception {
        validateReq(reqBO);
        String idempotencyKey = buildJobIdempotencyKey(reqBO);
        ClubJobRunDO existing = jobRunMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return buildResult(existing.getId(), null);
        }

        LocalDateTime startTime = LocalDateTime.now();
        ClubJobRunDO jobRun = insertRunningJobRun(buildRunningJobRun(reqBO, idempotencyKey, startTime));
        try {
            Long settlementRunId = settlementService.settleActivity(new ClubPointActivitySettlementRunReqBO()
                    .setActivityId(reqBO.getActivityId())
                    .setRunKey(buildSettlementRunKey(reqBO))
                    .setTriggerSource(reqBO.getTriggerSource())
                    .setOperatorUserId(reqBO.getHandlerUserId())
                    .setSettlementTime(reqBO.getSettlementTime())
                    .setJobRunId(jobRun.getId()));
            updateSuccess(jobRun, settlementRunId);
            return buildResult(jobRun.getId(), settlementRunId);
        } catch (Exception ex) {
            updateRetryableFailure(jobRun, ex);
            throw ex;
        }
    }

    private ClubJobRunDO insertRunningJobRun(ClubJobRunDO jobRun) {
        try {
            jobRunMapper.insert(jobRun);
            return jobRun;
        } catch (DuplicateKeyException ex) {
            ClubJobRunDO duplicated = jobRunMapper.selectByIdempotencyKey(jobRun.getIdempotencyKey());
            if (duplicated != null) {
                return duplicated;
            }
            throw ex;
        }
    }

    private void updateSuccess(ClubJobRunDO jobRun, Long settlementRunId) {
        jobRun.setStatus(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus())
                .setEndTime(LocalDateTime.now())
                .setSuccessCount(1)
                .setFailedCount(0)
                .setResultJson("{\"activityIds\":[" + jobRun.getBizId()
                        + "],\"settlementRunId\":" + settlementRunId + "}");
        jobRunMapper.updateById(jobRun);
    }

    private void updateRetryableFailure(ClubJobRunDO jobRun, Exception ex) {
        jobRun.setStatus(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus())
                .setEndTime(LocalDateTime.now())
                .setSuccessCount(0)
                .setFailedCount(1)
                .setNextRetryTime(LocalDateTime.now().plusMinutes(5))
                .setErrorType(truncate(ex.getClass().getSimpleName(), 128))
                .setErrorMessage(truncate(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName(),
                        ERROR_MESSAGE_MAX_LENGTH));
        jobRunMapper.updateById(jobRun);
    }

    private static ClubJobRunDO buildRunningJobRun(ClubPointActivitySettlementJobReqBO reqBO,
                                                   String idempotencyKey, LocalDateTime startTime) {
        return new ClubJobRunDO()
                .setTaskType(TASK_TYPE_ACTIVITY_SETTLEMENT)
                .setBizType(BIZ_TYPE_ACTIVITY)
                .setBizId(reqBO.getActivityId())
                .setRunKey(reqBO.getRunKey())
                .setIdempotencyKey(idempotencyKey)
                .setStatus(ClubPointSettlementRunStatusEnum.RUNNING.getStatus())
                .setPlannedTime(reqBO.getPlannedTime())
                .setStartTime(startTime)
                .setTriggerSource(resolveTriggerSource(reqBO))
                .setHandlerUserId(reqBO.getHandlerUserId())
                .setTotalCount(1)
                .setSuccessCount(0)
                .setSkipCount(0)
                .setFailedCount(0)
                .setRetryCount(resolveRetryCount(reqBO));
    }

    private static void validateReq(ClubPointActivitySettlementJobReqBO reqBO) {
        if (reqBO == null || !StringUtils.hasText(reqBO.getRunKey()) || reqBO.getActivityId() == null) {
            throw new IllegalArgumentException("Activity settlement job request is invalid");
        }
    }

    private static String buildJobIdempotencyKey(ClubPointActivitySettlementJobReqBO reqBO) {
        return TASK_TYPE_ACTIVITY_SETTLEMENT + "_JOB:" + reqBO.getRunKey() + ":" + resolveRetryCount(reqBO);
    }

    private static String buildSettlementRunKey(ClubPointActivitySettlementJobReqBO reqBO) {
        return TASK_TYPE_ACTIVITY_SETTLEMENT + "_JOB:" + reqBO.getRunKey() + ":"
                + resolveRetryCount(reqBO) + ":" + reqBO.getActivityId();
    }

    private static Integer resolveTriggerSource(ClubPointActivitySettlementJobReqBO reqBO) {
        return reqBO.getTriggerSource() != null ? reqBO.getTriggerSource()
                : ClubPointActivitySettlementTriggerSourceEnum.SCHEDULED.getSource();
    }

    private static Integer resolveRetryCount(ClubPointActivitySettlementJobReqBO reqBO) {
        return reqBO.getRetryCount() != null ? reqBO.getRetryCount() : DEFAULT_RETRY_COUNT;
    }

    private static String buildResult(Long jobRunId, Long settlementRunId) {
        return "activitySettlementJobRunId=" + jobRunId + ", settlementRunId=" + settlementRunId;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

}

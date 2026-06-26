package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearUserReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 年度清零 Job 编排服务
 */
@Service
public class ClubPointAnnualClearingJobService {

    private static final String TASK_TYPE_ANNUAL_CLEARING = "ANNUAL_CLEARING";
    private static final String BIZ_TYPE_YEAR = "YEAR";
    private static final String AUDIT_BIZ_TYPE_ANNUAL_CLEARING_JOB = "ANNUAL_CLEARING_JOB";
    private static final int DEFAULT_RETRY_COUNT = 0;
    private static final int TRIGGER_SCHEDULED = 1;
    private static final int ERROR_MESSAGE_MAX_LENGTH = 2048;

    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;
    @Resource
    private ClubPointAnnualClearingService annualClearingService;
    @Resource
    private ClubAuditService auditService;

    public String run(ClubPointAnnualClearingJobReqBO reqBO) throws Exception {
        validateReq(reqBO);
        String idempotencyKey = buildJobIdempotencyKey(reqBO);
        ClubJobRunDO existing = jobRunMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return buildResult(existing.getId());
        }

        if (requiresManualAudit(reqBO)) {
            auditService.createAuditLog(buildManualAuditReq(reqBO));
        }

        List<Long> targetUserIds = resolveTargetUserIds(reqBO);
        ClubJobRunDO jobRun = insertRunningJobRun(buildRunningJobRun(reqBO, idempotencyKey,
                targetUserIds.size(), LocalDateTime.now()));
        RunResult result = runUsers(reqBO, jobRun, targetUserIds);
        if (result.failedCount > 0) {
            updateRetryableFailure(jobRun, result);
            throw new IllegalStateException("Annual clearing failed users: " + result.failedUserIds);
        }
        updateSuccess(jobRun, result);
        return buildResult(jobRun.getId());
    }

    private RunResult runUsers(ClubPointAnnualClearingJobReqBO reqBO, ClubJobRunDO jobRun,
                               List<Long> targetUserIds) {
        RunResult result = new RunResult(reqBO.getYear(), targetUserIds);
        for (Long userId : targetUserIds) {
            try {
                Long recordId = annualClearingService.clearUser(new ClubPointAnnualClearUserReqBO()
                        .setYear(reqBO.getYear())
                        .setUserId(userId)
                        .setRunId(jobRun.getId())
                        .setClearTime(reqBO.getClearTime())
                        .setOperatorUserId(reqBO.getHandlerUserId())
                        .setReason(resolveReason(reqBO)));
                countRecordResult(result, userId, clearingRecordMapper.selectById(recordId));
            } catch (Exception ex) {
                result.addFailure(userId, ex);
            }
        }
        return result;
    }

    private List<Long> resolveTargetUserIds(ClubPointAnnualClearingJobReqBO reqBO) {
        if (reqBO.getUserIds() != null && !reqBO.getUserIds().isEmpty()) {
            return distinct(reqBO.getUserIds());
        }
        List<Long> userIds = new ArrayList<>();
        for (ClubPointAccountDO account : accountMapper.selectListForAnnualClearing()) {
            userIds.add(account.getUserId());
        }
        return userIds;
    }

    private static List<Long> distinct(List<Long> userIds) {
        Set<Long> set = new LinkedHashSet<>();
        for (Long userId : userIds) {
            if (userId != null) {
                set.add(userId);
            }
        }
        return new ArrayList<>(set);
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

    private void updateSuccess(ClubJobRunDO jobRun, RunResult result) {
        applyResult(jobRun, result)
                .setStatus(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus())
                .setErrorType(null)
                .setErrorMessage(null)
                .setNextRetryTime(null);
        jobRunMapper.updateById(jobRun);
    }

    private void updateRetryableFailure(ClubJobRunDO jobRun, RunResult result) {
        applyResult(jobRun, result)
                .setStatus(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus())
                .setNextRetryTime(LocalDateTime.now().plusMinutes(5))
                .setErrorType(truncate(result.errorType, 128))
                .setErrorMessage(truncate(result.errorMessage, ERROR_MESSAGE_MAX_LENGTH));
        jobRunMapper.updateById(jobRun);
    }

    private static ClubJobRunDO applyResult(ClubJobRunDO jobRun, RunResult result) {
        return jobRun.setEndTime(LocalDateTime.now())
                .setSuccessCount(result.successCount)
                .setSkipCount(result.skipCount)
                .setFailedCount(result.failedCount)
                .setResultJson(result.toJson());
    }

    private static void countRecordResult(RunResult result, Long userId, ClubPointAnnualClearingRecordDO record) {
        if (record == null) {
            result.addFailure(userId, new IllegalStateException("Annual clearing record is missing"));
            return;
        }
        if (ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus().equals(record.getStatus())) {
            result.successCount++;
            result.clearedUserIds.add(userId);
        } else if (ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus().equals(record.getStatus())) {
            result.skipCount++;
            result.skippedUserIds.add(userId);
        } else {
            result.addFailure(userId, new IllegalStateException("Annual clearing record status is " + record.getStatus()));
        }
    }

    private static ClubJobRunDO buildRunningJobRun(ClubPointAnnualClearingJobReqBO reqBO, String idempotencyKey,
                                                   int totalCount, LocalDateTime startTime) {
        return new ClubJobRunDO()
                .setTaskType(TASK_TYPE_ANNUAL_CLEARING)
                .setBizType(BIZ_TYPE_YEAR)
                .setBizId(reqBO.getYear().longValue())
                .setRunKey(reqBO.getRunKey())
                .setIdempotencyKey(idempotencyKey)
                .setStatus(ClubPointSettlementRunStatusEnum.RUNNING.getStatus())
                .setPlannedTime(reqBO.getPlannedTime())
                .setStartTime(startTime)
                .setTriggerSource(resolveTriggerSource(reqBO))
                .setHandlerUserId(reqBO.getHandlerUserId())
                .setTotalCount(totalCount)
                .setSuccessCount(0)
                .setSkipCount(0)
                .setFailedCount(0)
                .setRetryCount(resolveRetryCount(reqBO))
                .setManualHandleReason(reqBO.getManualHandleReason());
    }

    private static ClubAuditCreateReqBO buildManualAuditReq(ClubPointAnnualClearingJobReqBO reqBO) {
        return new ClubAuditCreateReqBO()
                .setActionType(ClubAuditActionTypeConstants.ANNUAL_CLEARING_MANUAL)
                .setBizType(AUDIT_BIZ_TYPE_ANNUAL_CLEARING_JOB)
                .setBizId(reqBO.getYear().longValue())
                .setOperatorUserId(reqBO.getHandlerUserId())
                .setOperatorNameSnapshot(reqBO.getOperatorNameSnapshot())
                .setOperatorRoleSnapshot(reqBO.getOperatorRoleSnapshot())
                .setOperationTime(LocalDateTime.now())
                .setClientIp(reqBO.getClientIp())
                .setUserAgent(reqBO.getUserAgent())
                .setReason(resolveReason(reqBO))
                .setTargetSnapshotJson(buildAuditSnapshot(reqBO))
                .setSuccess(true);
    }

    private static String buildAuditSnapshot(ClubPointAnnualClearingJobReqBO reqBO) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("year", reqBO.getYear());
        map.put("runKey", reqBO.getRunKey());
        map.put("retryCount", resolveRetryCount(reqBO));
        map.put("triggerSource", resolveTriggerSource(reqBO));
        map.put("userIds", reqBO.getUserIds());
        return JsonUtils.toJsonString(map);
    }

    private static boolean requiresManualAudit(ClubPointAnnualClearingJobReqBO reqBO) {
        return !Integer.valueOf(TRIGGER_SCHEDULED).equals(resolveTriggerSource(reqBO))
                || StringUtils.hasText(reqBO.getManualHandleReason());
    }

    private static void validateReq(ClubPointAnnualClearingJobReqBO reqBO) {
        if (reqBO == null || reqBO.getYear() == null || !StringUtils.hasText(reqBO.getRunKey())) {
            throw new IllegalArgumentException("Annual clearing job request is invalid");
        }
    }

    private static String buildJobIdempotencyKey(ClubPointAnnualClearingJobReqBO reqBO) {
        return TASK_TYPE_ANNUAL_CLEARING + "_JOB:" + reqBO.getRunKey() + ":" + resolveRetryCount(reqBO);
    }

    private static Integer resolveTriggerSource(ClubPointAnnualClearingJobReqBO reqBO) {
        return reqBO.getTriggerSource() != null ? reqBO.getTriggerSource() : TRIGGER_SCHEDULED;
    }

    private static Integer resolveRetryCount(ClubPointAnnualClearingJobReqBO reqBO) {
        return reqBO.getRetryCount() != null ? reqBO.getRetryCount() : DEFAULT_RETRY_COUNT;
    }

    private static String resolveReason(ClubPointAnnualClearingJobReqBO reqBO) {
        return StringUtils.hasText(reqBO.getManualHandleReason())
                ? reqBO.getManualHandleReason() : ClubPointAnnualClearingConstants.SOURCE_TITLE;
    }

    private static String buildResult(Long jobRunId) {
        return "annualClearingJobRunId=" + jobRunId;
    }

    private static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private static final class RunResult {

        private final Integer year;
        private final List<Long> targetUserIds;
        private final List<Long> clearedUserIds = new ArrayList<>();
        private final List<Long> skippedUserIds = new ArrayList<>();
        private final List<Long> failedUserIds = new ArrayList<>();
        private final Map<Long, String> failedMessages = new LinkedHashMap<>();
        private int successCount;
        private int skipCount;
        private int failedCount;
        private String errorType;
        private String errorMessage;

        private RunResult(Integer year, List<Long> targetUserIds) {
            this.year = year;
            this.targetUserIds = targetUserIds;
        }

        private void addFailure(Long userId, Exception ex) {
            failedCount++;
            failedUserIds.add(userId);
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
            failedMessages.put(userId, message);
            if (errorType == null) {
                errorType = ex.getClass().getSimpleName();
                errorMessage = "userId=" + userId + ": " + message;
            }
        }

        private String toJson() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("year", year);
            map.put("targetUserIds", targetUserIds);
            map.put("clearedUserIds", clearedUserIds);
            map.put("skippedUserIds", skippedUserIds);
            map.put("failedUserIds", failedUserIds);
            map.put("failedMessages", failedMessages);
            return JsonUtils.toJsonString(map);
        }

    }

}

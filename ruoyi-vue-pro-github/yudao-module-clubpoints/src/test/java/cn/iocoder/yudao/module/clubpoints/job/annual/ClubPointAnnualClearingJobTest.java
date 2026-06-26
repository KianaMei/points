package cn.iocoder.yudao.module.clubpoints.job.annual;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.job.annual.ClubPointAnnualClearingJob;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingJobService;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointAnnualClearingJob.class,
        ClubPointAnnualClearingJobService.class,
        ClubPointAnnualClearingServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class,
        ClubAuditServiceImpl.class
})
class ClubPointAnnualClearingJobTest extends BaseDbUnitTest {

    private static final Integer YEAR = 2026;
    private static final LocalDateTime CLEAR_TIME = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime PLANNED_TIME = LocalDateTime.of(2026, 1, 1, 2, 0);
    private static final String TASK_TYPE_ANNUAL_CLEARING = "ANNUAL_CLEARING";
    private static final String BIZ_TYPE_YEAR = "YEAR";
    private static final Integer TRIGGER_SCHEDULED = 1;
    private static final Integer TRIGGER_MANUAL = 2;
    private static final Integer TRIGGER_RETRY = 3;

    @Resource
    private ClubPointAnnualClearingJob job;
    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @Test
    void executeManualJobShouldClearAllAccountsWriteJobRunAndAudit() throws Exception {
        insertPublishedRuleVersion("M10-4-MANUAL-RULE");
        insertAccount(301L, 70, 0, 70, 20, 50);
        insertAccount(302L, 40, 0, 40, 40, 0);

        String result = executeJob(buildReq("M10-4-MANUAL", TRIGGER_MANUAL, 0)
                .setManualHandleReason("manual annual clearing"));

        ClubJobRunDO jobRun = jobRunMapper.selectByIdempotencyKey("ANNUAL_CLEARING_JOB:M10-4-MANUAL:0");
        assertNotNull(jobRun);
        assertTrue(result.contains(String.valueOf(jobRun.getId())));
        assertEquals(TASK_TYPE_ANNUAL_CLEARING, jobRun.getTaskType());
        assertEquals(BIZ_TYPE_YEAR, jobRun.getBizType());
        assertEquals(YEAR.longValue(), jobRun.getBizId());
        assertEquals(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), jobRun.getStatus());
        assertEquals(TRIGGER_MANUAL, jobRun.getTriggerSource());
        assertEquals(9101L, jobRun.getHandlerUserId());
        assertEquals(2, jobRun.getTotalCount());
        assertEquals(1, jobRun.getSuccessCount());
        assertEquals(1, jobRun.getSkipCount());
        assertEquals(0, jobRun.getFailedCount());
        assertTrue(jobRun.getResultJson().contains("\"year\":2026"));
        assertTrue(jobRun.getResultJson().contains("\"clearedUserIds\":[301]"));
        assertTrue(jobRun.getResultJson().contains("\"skippedUserIds\":[302]"));

        ClubPointAnnualClearingRecordDO cleared = clearingRecordMapper.selectByUserIdAndYear(301L, YEAR);
        assertNotNull(cleared);
        assertEquals(jobRun.getId(), cleared.getRunId());
        assertEquals(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus(), cleared.getStatus());
        assertEquals(1L, transactionMapper.selectCount());
        assertEquals(0, accountMapper.selectByUserId(301L).getAvailablePoints());
        assertEquals(20, accountMapper.selectByUserId(301L).getFrozenPoints());

        ClubPointAnnualClearingRecordDO skipped = clearingRecordMapper.selectByUserIdAndYear(302L, YEAR);
        assertNotNull(skipped);
        assertEquals(ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus(), skipped.getStatus());

        ClubAuditLogDO auditLog = auditLogMapper.selectList().get(0);
        assertEquals(ClubAuditActionTypeConstants.ANNUAL_CLEARING_MANUAL, auditLog.getActionType());
        assertEquals("ANNUAL_CLEARING_JOB", auditLog.getBizType());
        assertEquals(9101L, auditLog.getOperatorUserId());
        assertEquals("manual annual clearing", auditLog.getReason());
    }

    @Test
    void executeSameRunShouldReturnExistingJobRunAndNotDuplicateTransactions() throws Exception {
        insertPublishedRuleVersion("M10-4-IDEMPOTENT-RULE");
        insertAccount(311L, 80, 0, 80, 10, 70);

        String firstResult = executeJob(buildReq("M10-4-IDEMPOTENT", TRIGGER_MANUAL, 0));
        String secondResult = executeJob(buildReq("M10-4-IDEMPOTENT", TRIGGER_MANUAL, 0));

        assertEquals(firstResult, secondResult);
        assertEquals(1L, jobRunMapper.selectCount());
        assertEquals(1L, clearingRecordMapper.selectCount());
        assertEquals(1L, transactionMapper.selectCount());
        assertEquals(0, accountMapper.selectByUserId(311L).getAvailablePoints());
    }

    @Test
    void executeShouldRecordFailedUsersAndRetrySpecifiedUsers() throws Exception {
        insertAccount(321L, 30, 0, 30, 30, 0);
        insertAccount(322L, 50, 0, 50, 0, 50);

        assertThrows(Exception.class, () -> executeJob(buildReq("M10-4-FAIL", TRIGGER_SCHEDULED, 0)
                .setOperatorNameSnapshot(null)));

        ClubJobRunDO failedJobRun = jobRunMapper.selectByIdempotencyKey("ANNUAL_CLEARING_JOB:M10-4-FAIL:0");
        assertNotNull(failedJobRun);
        assertEquals(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus(), failedJobRun.getStatus());
        assertEquals(2, failedJobRun.getTotalCount());
        assertEquals(0, failedJobRun.getSuccessCount());
        assertEquals(1, failedJobRun.getSkipCount());
        assertEquals(1, failedJobRun.getFailedCount());
        assertTrue(failedJobRun.getResultJson().contains("\"failedUserIds\":[322]"));
        assertNotNull(failedJobRun.getErrorType());
        assertNotNull(failedJobRun.getErrorMessage());

        insertPublishedRuleVersion("M10-4-RETRY-RULE");
        executeJob(buildReq("M10-4-FAIL", TRIGGER_RETRY, 1)
                .setUserIds(Collections.singletonList(322L))
                .setManualHandleReason("retry failed user"));

        ClubJobRunDO retryJobRun = jobRunMapper.selectByIdempotencyKey("ANNUAL_CLEARING_JOB:M10-4-FAIL:1");
        assertNotNull(retryJobRun);
        assertEquals(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), retryJobRun.getStatus());
        assertEquals(TRIGGER_RETRY, retryJobRun.getTriggerSource());
        assertEquals(1, retryJobRun.getTotalCount());
        assertEquals(1, retryJobRun.getSuccessCount());
        assertEquals(0, retryJobRun.getFailedCount());
        assertEquals(1L, transactionMapper.selectCount());
        assertEquals(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus(),
                clearingRecordMapper.selectByUserIdAndYear(322L, YEAR).getStatus());
    }

    @Test
    void manualJobShouldNotStartWhenAuditFails() {
        insertPublishedRuleVersion("M10-4-AUDIT-FAIL-RULE");
        insertAccount(331L, 30, 0, 30, 0, 30);

        assertThrows(Exception.class, () -> executeJob(buildReq("M10-4-AUDIT-FAIL", TRIGGER_MANUAL, 0)
                .setOperatorNameSnapshot(null)
                .setManualHandleReason("audit failure")));

        assertEquals(0L, jobRunMapper.selectCount());
        assertEquals(0L, clearingRecordMapper.selectCount());
        assertEquals(0L, transactionMapper.selectCount());
        assertEquals(30, accountMapper.selectByUserId(331L).getAvailablePoints());
    }

    private String executeJob(ClubPointAnnualClearingJobReqBO reqBO) throws Exception {
        return job.execute(JsonUtils.toJsonString(reqBO));
    }

    private static ClubPointAnnualClearingJobReqBO buildReq(String runKey, Integer triggerSource, Integer retryCount) {
        return new ClubPointAnnualClearingJobReqBO()
                .setYear(YEAR)
                .setRunKey(runKey)
                .setTriggerSource(triggerSource)
                .setRetryCount(retryCount)
                .setHandlerUserId(9101L)
                .setOperatorNameSnapshot("Annual Admin")
                .setOperatorRoleSnapshot("admin")
                .setPlannedTime(PLANNED_TIME)
                .setClearTime(CLEAR_TIME);
    }

    private ClubPointRuleVersionDO insertPublishedRuleVersion(String versionNo) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo(versionNo)
                .setName(versionNo)
                .setStatus(2)
                .setPublicityTime(LocalDateTime.of(2025, 12, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2025, 12, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2025, 12, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);
        return version;
    }

    private void insertAccount(Long userId, Integer totalPositivePoints, Integer totalNegativePoints,
                               Integer netPoints, Integer frozenPoints, Integer availablePoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(totalPositivePoints)
                .setTotalNegativePoints(totalNegativePoints)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(totalPositivePoints)
                .setVersion(1));
    }

}

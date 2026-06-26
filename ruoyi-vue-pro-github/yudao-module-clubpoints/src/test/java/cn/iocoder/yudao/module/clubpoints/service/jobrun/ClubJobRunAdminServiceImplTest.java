package cn.iocoder.yudao.module.clubpoints.service.jobrun;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingJobService;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubJobRunAdminServiceImpl.class,
        ClubAuditServiceImpl.class,
        ClubJobRunAdminServiceImplTest.StubConfig.class
})
class ClubJobRunAdminServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 26, 9, 0);
    private static final Integer TRIGGER_RETRY = 3;

    @Resource
    private ClubJobRunAdminService jobRunAdminService;
    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private StubRetryDispatcher retryDispatcher;

    @BeforeEach
    void resetStubs() {
        retryDispatcher.reset();
    }

    @Test
    void getPageAndDetailShouldReadJobRunRecords() {
        Long settlementRunId = insertJobRun("ACTIVITY_SETTLEMENT", "ACTIVITY", 1001L,
                "RUN-M11-5-ACTIVITY", ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus(), 0,
                "{\"activityIds\":[1001]}");
        insertJobRun("ANNUAL_CLEARING", "YEAR", 2026L,
                "RUN-M11-5-ANNUAL", ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), 0,
                "{\"year\":2026}");

        ClubJobRunPageReqBO pageReqBO = new ClubJobRunPageReqBO()
                .setTaskType("ACTIVITY_SETTLEMENT")
                .setStatus(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus());
        pageReqBO.setPageNo(1);
        pageReqBO.setPageSize(10);

        PageResult<ClubJobRunDO> page = jobRunAdminService.getJobRunPage(pageReqBO);

        assertEquals(1L, page.getTotal());
        assertEquals(settlementRunId, page.getList().get(0).getId());

        ClubJobRunDO detail = jobRunAdminService.getJobRunDetail(settlementRunId);
        assertEquals("RUN-M11-5-ACTIVITY", detail.getRunKey());
        assertEquals("ACTIVITY", detail.getBizType());
    }

    @Test
    void handleShouldRetryFailedActivitySettlementAndWriteAudit() throws Exception {
        retryDispatcher.activityResult = "activity retry ok";
        Long failedRunId = insertJobRun("ACTIVITY_SETTLEMENT", "ACTIVITY", 1001L,
                "RUN-M11-5-ACTIVITY-RETRY", ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus(), 1,
                "{\"activityIds\":[1001]}");

        String result = jobRunAdminService.handleJobRun(buildHandleReq(failedRunId));

        assertEquals("activity retry ok", result);
        assertEquals(1, retryDispatcher.activityInvocations);
        assertEquals("RUN-M11-5-ACTIVITY-RETRY", retryDispatcher.activityReq.getRunKey());
        assertEquals(1001L, retryDispatcher.activityReq.getActivityId());
        assertEquals(TRIGGER_RETRY, retryDispatcher.activityReq.getTriggerSource());
        assertEquals(2, retryDispatcher.activityReq.getRetryCount());
        assertEquals(9101L, retryDispatcher.activityReq.getHandlerUserId());

        ClubAuditLogDO auditLog = auditLogMapper.selectList().get(0);
        assertEquals(ClubAuditActionTypeConstants.JOB_RUN_RETRY, auditLog.getActionType());
        assertEquals("JOB_RUN", auditLog.getBizType());
        assertEquals(failedRunId, auditLog.getBizId());
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"retryCount\":2"));
    }

    @Test
    void handleShouldRetryOnlyFailedUsersForAnnualClearing() throws Exception {
        retryDispatcher.annualResult = "annual retry ok";
        Long failedRunId = insertJobRun("ANNUAL_CLEARING", "YEAR", 2026L,
                "RUN-M11-5-ANNUAL-RETRY", ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus(), 0,
                "{\"year\":2026,\"failedUserIds\":[322]}");

        String result = jobRunAdminService.handleJobRun(buildHandleReq(failedRunId));

        assertEquals("annual retry ok", result);
        assertEquals(1, retryDispatcher.annualInvocations);
        assertEquals(2026, retryDispatcher.annualReq.getYear());
        assertEquals("RUN-M11-5-ANNUAL-RETRY", retryDispatcher.annualReq.getRunKey());
        assertEquals(Collections.singletonList(322L), retryDispatcher.annualReq.getUserIds());
        assertEquals(TRIGGER_RETRY, retryDispatcher.annualReq.getTriggerSource());
        assertEquals(1, retryDispatcher.annualReq.getRetryCount());
        assertEquals("retry failed job", retryDispatcher.annualReq.getManualHandleReason());
    }

    @Test
    void handleShouldRejectNonFailedJobRun() {
        Long successRunId = insertJobRun("ACTIVITY_SETTLEMENT", "ACTIVITY", 1001L,
                "RUN-M11-5-SUCCESS", ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), 0,
                "{\"activityIds\":[1001]}");

        assertThrows(ServiceException.class, () -> jobRunAdminService.handleJobRun(buildHandleReq(successRunId)));
    }

    private ClubJobRunHandleReqBO buildHandleReq(Long id) {
        return new ClubJobRunHandleReqBO()
                .setId(id)
                .setReason("retry failed job")
                .setOperatorUserId(9101L)
                .setOperatorNameSnapshot("Job Admin")
                .setOperatorRoleSnapshot("admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private Long insertJobRun(String taskType, String bizType, Long bizId, String runKey, Integer status,
                              Integer retryCount, String resultJson) {
        ClubJobRunDO jobRun = new ClubJobRunDO()
                .setTaskType(taskType)
                .setBizType(bizType)
                .setBizId(bizId)
                .setRunKey(runKey)
                .setIdempotencyKey(taskType + "_JOB:" + runKey + ":" + retryCount)
                .setStatus(status)
                .setPlannedTime(BASE_TIME)
                .setStartTime(BASE_TIME.plusMinutes(1))
                .setEndTime(BASE_TIME.plusMinutes(2))
                .setTriggerSource(1)
                .setHandlerUserId(9001L)
                .setTotalCount(1)
                .setSuccessCount(0)
                .setSkipCount(0)
                .setFailedCount(1)
                .setRetryCount(retryCount)
                .setNextRetryTime(BASE_TIME.plusMinutes(5))
                .setErrorType(status.equals(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus())
                        ? "IllegalStateException" : null)
                .setErrorMessage(status.equals(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus())
                        ? "failed" : null)
                .setResultJson(resultJson);
        jobRunMapper.insert(jobRun);
        assertNotNull(jobRun.getId());
        return jobRun.getId();
    }

    @TestConfiguration
    static class StubConfig {

        @Bean
        @Primary
        StubRetryDispatcher retryDispatcher() {
            return new StubRetryDispatcher();
        }

    }

    static class StubRetryDispatcher implements ClubJobRunRetryDispatcher {

        private ClubPointActivitySettlementJobReqBO activityReq;
        private ClubPointAnnualClearingJobReqBO annualReq;
        private String activityResult;
        private String annualResult;
        private int activityInvocations;
        private int annualInvocations;

        @Override
        public String retryActivitySettlement(ClubPointActivitySettlementJobReqBO reqBO) {
            this.activityReq = reqBO;
            this.activityInvocations++;
            return activityResult;
        }

        @Override
        public String retryAnnualClearing(ClubPointAnnualClearingJobReqBO reqBO) {
            this.annualReq = reqBO;
            this.annualInvocations++;
            return annualResult;
        }

        private void reset() {
            this.activityReq = null;
            this.annualReq = null;
            this.activityResult = null;
            this.annualResult = null;
            this.activityInvocations = 0;
            this.annualInvocations = 0;
        }

    }

}

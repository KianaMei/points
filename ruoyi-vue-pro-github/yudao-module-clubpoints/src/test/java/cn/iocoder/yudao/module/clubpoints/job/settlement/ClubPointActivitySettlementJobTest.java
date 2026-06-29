package cn.iocoder.yudao.module.clubpoints.job.settlement;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityPointConfigVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.job.ClubJobRunMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.settlement.ClubPointActivitySettlementRunMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivitySettlementTriggerSourceEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointSettlementRunStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementJobService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointActivitySettlementJob.class,
        ClubPointActivitySettlementJobService.class,
        ClubPointActivitySettlementServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class,
        ClubAuditServiceImpl.class
})
class ClubPointActivitySettlementJobTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);
    private static final String JOB_TASK_ACTIVITY_SETTLEMENT = "ACTIVITY_SETTLEMENT";
    private static final String JOB_BIZ_ACTIVITY = "ACTIVITY";

    @Resource
    private ClubPointActivitySettlementJob job;
    @Resource
    private ClubJobRunMapper jobRunMapper;
    @Resource
    private ClubPointActivitySettlementRunMapper settlementRunMapper;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityPointConfigVersionMapper configVersionMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @Test
    void executeShouldSettleActivityAndWriteJobRun() throws Exception {
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub();
        ClubPointActivityDO activity = insertEndedActivity(club);
        insertConfigVersion(activity, ruleVersion);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 7301L, "Job User");
        insertAttendance(registration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                activity.getStartTime());

        String result = executeJob("M7-5-JOB-1", activity.getId(),
                ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource(), 0);

        ClubJobRunDO jobRun = jobRunMapper.selectByIdempotencyKey("ACTIVITY_SETTLEMENT_JOB:M7-5-JOB-1:0");
        assertNotNull(jobRun);
        assertTrue(result.contains(String.valueOf(jobRun.getId())));
        assertEquals(JOB_TASK_ACTIVITY_SETTLEMENT, jobRun.getTaskType());
        assertEquals(JOB_BIZ_ACTIVITY, jobRun.getBizType());
        assertEquals(activity.getId(), jobRun.getBizId());
        assertEquals(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), jobRun.getStatus());
        assertEquals(ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource(),
                jobRun.getTriggerSource());
        assertEquals(9001L, jobRun.getHandlerUserId());
        assertEquals(1, jobRun.getTotalCount());
        assertEquals(1, jobRun.getSuccessCount());
        assertEquals(0, jobRun.getFailedCount());
        assertTrue(jobRun.getResultJson().contains("\"activityIds\":["));

        ClubPointActivitySettlementRunDO settlementRun = settlementRunMapper.selectByRunKey(
                "ACTIVITY_SETTLEMENT_JOB:M7-5-JOB-1:0:" + activity.getId());
        assertNotNull(settlementRun);
        assertEquals(jobRun.getId(), settlementRun.getJobRunId());
        ClubPointTransactionDO baseTransaction = transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.BASE.buildIdempotencyKey(activity.getId(), 7301L, 202607));
        assertNotNull(baseTransaction);
        assertEquals(8, baseTransaction.getPoints());
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(), activityMapper.selectById(activity.getId()).getStatus());
    }

    @Test
    void executeRetryShouldNotDuplicateLedgerTransactions() throws Exception {
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub();
        ClubPointActivityDO activity = insertEndedActivity(club);
        insertConfigVersion(activity, ruleVersion);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 7302L, "Retry User");
        insertAttendance(registration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                activity.getStartTime());

        executeJob("M7-5-RETRY-SUCCESS", activity.getId(),
                ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource(), 0);
        executeJob("M7-5-RETRY-SUCCESS", activity.getId(), 3, 1);

        assertEquals(1L, countSettlementTransactions(activity.getId()));
        assertNotNull(jobRunMapper.selectByIdempotencyKey("ACTIVITY_SETTLEMENT_JOB:M7-5-RETRY-SUCCESS:0"));
        ClubJobRunDO retryJobRun = jobRunMapper.selectByIdempotencyKey("ACTIVITY_SETTLEMENT_JOB:M7-5-RETRY-SUCCESS:1");
        assertNotNull(retryJobRun);
        assertEquals(3, retryJobRun.getTriggerSource());
        assertEquals(1, retryJobRun.getRetryCount());
        assertEquals(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), retryJobRun.getStatus());
    }

    @Test
    void executeShouldRecordRetryableFailureWhenSettlementFails() {
        ClubPointClubDO club = insertClub();
        ClubPointActivityDO activity = insertEndedActivity(club);
        insertRegistration(activity, 7303L, "Failure User");

        assertThrows(Exception.class, () -> executeJob("M7-5-FAIL", activity.getId(),
                ClubPointActivitySettlementTriggerSourceEnum.SCHEDULED.getSource(), 0));

        ClubJobRunDO jobRun = jobRunMapper.selectByIdempotencyKey("ACTIVITY_SETTLEMENT_JOB:M7-5-FAIL:0");
        assertNotNull(jobRun);
        assertEquals(ClubPointSettlementRunStatusEnum.RETRYABLE_FAILED.getStatus(), jobRun.getStatus());
        assertEquals(1, jobRun.getTotalCount());
        assertEquals(0, jobRun.getSuccessCount());
        assertEquals(1, jobRun.getFailedCount());
        assertNotNull(jobRun.getNextRetryTime());
        assertNotNull(jobRun.getErrorType());
        assertNotNull(jobRun.getErrorMessage());
    }

    @Test
    void executeBlankParamShouldScanEndedActivitiesAndSettleAutomatically() throws Exception {
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub();
        ClubPointActivityDO first = insertEndedActivity(club);
        insertConfigVersion(first, ruleVersion);
        ClubPointActivityRegistrationDO firstRegistration = insertRegistration(first, 7304L, "Auto User A");
        insertAttendance(firstRegistration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                first.getStartTime());
        ClubPointActivityDO second = insertEndedActivity(club);
        insertConfigVersion(second, ruleVersion);
        ClubPointActivityRegistrationDO secondRegistration = insertRegistration(second, 7305L, "Auto User B");
        insertAttendance(secondRegistration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                second.getStartTime());

        String result = job.execute("");

        assertTrue(result.contains("\"successCount\":2"));
        assertEquals(2L, jobRunMapper.selectList().size());
        assertEquals(2L, settlementRunMapper.selectList().size());
        assertEquals(1L, countSettlementTransactions(first.getId()));
        assertEquals(1L, countSettlementTransactions(second.getId()));
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(),
                activityMapper.selectById(first.getId()).getStatus());
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(),
                activityMapper.selectById(second.getId()).getStatus());
    }

    private String executeJob(String runKey, Long activityId, Integer triggerSource, Integer retryCount)
            throws Exception {
        return job.execute(JsonUtils.toJsonString(new ClubPointActivitySettlementJobReqBO()
                .setRunKey(runKey)
                .setActivityId(activityId)
                .setTriggerSource(triggerSource)
                .setRetryCount(retryCount)
                .setHandlerUserId(9001L)
                .setPlannedTime(BASE_TIME.plusDays(5))));
    }

    private ClubPointRuleVersionDO seedSettlementRules() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("M7-JOB-RULE-001")
                .setName("M7 job rules")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(BASE_TIME.minusDays(1))
                .setPublishedTime(BASE_TIME.minusDays(1));
        ruleVersionMapper.insert(version);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE.getCode(),
                "Medium base", ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), 8, 1);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode(),
                "Full extra", ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(), 2, 2);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT.getCode(),
                "Absence single", ClubPointCategoryEnum.DEDUCTION.getCategory(), 3, 3);
        insertIntRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_THRESHOLD.getCode(),
                "Absence monthly threshold", ClubPointCategoryEnum.DEDUCTION.getCategory(), 3, 4);
        insertRuleItem(version.getId(), ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT.getCode(),
                "Absence monthly", ClubPointCategoryEnum.DEDUCTION.getCategory(), 20, 5);
        return version;
    }

    private void insertRuleItem(Long versionId, String code, String name, Integer category,
                                Integer defaultPoints, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(1)
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(30)
                .setDefaultPoints(defaultPoints)
                .setStatus(1)
                .setSort(sort));
    }

    private void insertIntRuleItem(Long versionId, String code, String name, Integer category,
                                   Integer intValue, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(2)
                .setCategory(category)
                .setIntValue(intValue)
                .setStatus(1)
                .setSort(sort));
    }

    private ClubPointClubDO insertClub() {
        ClubPointClubDO club = new ClubPointClubDO()
                .setCode("CLUB-M7-JOB")
                .setName("M7 Job Club")
                .setStatus(ClubPointClubStatusEnum.ENABLED.getStatus())
                .setDescription("desc")
                .setContactText("contact")
                .setSort(10)
                .setRemark("remark");
        clubMapper.insert(club);
        return club;
    }

    private ClubPointActivityDO insertEndedActivity(ClubPointClubDO club) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle("M7 Job Activity")
                .setLocation("Gym")
                .setDescription("Settlement job activity")
                .setLevel(2)
                .setStatus(ClubPointActivityStatusEnum.ENDED.getStatus())
                .setStartTime(BASE_TIME.plusDays(3))
                .setEndTime(BASE_TIME.plusDays(3).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(2))
                .setCancelDeadlineTime(BASE_TIME.plusDays(2).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(3).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(3).plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(BASE_TIME.plusDays(3).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(3).plusHours(3))
                .setCreatorUserId(2000L)
                .setSnapshotJson("{}")
                .setRemark("activity remark");
        activityMapper.insert(activity);
        return activity;
    }

    private void insertConfigVersion(ClubPointActivityDO activity, ClubPointRuleVersionDO ruleVersion) {
        ClubPointActivityPointConfigVersionDO config = new ClubPointActivityPointConfigVersionDO()
                .setActivityId(activity.getId())
                .setVersionNo(1)
                .setLevel(2)
                .setBasePoints(8)
                .setFullExtraPoints(2)
                .setRuleVersionId(ruleVersion.getId())
                .setEffectiveTime(activity.getStartTime())
                .setCreatedReason("publish")
                .setActive(true)
                .setRuleSnapshotJson("{}");
        configVersionMapper.insert(config);
        activity.setCurrentConfigVersionId(config.getId());
        activityMapper.updateById(activity);
    }

    private ClubPointActivityRegistrationDO insertRegistration(ClubPointActivityDO activity, Long userId,
                                                               String userName) {
        ClubPointActivityRegistrationDO registration = new ClubPointActivityRegistrationDO()
                .setActivityId(activity.getId())
                .setClubId(activity.getClubId())
                .setUserId(userId)
                .setStatus(ClubPointRegistrationStatusEnum.REGISTERED.getStatus())
                .setRegisterTime(BASE_TIME.plusDays(1))
                .setNoAbsenceDeduct(false)
                .setSpecialAbsenceFlag(false)
                .setUserNameSnapshot(userName)
                .setDeptIdSnapshot(61L)
                .setDeptNameSnapshot("Operations")
                .setMobileSnapshot("1390000" + userId)
                .setClubNameSnapshot(activity.getClubNameSnapshot())
                .setActivityTitleSnapshot(activity.getTitle())
                .setActivityStartTimeSnapshot(activity.getStartTime())
                .setActivityEndTimeSnapshot(activity.getEndTime())
                .setActiveUniqueKey(activity.getId() + ":" + userId);
        registrationMapper.insert(registration);
        return registration;
    }

    private void insertAttendance(ClubPointActivityRegistrationDO registration, Integer targetType,
                                  LocalDateTime recordTime) {
        attendanceRecordMapper.insert(new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType)
                .setRecordTime(recordTime)
                .setSourceType(1)
                .setOperatorUserId(registration.getUserId())
                .setClientIp("127.0.0.1")
                .setRemark("attendance"));
    }

    private long countSettlementTransactions(Long activityId) {
        return transactionMapper.selectList().stream()
                .filter(transaction -> activityId.equals(transaction.getActivityId()))
                .filter(transaction -> ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType()
                        .equals(transaction.getSourceType()))
                .count();
    }

}

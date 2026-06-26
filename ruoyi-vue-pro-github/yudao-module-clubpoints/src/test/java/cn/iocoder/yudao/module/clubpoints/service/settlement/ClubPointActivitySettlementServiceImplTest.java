package cn.iocoder.yudao.module.clubpoints.service.settlement;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityPointConfigVersionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
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
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementRunReqBO;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointActivitySettlementServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubPointRuleServiceImpl.class,
        ClubAuditServiceImpl.class
})
class ClubPointActivitySettlementServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Resource
    private ClubPointActivitySettlementService settlementService;
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
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @Test
    void settleActivityShouldCreateLedgerTransactionsRunRecordAndSettleActivity() {
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub();
        ClubPointActivityDO activity = insertEndedActivity(club);
        ClubPointActivityPointConfigVersionDO config = insertConfigVersion(activity, ruleVersion);
        ClubPointActivityRegistrationDO checkinOnly = insertRegistration(activity, 7101L, "Checkin Only", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        ClubPointActivityRegistrationDO fullAttendance = insertRegistration(activity, 7102L, "Full Attendance", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        ClubPointActivityRegistrationDO absent = insertRegistration(activity, 7103L, "Absent", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(activity, 7104L, "Special Absence", false, true,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(activity, 7105L, "No Deduct", true, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(activity, 7106L, "Canceled", true, false,
                ClubPointRegistrationStatusEnum.CANCELED.getStatus());
        insertAttendance(checkinOnly, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(3));
        insertAttendance(fullAttendance, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(3));
        insertAttendance(fullAttendance, ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType(), BASE_TIME.plusDays(3).plusHours(2));
        insertExistingAccount(absent.getUserId(), 12);

        Long runId = settlementService.settleActivity(new ClubPointActivitySettlementRunReqBO()
                .setActivityId(activity.getId())
                .setRunKey("M7-2-RUN-1")
                .setTriggerSource(ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource())
                .setOperatorUserId(9001L)
                .setSettlementTime(BASE_TIME.plusDays(4)));

        ClubPointActivitySettlementRunDO run = settlementRunMapper.selectById(runId);
        assertEquals(activity.getId(), run.getActivityId());
        assertEquals("M7-2-RUN-1", run.getRunKey());
        assertEquals(ClubPointSettlementRunStatusEnum.SUCCESS.getStatus(), run.getStatus());
        assertEquals(config.getId(), run.getConfigVersionId());
        assertEquals(6, run.getRegistrationCount());
        assertEquals(3, run.getSuccessCount());
        assertEquals(3, run.getSkipCount());
        assertEquals(0, run.getFailedCount());
        assertEquals(ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource(), run.getTriggerSource());
        assertEquals(9001L, run.getOperatorUserId());

        ClubPointTransactionDO baseOnlyTransaction = transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.BASE.buildIdempotencyKey(activity.getId(), checkinOnly.getUserId(), 202607));
        assertNotNull(baseOnlyTransaction);
        assertEquals(8, baseOnlyTransaction.getPoints());
        assertEquals(ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE.getCode(), baseOnlyTransaction.getRuleItemCodeSnapshot());
        assertEquals(ClubPointTransactionDirectionEnum.INCREASE.getDirection(), baseOnlyTransaction.getDirection());
        assertEquals(ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), baseOnlyTransaction.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(), baseOnlyTransaction.getSourceType());
        assertEquals(activity.getClubId(), baseOnlyTransaction.getIssuingClubId());
        assertEquals(activity.getId(), baseOnlyTransaction.getSourceId());
        assertEquals(checkinOnly.getId(), baseOnlyTransaction.getSourceItemId());
        assertEquals(activity.getEndTime(), baseOnlyTransaction.getOccurredAt());
        assertTrue(baseOnlyTransaction.getSnapshotJson().contains("\"itemType\":\"BASE\""));

        ClubPointTransactionDO fullBaseTransaction = transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.BASE.buildIdempotencyKey(activity.getId(), fullAttendance.getUserId(), 202607));
        ClubPointTransactionDO fullExtraTransaction = transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.FULL_EXTRA.buildIdempotencyKey(activity.getId(), fullAttendance.getUserId(), 202607));
        assertNotNull(fullBaseTransaction);
        assertNotNull(fullExtraTransaction);
        assertEquals(2, fullExtraTransaction.getPoints());
        assertEquals(ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA.getCode(), fullExtraTransaction.getRuleItemCodeSnapshot());
        assertEquals(ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(), fullExtraTransaction.getPointCategory());

        ClubPointTransactionDO absenceTransaction = transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.buildIdempotencyKey(activity.getId(), absent.getUserId(), 202607));
        assertNotNull(absenceTransaction);
        assertEquals(3, absenceTransaction.getPoints());
        assertEquals(ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT.getCode(), absenceTransaction.getRuleItemCodeSnapshot());
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), absenceTransaction.getDirection());
        assertEquals(ClubPointCategoryEnum.DEDUCTION.getCategory(), absenceTransaction.getPointCategory());

        assertNull(transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.BASE.buildIdempotencyKey(activity.getId(), 7104L, 202607)));
        assertNull(transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.buildIdempotencyKey(activity.getId(), 7105L, 202607)));
        assertNull(transactionMapper.selectByIdempotencyKey(
                ClubPointActivitySettlementItemTypeEnum.ABSENCE_SINGLE.buildIdempotencyKey(activity.getId(), 7106L, 202607)));

        assertEquals(8, accountMapper.selectByUserId(checkinOnly.getUserId()).getAvailablePoints());
        assertEquals(10, accountMapper.selectByUserId(fullAttendance.getUserId()).getAvailablePoints());
        assertEquals(9, accountMapper.selectByUserId(absent.getUserId()).getAvailablePoints());
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(), activityMapper.selectById(activity.getId()).getStatus());
    }

    @Test
    void settleActivityWithSameRunKeyShouldReturnExistingRunAndKeepLedgerOnce() {
        SettlementFixture fixture = insertSettlementFixture();
        Long firstRunId = settlementService.settleActivity(buildRunReq(fixture.activity.getId(), "M7-3-SAME-RUN"));

        Long secondRunId = settlementService.settleActivity(buildRunReq(fixture.activity.getId(), "M7-3-SAME-RUN"));

        assertEquals(firstRunId, secondRunId);
        assertEquals(1L, settlementRunMapper.selectList().size());
        assertEquals(4L, countSettlementTransactions(fixture.activity.getId()));
        assertFixtureAccounts(fixture);
    }

    @Test
    void settleActivityAgainWithDifferentRunKeyShouldReuseLedgerIdempotency() {
        SettlementFixture fixture = insertSettlementFixture();
        Long firstRunId = settlementService.settleActivity(buildRunReq(fixture.activity.getId(), "M7-3-RERUN-1"));

        Long secondRunId = settlementService.settleActivity(buildRunReq(fixture.activity.getId(), "M7-3-RERUN-2"));

        assertTrue(secondRunId > firstRunId);
        assertEquals(2L, settlementRunMapper.selectList().size());
        assertEquals(4L, countSettlementTransactions(fixture.activity.getId()));
        assertFixtureAccounts(fixture);
    }

    @Test
    void settleActivityConcurrentlyShouldNotDuplicateLedgerTransactions() throws Exception {
        SettlementFixture fixture = insertSettlementFixture();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Callable<Long> firstTask = buildConcurrentSettlementTask(fixture.activity.getId(), "M7-3-CONCURRENT-1", ready, start);
        Callable<Long> secondTask = buildConcurrentSettlementTask(fixture.activity.getId(), "M7-3-CONCURRENT-2", ready, start);

        Future<Long> first = executorService.submit(firstTask);
        Future<Long> second = executorService.submit(secondTask);
        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        Long firstRunId = first.get(10, TimeUnit.SECONDS);
        Long secondRunId = second.get(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        assertTrue(firstRunId > 0);
        assertTrue(secondRunId > 0);
        assertEquals(2L, settlementRunMapper.selectList().size());
        assertEquals(4L, countSettlementTransactions(fixture.activity.getId()));
        assertFixtureAccounts(fixture);
    }

    @Test
    void settleActivityShouldDeductMonthlyAbsenceOnceWhenThresholdReached() {
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub();
        Long userId = 7201L;
        insertExistingAccount(userId, 50);
        ClubPointActivityDO first = insertEndedActivity(club, BASE_TIME.plusDays(1),
                ClubPointActivityStatusEnum.ENDED.getStatus());
        ClubPointActivityDO second = insertEndedActivity(club, BASE_TIME.plusDays(2),
                ClubPointActivityStatusEnum.ENDED.getStatus());
        insertConfigVersion(first, ruleVersion);
        insertConfigVersion(second, ruleVersion);
        insertRegistration(first, userId, "Monthly Absent", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(second, userId, "Monthly Absent", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        String monthlyKey = ClubPointActivitySettlementItemTypeEnum.ABSENCE_MONTHLY
                .buildIdempotencyKey(null, userId, 202607);

        settlementService.settleActivity(buildRunReq(first.getId(), "M7-4-MONTHLY-1"));
        settlementService.settleActivity(buildRunReq(second.getId(), "M7-4-MONTHLY-2"));
        assertNull(transactionMapper.selectByIdempotencyKey(monthlyKey));

        ClubPointActivityDO third = insertEndedActivity(club, BASE_TIME.plusDays(3),
                ClubPointActivityStatusEnum.ENDED.getStatus());
        insertConfigVersion(third, ruleVersion);
        insertRegistration(third, userId, "Monthly Absent", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        settlementService.settleActivity(buildRunReq(third.getId(), "M7-4-MONTHLY-3"));

        ClubPointTransactionDO monthlyTransaction = transactionMapper.selectByIdempotencyKey(monthlyKey);
        assertNotNull(monthlyTransaction);
        assertEquals(20, monthlyTransaction.getPoints());
        assertEquals(ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT.getCode(),
                monthlyTransaction.getRuleItemCodeSnapshot());
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), monthlyTransaction.getDirection());
        assertEquals(ClubPointCategoryEnum.DEDUCTION.getCategory(), monthlyTransaction.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(),
                monthlyTransaction.getSourceType());
        assertNull(monthlyTransaction.getIssuingClubId());
        assertNull(monthlyTransaction.getActivityId());
        assertTrue(monthlyTransaction.getSnapshotJson().contains("\"businessMonth\":202607"));
        assertTrue(monthlyTransaction.getSnapshotJson().contains("\"absenceCount\":3"));
        assertTrue(monthlyTransaction.getSnapshotJson().contains("\"threshold\":3"));
        assertEquals(21, accountMapper.selectByUserId(userId).getAvailablePoints());

        ClubPointActivityDO fourth = insertEndedActivity(club, BASE_TIME.plusDays(4),
                ClubPointActivityStatusEnum.ENDED.getStatus());
        insertConfigVersion(fourth, ruleVersion);
        insertRegistration(fourth, userId, "Monthly Absent", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        settlementService.settleActivity(buildRunReq(fourth.getId(), "M7-4-MONTHLY-4"));

        assertEquals(monthlyTransaction.getId(), transactionMapper.selectByIdempotencyKey(monthlyKey).getId());
        assertEquals(18, accountMapper.selectByUserId(userId).getAvailablePoints());
    }

    private ClubPointRuleVersionDO seedSettlementRules() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("M7-RULE-001")
                .setName("M7 rules")
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
                .setCode("CLUB-M7-2001")
                .setName("M7 Settlement Club")
                .setStatus(ClubPointClubStatusEnum.ENABLED.getStatus())
                .setDescription("desc")
                .setContactText("contact")
                .setSort(10)
                .setRemark("remark");
        clubMapper.insert(club);
        return club;
    }

    private ClubPointActivityDO insertEndedActivity(ClubPointClubDO club) {
        return insertEndedActivity(club, BASE_TIME.plusDays(3), ClubPointActivityStatusEnum.ENDED.getStatus());
    }

    private ClubPointActivityDO insertEndedActivity(ClubPointClubDO club, LocalDateTime startTime,
                                                   Integer status) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle("M7 Settlement Activity")
                .setLocation("Gym")
                .setDescription("Settlement activity desc")
                .setLevel(2)
                .setStatus(status)
                .setStartTime(startTime)
                .setEndTime(startTime.plusHours(2))
                .setRegistrationDeadline(startTime.minusDays(1))
                .setCancelDeadlineTime(startTime.minusHours(12))
                .setCheckinStartTime(startTime.minusMinutes(30))
                .setCheckinEndTime(startTime.plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(startTime.plusHours(1))
                .setCheckoutEndTime(startTime.plusHours(3))
                .setCreatorUserId(2000L)
                .setSnapshotJson("{}")
                .setRemark("activity remark");
        activityMapper.insert(activity);
        return activity;
    }

    private ClubPointActivityPointConfigVersionDO insertConfigVersion(ClubPointActivityDO activity,
                                                                      ClubPointRuleVersionDO ruleVersion) {
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
        return config;
    }

    private ClubPointActivityRegistrationDO insertRegistration(ClubPointActivityDO activity, Long userId,
                                                               String userName, boolean noAbsenceDeduct,
                                                               boolean specialAbsence, Integer status) {
        ClubPointActivityRegistrationDO registration = new ClubPointActivityRegistrationDO()
                .setActivityId(activity.getId())
                .setClubId(activity.getClubId())
                .setUserId(userId)
                .setStatus(status)
                .setRegisterTime(BASE_TIME.plusDays(1))
                .setNoAbsenceDeduct(noAbsenceDeduct)
                .setSpecialAbsenceFlag(specialAbsence)
                .setSpecialAbsenceReason(specialAbsence ? "official leave" : null)
                .setSpecialAbsenceTime(specialAbsence ? BASE_TIME.plusDays(2) : null)
                .setSpecialAbsenceOperatorId(specialAbsence ? 9001L : null)
                .setUserNameSnapshot(userName)
                .setDeptIdSnapshot(61L)
                .setDeptNameSnapshot("Operations")
                .setMobileSnapshot("1390000" + userId)
                .setClubNameSnapshot(activity.getClubNameSnapshot())
                .setActivityTitleSnapshot(activity.getTitle())
                .setActivityStartTimeSnapshot(activity.getStartTime())
                .setActivityEndTimeSnapshot(activity.getEndTime())
                .setActiveUniqueKey(ClubPointRegistrationStatusEnum.REGISTERED.getStatus().equals(status)
                        ? activity.getId() + ":" + userId : null);
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

    private void insertExistingAccount(Long userId, Integer availablePoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(availablePoints)
                .setTotalNegativePoints(0)
                .setNetPoints(availablePoints)
                .setFrozenPoints(0)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(availablePoints)
                .setVersion(1));
    }

    private SettlementFixture insertSettlementFixture() {
        ClubPointRuleVersionDO ruleVersion = seedSettlementRules();
        ClubPointClubDO club = insertClub();
        ClubPointActivityDO activity = insertEndedActivity(club);
        insertConfigVersion(activity, ruleVersion);
        ClubPointActivityRegistrationDO checkinOnly = insertRegistration(activity, 7101L, "Checkin Only", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        ClubPointActivityRegistrationDO fullAttendance = insertRegistration(activity, 7102L, "Full Attendance", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        ClubPointActivityRegistrationDO absent = insertRegistration(activity, 7103L, "Absent", false, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(activity, 7104L, "Special Absence", false, true,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(activity, 7105L, "No Deduct", true, false,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(activity, 7106L, "Canceled", true, false,
                ClubPointRegistrationStatusEnum.CANCELED.getStatus());
        insertAttendance(checkinOnly, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(3));
        insertAttendance(fullAttendance, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(3));
        insertAttendance(fullAttendance, ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType(), BASE_TIME.plusDays(3).plusHours(2));
        insertExistingAccount(absent.getUserId(), 12);
        return new SettlementFixture(activity, checkinOnly, fullAttendance, absent);
    }

    private static ClubPointActivitySettlementRunReqBO buildRunReq(Long activityId, String runKey) {
        return new ClubPointActivitySettlementRunReqBO()
                .setActivityId(activityId)
                .setRunKey(runKey)
                .setTriggerSource(ClubPointActivitySettlementTriggerSourceEnum.ADMIN_MANUAL.getSource())
                .setOperatorUserId(9001L)
                .setSettlementTime(BASE_TIME.plusDays(4));
    }

    private Callable<Long> buildConcurrentSettlementTask(Long activityId, String runKey,
                                                         CountDownLatch ready, CountDownLatch start) {
        return () -> {
            ready.countDown();
            assertTrue(start.await(5, TimeUnit.SECONDS));
            return settlementService.settleActivity(buildRunReq(activityId, runKey));
        };
    }

    private long countSettlementTransactions(Long activityId) {
        return transactionMapper.selectList().stream()
                .filter(transaction -> activityId.equals(transaction.getActivityId()))
                .filter(transaction -> ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType()
                        .equals(transaction.getSourceType()))
                .count();
    }

    private void assertFixtureAccounts(SettlementFixture fixture) {
        assertEquals(8, accountMapper.selectByUserId(fixture.checkinOnly.getUserId()).getAvailablePoints());
        assertEquals(10, accountMapper.selectByUserId(fixture.fullAttendance.getUserId()).getAvailablePoints());
        assertEquals(9, accountMapper.selectByUserId(fixture.absent.getUserId()).getAvailablePoints());
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(),
                activityMapper.selectById(fixture.activity.getId()).getStatus());
    }

    private static final class SettlementFixture {

        private final ClubPointActivityDO activity;
        private final ClubPointActivityRegistrationDO checkinOnly;
        private final ClubPointActivityRegistrationDO fullAttendance;
        private final ClubPointActivityRegistrationDO absent;

        private SettlementFixture(ClubPointActivityDO activity,
                                  ClubPointActivityRegistrationDO checkinOnly,
                                  ClubPointActivityRegistrationDO fullAttendance,
                                  ClubPointActivityRegistrationDO absent) {
            this.activity = activity;
            this.checkinOnly = checkinOnly;
            this.fullAttendance = fullAttendance;
            this.absent = absent;
        }

    }

}

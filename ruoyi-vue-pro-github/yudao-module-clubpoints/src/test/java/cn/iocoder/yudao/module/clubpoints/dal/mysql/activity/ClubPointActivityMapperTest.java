package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityPointConfigVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceCorrectionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointActivityMapperTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 10, 0);

    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubPointActivityPointConfigVersionMapper configVersionMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointAttendanceCorrectionMapper attendanceCorrectionMapper;

    @Test
    void activityMappersShouldPersistFactsAndRuleSnapshots() {
        ClubPointActivityDO activity = buildActivity();
        activityMapper.insert(activity);

        ClubPointActivityDO savedActivity = activityMapper.selectById(activity.getId());
        assertNotNull(savedActivity);
        assertEquals(101L, savedActivity.getClubId());
        assertEquals("CLUB-M6-001", savedActivity.getClubCodeSnapshot());
        assertEquals("Running Club", savedActivity.getClubNameSnapshot());
        assertEquals("Morning Run", savedActivity.getTitle());
        assertEquals("Park", savedActivity.getLocation());
        assertEquals("Warm up and run", savedActivity.getDescription());
        assertEquals(1001L, savedActivity.getCoverFileId());
        assertEquals(2, savedActivity.getLevel());
        assertEquals(1, savedActivity.getStatus());
        assertEquals(BASE_TIME.plusDays(3), savedActivity.getStartTime());
        assertEquals(BASE_TIME.plusDays(3).plusHours(2), savedActivity.getEndTime());
        assertEquals(BASE_TIME.plusDays(2), savedActivity.getRegistrationDeadline());
        assertEquals(BASE_TIME.plusDays(2).plusHours(12), savedActivity.getCancelDeadlineTime());
        assertEquals(BASE_TIME.plusDays(3).minusMinutes(30), savedActivity.getCheckinStartTime());
        assertEquals(BASE_TIME.plusDays(3).plusMinutes(30), savedActivity.getCheckinEndTime());
        assertEquals(1, savedActivity.getCheckoutMode());
        assertEquals(BASE_TIME.plusDays(3).plusHours(1), savedActivity.getCheckoutStartTime());
        assertEquals(BASE_TIME.plusDays(3).plusHours(3), savedActivity.getCheckoutEndTime());
        assertEquals(3001L, savedActivity.getCurrentConfigVersionId());
        assertEquals(9001L, savedActivity.getCreatorUserId());
        assertEquals(BASE_TIME.plusHours(1), savedActivity.getSubmitTime());
        assertEquals(BASE_TIME.plusHours(2), savedActivity.getPublishTime());
        assertEquals(BASE_TIME.plusHours(3), savedActivity.getCancelTime());
        assertEquals("weather", savedActivity.getCancelReason());
        assertEquals("{\"title\":\"Morning Run\"}", savedActivity.getSnapshotJson());
        assertEquals("activity remark", savedActivity.getRemark());

        ClubPointActivityReviewRecordDO reviewRecord = buildReviewRecord(savedActivity.getId());
        reviewRecordMapper.insert(reviewRecord);
        ClubPointActivityReviewRecordDO savedReviewRecord = reviewRecordMapper.selectById(reviewRecord.getId());
        assertNotNull(savedReviewRecord);
        assertEquals(savedActivity.getId(), savedReviewRecord.getActivityId());
        assertEquals(8001L, savedReviewRecord.getReviewerUserId());
        assertEquals(1, savedReviewRecord.getResult());
        assertEquals("approved", savedReviewRecord.getReason());
        assertEquals(BASE_TIME.plusHours(4), savedReviewRecord.getReviewTime());
        assertEquals("{\"status\":2}", savedReviewRecord.getActivitySnapshotJson());
        assertEquals(7001L, savedReviewRecord.getAuditLogId());

        ClubPointActivityPointConfigVersionDO configVersion = buildConfigVersion(savedActivity.getId());
        configVersionMapper.insert(configVersion);
        ClubPointActivityPointConfigVersionDO savedConfigVersion = configVersionMapper.selectById(configVersion.getId());
        assertNotNull(savedConfigVersion);
        assertEquals(savedActivity.getId(), savedConfigVersion.getActivityId());
        assertEquals(1, savedConfigVersion.getVersionNo());
        assertEquals(2, savedConfigVersion.getLevel());
        assertEquals(10, savedConfigVersion.getBasePoints());
        assertEquals(5, savedConfigVersion.getFullExtraPoints());
        assertEquals(6001L, savedConfigVersion.getRuleVersionId());
        assertEquals(6101L, savedConfigVersion.getBaseRuleItemId());
        assertEquals(6102L, savedConfigVersion.getFullRuleItemId());
        assertEquals(BASE_TIME.plusHours(5), savedConfigVersion.getEffectiveTime());
        assertEquals("initial publish", savedConfigVersion.getCreatedReason());
        assertTrue(savedConfigVersion.getActive());
        assertEquals("{\"base\":10,\"full\":5}", savedConfigVersion.getRuleSnapshotJson());

        ClubPointActivityRegistrationDO registration = buildRegistration(savedActivity);
        registrationMapper.insert(registration);
        ClubPointActivityRegistrationDO savedRegistration = registrationMapper.selectById(registration.getId());
        assertNotNull(savedRegistration);
        assertEquals(savedActivity.getId(), savedRegistration.getActivityId());
        assertEquals(savedActivity.getClubId(), savedRegistration.getClubId());
        assertEquals(5001L, savedRegistration.getUserId());
        assertEquals(1, savedRegistration.getStatus());
        assertEquals(BASE_TIME.plusHours(6), savedRegistration.getRegisterTime());
        assertEquals(BASE_TIME.plusHours(7), savedRegistration.getCancelTime());
        assertEquals(1, savedRegistration.getCancelReasonType());
        assertEquals("personal", savedRegistration.getCancelReason());
        assertEquals(5001L, savedRegistration.getCancelOperatorUserId());
        assertTrue(savedRegistration.getNoAbsenceDeduct());
        assertTrue(savedRegistration.getSpecialAbsenceFlag());
        assertEquals("sick leave", savedRegistration.getSpecialAbsenceReason());
        assertEquals(BASE_TIME.plusHours(8), savedRegistration.getSpecialAbsenceTime());
        assertEquals(8001L, savedRegistration.getSpecialAbsenceOperatorId());
        assertEquals("User 5001", savedRegistration.getUserNameSnapshot());
        assertEquals(51L, savedRegistration.getDeptIdSnapshot());
        assertEquals("Ops", savedRegistration.getDeptNameSnapshot());
        assertEquals("13800005001", savedRegistration.getMobileSnapshot());
        assertEquals(savedActivity.getClubNameSnapshot(), savedRegistration.getClubNameSnapshot());
        assertEquals(savedActivity.getTitle(), savedRegistration.getActivityTitleSnapshot());
        assertEquals(savedActivity.getStartTime(), savedRegistration.getActivityStartTimeSnapshot());
        assertEquals(savedActivity.getEndTime(), savedRegistration.getActivityEndTimeSnapshot());
        assertEquals(savedActivity.getId() + ":5001", savedRegistration.getActiveUniqueKey());

        ClubPointAttendanceRecordDO attendanceRecord = buildAttendanceRecord(savedRegistration);
        attendanceRecordMapper.insert(attendanceRecord);
        ClubPointAttendanceRecordDO savedAttendanceRecord = attendanceRecordMapper.selectById(attendanceRecord.getId());
        assertNotNull(savedAttendanceRecord);
        assertEquals(savedRegistration.getId(), savedAttendanceRecord.getRegistrationId());
        assertEquals(savedActivity.getId(), savedAttendanceRecord.getActivityId());
        assertEquals(5001L, savedAttendanceRecord.getUserId());
        assertEquals(1, savedAttendanceRecord.getTargetType());
        assertEquals(BASE_TIME.plusHours(9), savedAttendanceRecord.getRecordTime());
        assertEquals(2, savedAttendanceRecord.getSourceType());
        assertEquals(8001L, savedAttendanceRecord.getOperatorUserId());
        assertEquals("manual checkin", savedAttendanceRecord.getReason());
        assertEquals("127.0.0.1", savedAttendanceRecord.getClientIp());
        assertEquals("attendance remark", savedAttendanceRecord.getRemark());

        ClubPointAttendanceCorrectionDO correction = buildAttendanceCorrection(savedAttendanceRecord);
        attendanceCorrectionMapper.insert(correction);
        ClubPointAttendanceCorrectionDO savedCorrection = attendanceCorrectionMapper.selectById(correction.getId());
        assertNotNull(savedCorrection);
        assertEquals(savedAttendanceRecord.getId(), savedCorrection.getAttendanceRecordId());
        assertEquals(savedRegistration.getId(), savedCorrection.getRegistrationId());
        assertEquals(savedActivity.getId(), savedCorrection.getActivityId());
        assertEquals(5001L, savedCorrection.getUserId());
        assertEquals(1, savedCorrection.getTargetType());
        assertEquals(2, savedCorrection.getCorrectionType());
        assertEquals(BASE_TIME.plusHours(9), savedCorrection.getBeforeRecordTime());
        assertEquals(BASE_TIME.plusHours(10), savedCorrection.getAfterRecordTime());
        assertEquals("time correction", savedCorrection.getReason());
        assertEquals(8001L, savedCorrection.getOperatorUserId());
        assertEquals(7002L, savedCorrection.getAuditLogId());
    }

    private static ClubPointActivityDO buildActivity() {
        return new ClubPointActivityDO()
                .setClubId(101L)
                .setClubCodeSnapshot("CLUB-M6-001")
                .setClubNameSnapshot("Running Club")
                .setTitle("Morning Run")
                .setLocation("Park")
                .setDescription("Warm up and run")
                .setCoverFileId(1001L)
                .setLevel(2)
                .setStatus(1)
                .setStartTime(BASE_TIME.plusDays(3))
                .setEndTime(BASE_TIME.plusDays(3).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(2))
                .setCancelDeadlineTime(BASE_TIME.plusDays(2).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(3).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(3).plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(BASE_TIME.plusDays(3).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(3).plusHours(3))
                .setCurrentConfigVersionId(3001L)
                .setCreatorUserId(9001L)
                .setSubmitTime(BASE_TIME.plusHours(1))
                .setPublishTime(BASE_TIME.plusHours(2))
                .setCancelTime(BASE_TIME.plusHours(3))
                .setCancelReason("weather")
                .setSnapshotJson("{\"title\":\"Morning Run\"}")
                .setRemark("activity remark");
    }

    private static ClubPointActivityReviewRecordDO buildReviewRecord(Long activityId) {
        return new ClubPointActivityReviewRecordDO()
                .setActivityId(activityId)
                .setReviewerUserId(8001L)
                .setResult(1)
                .setReason("approved")
                .setReviewTime(BASE_TIME.plusHours(4))
                .setActivitySnapshotJson("{\"status\":2}")
                .setAuditLogId(7001L);
    }

    private static ClubPointActivityPointConfigVersionDO buildConfigVersion(Long activityId) {
        return new ClubPointActivityPointConfigVersionDO()
                .setActivityId(activityId)
                .setVersionNo(1)
                .setLevel(2)
                .setBasePoints(10)
                .setFullExtraPoints(5)
                .setRuleVersionId(6001L)
                .setBaseRuleItemId(6101L)
                .setFullRuleItemId(6102L)
                .setEffectiveTime(BASE_TIME.plusHours(5))
                .setCreatedReason("initial publish")
                .setActive(true)
                .setRuleSnapshotJson("{\"base\":10,\"full\":5}");
    }

    private static ClubPointActivityRegistrationDO buildRegistration(ClubPointActivityDO activity) {
        return new ClubPointActivityRegistrationDO()
                .setActivityId(activity.getId())
                .setClubId(activity.getClubId())
                .setUserId(5001L)
                .setStatus(1)
                .setRegisterTime(BASE_TIME.plusHours(6))
                .setCancelTime(BASE_TIME.plusHours(7))
                .setCancelReasonType(1)
                .setCancelReason("personal")
                .setCancelOperatorUserId(5001L)
                .setNoAbsenceDeduct(true)
                .setSpecialAbsenceFlag(true)
                .setSpecialAbsenceReason("sick leave")
                .setSpecialAbsenceTime(BASE_TIME.plusHours(8))
                .setSpecialAbsenceOperatorId(8001L)
                .setUserNameSnapshot("User 5001")
                .setDeptIdSnapshot(51L)
                .setDeptNameSnapshot("Ops")
                .setMobileSnapshot("13800005001")
                .setClubNameSnapshot(activity.getClubNameSnapshot())
                .setActivityTitleSnapshot(activity.getTitle())
                .setActivityStartTimeSnapshot(activity.getStartTime())
                .setActivityEndTimeSnapshot(activity.getEndTime())
                .setActiveUniqueKey(activity.getId() + ":5001");
    }

    private static ClubPointAttendanceRecordDO buildAttendanceRecord(ClubPointActivityRegistrationDO registration) {
        return new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(1)
                .setRecordTime(BASE_TIME.plusHours(9))
                .setSourceType(2)
                .setOperatorUserId(8001L)
                .setReason("manual checkin")
                .setClientIp("127.0.0.1")
                .setRemark("attendance remark");
    }

    private static ClubPointAttendanceCorrectionDO buildAttendanceCorrection(
            ClubPointAttendanceRecordDO attendanceRecord) {
        return new ClubPointAttendanceCorrectionDO()
                .setAttendanceRecordId(attendanceRecord.getId())
                .setRegistrationId(attendanceRecord.getRegistrationId())
                .setActivityId(attendanceRecord.getActivityId())
                .setUserId(attendanceRecord.getUserId())
                .setTargetType(attendanceRecord.getTargetType())
                .setCorrectionType(2)
                .setBeforeRecordTime(attendanceRecord.getRecordTime())
                .setAfterRecordTime(BASE_TIME.plusHours(10))
                .setReason("time correction")
                .setOperatorUserId(8001L)
                .setAuditLogId(7002L);
    }

}

package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceCorrectionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceCorrectionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceCorrectionTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceCorrectReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSupplementReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointSpecialAbsenceReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ATTENDANCE_CORRECT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ATTENDANCE_SUPPLEMENT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.SPECIAL_ABSENCE_MARK;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointAttendanceServiceImpl.class, ClubScopeServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointAttendanceCorrectionServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Resource
    private ClubPointAttendanceService attendanceService;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointAttendanceCorrectionMapper correctionMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;

    @Test
    void supplementAttendanceShouldPersistEffectiveRecordCorrectionAndAudit() {
        ClubPointClubDO club = insertClub("CLUB-M6-6001", "Supplement Club");
        insertMember(club, 6001L, "Employee 6001");
        ClubPointActivityDO activity = insertActivity(club);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 6001L);

        Long correctionId = attendanceService.supplementAttendance(buildSupplementReq(registration.getId(),
                ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(4)));

        ClubPointAttendanceCorrectionDO correction = correctionMapper.selectById(correctionId);
        assertEquals(registration.getId(), correction.getRegistrationId());
        assertEquals(activity.getId(), correction.getActivityId());
        assertEquals(6001L, correction.getUserId());
        assertEquals(ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), correction.getTargetType());
        assertEquals(ClubPointAttendanceCorrectionTypeEnum.SUPPLEMENT.getCorrectionType(),
                correction.getCorrectionType());
        assertEquals(BASE_TIME.plusDays(4), correction.getAfterRecordTime());
        assertNotNull(correction.getAttendanceRecordId());
        assertNotNull(correction.getAuditLogId());

        ClubPointAttendanceRecordDO record = attendanceRecordMapper.selectById(correction.getAttendanceRecordId());
        assertEquals(ClubPointAttendanceSourceTypeEnum.SUPPLEMENT.getSourceType(), record.getSourceType());
        assertEquals(9001L, record.getOperatorUserId());
        assertEquals("补录签到", record.getReason());

        ClubAuditLogDO auditLog = auditLogMapper.selectById(correction.getAuditLogId());
        assertEquals(ATTENDANCE_SUPPLEMENT, auditLog.getActionType());
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"registrationId\":" + registration.getId()));
    }

    @Test
    void correctAttendanceShouldKeepRecordAndWriteCorrectionHistory() {
        ClubPointClubDO club = insertClub("CLUB-M6-6002", "Correct Club");
        insertMember(club, 6002L, "Employee 6002");
        ClubPointActivityDO activity = insertActivity(club);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 6002L);
        ClubPointAttendanceRecordDO record = insertAttendanceRecord(registration,
                ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(4).minusMinutes(5));

        Long correctionId = attendanceService.correctAttendance(buildCorrectReq(record.getId(),
                BASE_TIME.plusDays(4).minusMinutes(1)));

        ClubPointAttendanceRecordDO correctedRecord = attendanceRecordMapper.selectById(record.getId());
        assertEquals(record.getId(), correctedRecord.getId());
        assertEquals(BASE_TIME.plusDays(4).minusMinutes(1), correctedRecord.getRecordTime());
        assertEquals(ClubPointAttendanceSourceTypeEnum.CORRECTION.getSourceType(), correctedRecord.getSourceType());

        ClubPointAttendanceCorrectionDO correction = correctionMapper.selectById(correctionId);
        assertEquals(record.getId(), correction.getAttendanceRecordId());
        assertEquals(BASE_TIME.plusDays(4).minusMinutes(5), correction.getBeforeRecordTime());
        assertEquals(BASE_TIME.plusDays(4).minusMinutes(1), correction.getAfterRecordTime());
        assertEquals(ClubPointAttendanceCorrectionTypeEnum.CORRECTION.getCorrectionType(),
                correction.getCorrectionType());
        assertEquals(ATTENDANCE_CORRECT, auditLogMapper.selectById(correction.getAuditLogId()).getActionType());
    }

    @Test
    void markSpecialAbsenceShouldUpdateRegistrationOnlyAndWriteAudit() {
        ClubPointClubDO club = insertClub("CLUB-M6-6003", "Special Absence Club");
        insertMember(club, 6003L, "Employee 6003");
        ClubPointActivityDO activity = insertActivity(club);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 6003L);

        attendanceService.markSpecialAbsence(buildSpecialAbsenceReq(registration.getId()));

        ClubPointActivityRegistrationDO updated = registrationMapper.selectById(registration.getId());
        assertTrue(updated.getSpecialAbsenceFlag());
        assertTrue(updated.getNoAbsenceDeduct());
        assertEquals("特殊请假", updated.getSpecialAbsenceReason());
        assertEquals(BASE_TIME.plusDays(2), updated.getSpecialAbsenceTime());
        assertEquals(9003L, updated.getSpecialAbsenceOperatorId());
        assertEquals(0L, correctionMapper.selectCount());
        assertEquals(1L, auditLogMapper.selectCount());
        assertEquals(SPECIAL_ABSENCE_MARK, auditLogMapper.selectList().get(0).getActionType());
    }

    @Test
    void supplementShouldRejectNonGlobalDuplicateAndRollbackWhenAuditFails() {
        ClubPointClubDO club = insertClub("CLUB-M6-6004", "Supplement Guard Club");
        insertMember(club, 6004L, "Employee 6004");
        ClubPointActivityDO activity = insertActivity(club);
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 6004L);
        insertAttendanceRecord(registration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                BASE_TIME.plusDays(4).minusMinutes(5));

        assertServiceException(() -> attendanceService.supplementAttendance(buildSupplementReq(registration.getId(),
                ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType(), BASE_TIME.plusDays(4).plusHours(1))
                .setOperatorGlobalScope(false)), CLUB_SCOPE_DENIED);
        assertServiceException(() -> attendanceService.supplementAttendance(buildSupplementReq(registration.getId(),
                ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), BASE_TIME.plusDays(4))),
                CLUB_ATTENDANCE_ALREADY_EXISTS);

        ClubPointActivityRegistrationDO rollbackRegistration = insertRegistration(activity, 6064L);
        assertServiceException(() -> attendanceService.supplementAttendance(buildSupplementReq(
                rollbackRegistration.getId(), ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                BASE_TIME.plusDays(4)).setOperatorNameSnapshot(null)), CLUB_AUDIT_WRITE_FAILED);
        assertEquals(1L, attendanceRecordMapper.selectCount());
        assertEquals(0L, correctionMapper.selectCount());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void correctAttendanceShouldRejectMissingRecord() {
        assertServiceException(() -> attendanceService.correctAttendance(buildCorrectReq(99999L,
                BASE_TIME.plusDays(4))), CLUB_ATTENDANCE_NOT_FOUND);
    }

    private ClubPointClubDO insertClub(String code, String name) {
        ClubPointClubDO club = new ClubPointClubDO()
                .setCode(code)
                .setName(name)
                .setStatus(ClubPointClubStatusEnum.ENABLED.getStatus())
                .setDescription("desc")
                .setContactText("contact")
                .setSort(10)
                .setRemark("remark");
        clubMapper.insert(club);
        return club;
    }

    private void insertMember(ClubPointClubDO club, Long userId, String userName) {
        memberMapper.insert(new ClubMemberDO()
                .setClubId(club.getId())
                .setUserId(userId)
                .setDeptIdSnapshot(61L)
                .setUserNameSnapshot(userName)
                .setDeptNameSnapshot("Operations")
                .setMobileSnapshot("1390000" + userId)
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setStatus(ClubPointMemberStatusEnum.ACTIVE.getStatus())
                .setJoinTime(BASE_TIME.minusDays(1))
                .setOperatorUserId(900L)
                .setActiveUniqueKey(club.getId() + ":" + userId));
    }

    private ClubPointActivityDO insertActivity(ClubPointClubDO club) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle("Correction Activity")
                .setLocation("Gym")
                .setDescription("Correction activity desc")
                .setLevel(2)
                .setStatus(ClubPointActivityStatusEnum.PUBLISHED.getStatus())
                .setStartTime(BASE_TIME.plusDays(4))
                .setEndTime(BASE_TIME.plusDays(4).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(3))
                .setCancelDeadlineTime(BASE_TIME.plusDays(3).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(4).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(4).plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(BASE_TIME.plusDays(4).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(4).plusHours(3))
                .setCreatorUserId(2000L)
                .setSnapshotJson("{}")
                .setRemark("activity remark");
        activityMapper.insert(activity);
        return activity;
    }

    private ClubPointActivityRegistrationDO insertRegistration(ClubPointActivityDO activity, Long userId) {
        ClubPointActivityRegistrationDO registration = new ClubPointActivityRegistrationDO()
                .setActivityId(activity.getId())
                .setClubId(activity.getClubId())
                .setUserId(userId)
                .setStatus(ClubPointRegistrationStatusEnum.REGISTERED.getStatus())
                .setRegisterTime(BASE_TIME.minusDays(1))
                .setNoAbsenceDeduct(false)
                .setSpecialAbsenceFlag(false)
                .setUserNameSnapshot("Employee " + userId)
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

    private ClubPointAttendanceRecordDO insertAttendanceRecord(ClubPointActivityRegistrationDO registration,
                                                               Integer targetType,
                                                               LocalDateTime recordTime) {
        ClubPointAttendanceRecordDO record = new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType)
                .setRecordTime(recordTime)
                .setSourceType(ClubPointAttendanceSourceTypeEnum.SELF.getSourceType())
                .setOperatorUserId(registration.getUserId())
                .setClientIp("127.0.0.1")
                .setRemark("self attendance");
        attendanceRecordMapper.insert(record);
        return record;
    }

    private static ClubPointAttendanceSupplementReqBO buildSupplementReq(Long registrationId, Integer targetType,
                                                                         LocalDateTime recordTime) {
        return new ClubPointAttendanceSupplementReqBO()
                .setRegistrationId(registrationId)
                .setTargetType(targetType)
                .setRecordTime(recordTime)
                .setReason("补录签到")
                .setOperatorUserId(9001L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("管理员")
                .setOperatorGlobalScope(true)
                .setOperationTime(BASE_TIME.plusDays(2))
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static ClubPointAttendanceCorrectReqBO buildCorrectReq(Long attendanceRecordId,
                                                                   LocalDateTime newRecordTime) {
        return new ClubPointAttendanceCorrectReqBO()
                .setAttendanceRecordId(attendanceRecordId)
                .setNewRecordTime(newRecordTime)
                .setReason("修正签到")
                .setOperatorUserId(9002L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("管理员")
                .setOperatorGlobalScope(true)
                .setOperationTime(BASE_TIME.plusDays(2))
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

    private static ClubPointSpecialAbsenceReqBO buildSpecialAbsenceReq(Long registrationId) {
        return new ClubPointSpecialAbsenceReqBO()
                .setRegistrationId(registrationId)
                .setReason("特殊请假")
                .setOperatorUserId(9003L)
                .setOperatorNameSnapshot("Admin")
                .setOperatorRoleSnapshot("管理员")
                .setOperatorGlobalScope(true)
                .setOperationTime(BASE_TIME.plusDays(2))
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit");
    }

}

package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_ALREADY_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_CHECKIN_REQUIRED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ATTENDANCE_WINDOW_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({ClubPointAttendanceServiceImpl.class, ClubScopeServiceImpl.class, ClubAuditServiceImpl.class})
class ClubPointAttendanceServiceImplTest extends BaseDbUnitTest {

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
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;

    @Test
    void checkInShouldPersistSelfAttendanceWithinWindowAndNotGenerateLedgerTransaction() {
        ClubPointClubDO club = insertClub("CLUB-M6-5001", "Attendance Club");
        insertMember(club, 5001L, "Employee 5001");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus());
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 5001L);

        Long recordId = attendanceService.checkIn(buildSelfReq(registration.getId(), 5001L,
                BASE_TIME.plusDays(4).minusMinutes(10)));

        ClubPointAttendanceRecordDO record = attendanceRecordMapper.selectById(recordId);
        assertEquals(registration.getId(), record.getRegistrationId());
        assertEquals(activity.getId(), record.getActivityId());
        assertEquals(5001L, record.getUserId());
        assertEquals(ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(), record.getTargetType());
        assertEquals(ClubPointAttendanceSourceTypeEnum.SELF.getSourceType(), record.getSourceType());
        assertEquals(5001L, record.getOperatorUserId());
        assertEquals(BASE_TIME.plusDays(4).minusMinutes(10), record.getRecordTime());
        assertEquals("127.0.0.1", record.getClientIp());
        assertEquals("self attendance", record.getRemark());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void checkOutShouldPersistAfterCheckInWithinWindow() {
        ClubPointClubDO club = insertClub("CLUB-M6-5002", "Checkout Club");
        insertMember(club, 5002L, "Employee 5002");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus());
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 5002L);
        attendanceService.checkIn(buildSelfReq(registration.getId(), 5002L,
                BASE_TIME.plusDays(4).minusMinutes(5)));

        Long recordId = attendanceService.checkOut(buildSelfReq(registration.getId(), 5002L,
                BASE_TIME.plusDays(4).plusHours(1).plusMinutes(30)));

        ClubPointAttendanceRecordDO record = attendanceRecordMapper.selectById(recordId);
        assertEquals(registration.getId(), record.getRegistrationId());
        assertEquals(activity.getId(), record.getActivityId());
        assertEquals(5002L, record.getUserId());
        assertEquals(ClubPointAttendanceTargetTypeEnum.CHECK_OUT.getTargetType(), record.getTargetType());
        assertEquals(ClubPointAttendanceSourceTypeEnum.SELF.getSourceType(), record.getSourceType());
        assertEquals(2L, attendanceRecordMapper.selectCount());
    }

    @Test
    void checkInShouldRejectInvalidRegistrationOwnerActivityStatusAndWindow() {
        ClubPointClubDO club = insertClub("CLUB-M6-5003", "Guard Attendance Club");
        insertMember(club, 5003L, "Employee 5003");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus());
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 5003L);

        assertServiceException(() -> attendanceService.checkIn(buildSelfReq(99999L, 5003L,
                BASE_TIME.plusDays(4))), CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        assertServiceException(() -> attendanceService.checkIn(buildSelfReq(registration.getId(), 5999L,
                BASE_TIME.plusDays(4))), CLUB_SCOPE_DENIED);
        assertServiceException(() -> attendanceService.checkIn(buildSelfReq(registration.getId(), 5003L,
                BASE_TIME.plusDays(4).minusHours(2))), CLUB_ATTENDANCE_WINDOW_CLOSED);

        ClubPointActivityDO canceledActivity = insertActivity(club, ClubPointActivityStatusEnum.CANCELED.getStatus());
        ClubPointActivityRegistrationDO canceledRegistration = insertRegistration(canceledActivity, 5003L);
        assertServiceException(() -> attendanceService.checkIn(buildSelfReq(canceledRegistration.getId(), 5003L,
                BASE_TIME.plusDays(4))), CLUB_ACTIVITY_STATUS_INVALID);
    }

    @Test
    void checkInAndCheckOutShouldRejectDuplicateAttendance() {
        ClubPointClubDO club = insertClub("CLUB-M6-5004", "Duplicate Attendance Club");
        insertMember(club, 5004L, "Employee 5004");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus());
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 5004L);
        attendanceService.checkIn(buildSelfReq(registration.getId(), 5004L,
                BASE_TIME.plusDays(4).minusMinutes(5)));
        attendanceService.checkOut(buildSelfReq(registration.getId(), 5004L,
                BASE_TIME.plusDays(4).plusHours(1).plusMinutes(30)));

        assertServiceException(() -> attendanceService.checkIn(buildSelfReq(registration.getId(), 5004L,
                BASE_TIME.plusDays(4))), CLUB_ATTENDANCE_ALREADY_EXISTS);
        assertServiceException(() -> attendanceService.checkOut(buildSelfReq(registration.getId(), 5004L,
                BASE_TIME.plusDays(4).plusHours(2))), CLUB_ATTENDANCE_ALREADY_EXISTS);
    }

    @Test
    void checkOutShouldRejectWhenCheckInMissingOrWindowClosed() {
        ClubPointClubDO club = insertClub("CLUB-M6-5005", "Checkout Guard Club");
        insertMember(club, 5005L, "Employee 5005");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus());
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 5005L);

        assertServiceException(() -> attendanceService.checkOut(buildSelfReq(registration.getId(), 5005L,
                BASE_TIME.plusDays(4).plusHours(1).plusMinutes(30))), CLUB_ATTENDANCE_CHECKIN_REQUIRED);

        attendanceService.checkIn(buildSelfReq(registration.getId(), 5005L,
                BASE_TIME.plusDays(4).minusMinutes(5)));
        assertServiceException(() -> attendanceService.checkOut(buildSelfReq(registration.getId(), 5005L,
                BASE_TIME.plusDays(4).minusMinutes(1))), CLUB_ATTENDANCE_WINDOW_CLOSED);
    }

    @Test
    void attendanceShouldRejectCanceledRegistration() {
        ClubPointClubDO club = insertClub("CLUB-M6-5006", "Canceled Registration Attendance Club");
        insertMember(club, 5006L, "Employee 5006");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus());
        ClubPointActivityRegistrationDO registration = insertRegistration(activity, 5006L)
                .setStatus(ClubPointRegistrationStatusEnum.CANCELED.getStatus())
                .setActiveUniqueKey(null);
        registrationMapper.updateById(registration);

        assertServiceException(() -> attendanceService.checkIn(buildSelfReq(registration.getId(), 5006L,
                BASE_TIME.plusDays(4))), CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
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
                .setDeptIdSnapshot(51L)
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

    private ClubPointActivityDO insertActivity(ClubPointClubDO club, Integer status) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle("Attendance Activity")
                .setLocation("Gym")
                .setDescription("Attendance activity desc")
                .setLevel(2)
                .setStatus(status)
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
        assertNotNull(activity.getId());
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
                .setDeptIdSnapshot(51L)
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

    private static ClubPointAttendanceSelfReqBO buildSelfReq(Long registrationId, Long userId,
                                                              LocalDateTime recordTime) {
        return new ClubPointAttendanceSelfReqBO()
                .setRegistrationId(registrationId)
                .setUserId(userId)
                .setRecordTime(recordTime)
                .setClientIp("127.0.0.1")
                .setRemark("self attendance");
    }

}

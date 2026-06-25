package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationCancelReasonEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_CANCEL_WINDOW_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_DUPLICATED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_REGISTRATION_NOT_FOUND;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_ACTIVITY_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointRegistrationServiceImpl.class, ClubScopeServiceImpl.class})
class ClubPointRegistrationServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Resource
    private ClubPointRegistrationService registrationService;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;

    @Test
    void createRegistrationShouldPersistMemberAndActivitySnapshots() {
        ClubPointClubDO club = insertClub("CLUB-M6-4001", "Registration Club");
        insertMember(club, 3001L, "Employee 3001");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(12));

        Long registrationId = registrationService.createRegistration(buildCreateReq(activity.getId(), 3001L, BASE_TIME));

        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(registrationId);
        assertEquals(activity.getId(), registration.getActivityId());
        assertEquals(club.getId(), registration.getClubId());
        assertEquals(3001L, registration.getUserId());
        assertEquals(ClubPointRegistrationStatusEnum.REGISTERED.getStatus(), registration.getStatus());
        assertEquals(BASE_TIME, registration.getRegisterTime());
        assertFalse(registration.getNoAbsenceDeduct());
        assertFalse(registration.getSpecialAbsenceFlag());
        assertEquals("Employee 3001", registration.getUserNameSnapshot());
        assertEquals(31L, registration.getDeptIdSnapshot());
        assertEquals("Ops", registration.getDeptNameSnapshot());
        assertEquals("13800003001", registration.getMobileSnapshot());
        assertEquals(club.getName(), registration.getClubNameSnapshot());
        assertEquals(activity.getTitle(), registration.getActivityTitleSnapshot());
        assertEquals(activity.getStartTime(), registration.getActivityStartTimeSnapshot());
        assertEquals(activity.getEndTime(), registration.getActivityEndTimeSnapshot());
        assertEquals(activity.getId() + ":3001", registration.getActiveUniqueKey());
    }

    @Test
    void createRegistrationShouldRejectDuplicateActiveRegistration() {
        ClubPointClubDO club = insertClub("CLUB-M6-4002", "Duplicate Registration Club");
        insertMember(club, 3002L, "Employee 3002");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(12));
        registrationService.createRegistration(buildCreateReq(activity.getId(), 3002L, BASE_TIME));

        assertServiceException(() -> registrationService.createRegistration(
                buildCreateReq(activity.getId(), 3002L, BASE_TIME.plusMinutes(1))),
                CLUB_ACTIVITY_REGISTRATION_DUPLICATED);

        assertEquals(1L, registrationMapper.selectCount());
    }

    @Test
    void createRegistrationShouldRejectNonMemberClosedWindowAndUnpublishedActivity() {
        ClubPointClubDO club = insertClub("CLUB-M6-4003", "Guard Registration Club");
        insertMember(club, 3003L, "Employee 3003");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.minusMinutes(1), BASE_TIME.plusDays(1));

        assertServiceException(() -> registrationService.createRegistration(
                buildCreateReq(activity.getId(), 3999L, BASE_TIME)), CLUB_SCOPE_DENIED);
        assertServiceException(() -> registrationService.createRegistration(
                buildCreateReq(activity.getId(), 3003L, BASE_TIME)), CLUB_ACTIVITY_REGISTRATION_CLOSED);

        ClubPointActivityDO draftActivity = insertActivity(club, ClubPointActivityStatusEnum.DRAFT.getStatus(),
                BASE_TIME.plusDays(1), BASE_TIME.plusDays(1));
        assertServiceException(() -> registrationService.createRegistration(
                buildCreateReq(draftActivity.getId(), 3003L, BASE_TIME)), CLUB_ACTIVITY_STATUS_INVALID);
    }

    @Test
    void cancelRegistrationShouldMarkCanceledClearUniqueKeyAndRejectInvalidCases() {
        ClubPointClubDO club = insertClub("CLUB-M6-4004", "Cancel Registration Club");
        insertMember(club, 3004L, "Employee 3004");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(12));
        Long registrationId = registrationService.createRegistration(buildCreateReq(activity.getId(), 3004L, BASE_TIME));

        registrationService.cancelRegistration(new ClubPointRegistrationCancelReqBO()
                .setRegistrationId(registrationId)
                .setUserId(3004L)
                .setReason("personal")
                .setOperationTime(BASE_TIME.plusHours(1)));

        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(registrationId);
        assertEquals(ClubPointRegistrationStatusEnum.CANCELED.getStatus(), registration.getStatus());
        assertEquals(BASE_TIME.plusHours(1), registration.getCancelTime());
        assertEquals(ClubPointRegistrationCancelReasonEnum.SELF_CANCEL.getReasonType(), registration.getCancelReasonType());
        assertEquals("personal", registration.getCancelReason());
        assertEquals(3004L, registration.getCancelOperatorUserId());
        assertTrue(registration.getNoAbsenceDeduct());
        assertNull(registration.getActiveUniqueKey());

        assertServiceException(() -> registrationService.cancelRegistration(new ClubPointRegistrationCancelReqBO()
                .setRegistrationId(registrationId)
                .setUserId(3004L)
                .setOperationTime(BASE_TIME.plusHours(2))), CLUB_ACTIVITY_REGISTRATION_NOT_FOUND);
        assertServiceException(() -> registrationService.cancelRegistration(new ClubPointRegistrationCancelReqBO()
                .setRegistrationId(registrationId)
                .setUserId(3999L)
                .setOperationTime(BASE_TIME.plusHours(2))), CLUB_SCOPE_DENIED);
    }

    @Test
    void cancelRegistrationShouldRejectAfterCancelWindowClosed() {
        ClubPointClubDO club = insertClub("CLUB-M6-4005", "Cancel Window Club");
        insertMember(club, 3005L, "Employee 3005");
        ClubPointActivityDO activity = insertActivity(club, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.plusDays(3), BASE_TIME.plusHours(2));
        Long registrationId = registrationService.createRegistration(buildCreateReq(activity.getId(), 3005L, BASE_TIME));

        assertServiceException(() -> registrationService.cancelRegistration(new ClubPointRegistrationCancelReqBO()
                .setRegistrationId(registrationId)
                .setUserId(3005L)
                .setReason("late")
                .setOperationTime(BASE_TIME.plusHours(3))), CLUB_ACTIVITY_CANCEL_WINDOW_CLOSED);

        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(registrationId);
        assertEquals(ClubPointRegistrationStatusEnum.REGISTERED.getStatus(), registration.getStatus());
        assertNotNull(registration.getActiveUniqueKey());
    }

    @Test
    void leaderAndAdminRegistrationPageShouldRespectScope() {
        ClubPointClubDO managedClub = insertClub("CLUB-M6-4006", "Managed Registration Club");
        ClubPointClubDO otherClub = insertClub("CLUB-M6-4007", "Other Registration Club");
        insertLeader(managedClub.getId(), 2006L);
        insertMember(managedClub, 3006L, "Employee 3006");
        insertMember(otherClub, 3007L, "Employee 3007");
        ClubPointActivityDO managedActivity = insertActivity(managedClub, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(12));
        ClubPointActivityDO otherActivity = insertActivity(otherClub, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(12));
        Long managedRegistrationId = registrationService.createRegistration(
                buildCreateReq(managedActivity.getId(), 3006L, BASE_TIME));
        registrationService.createRegistration(buildCreateReq(otherActivity.getId(), 3007L, BASE_TIME));

        PageResult<ClubPointActivityRegistrationDO> leaderPage = registrationService.getLeaderRegistrationPage(
                2006L, buildPageReq(managedClub.getId(), null, null, null));
        assertEquals(1L, leaderPage.getTotal());
        assertEquals(managedRegistrationId, leaderPage.getList().get(0).getId());

        assertServiceException(() -> registrationService.getLeaderRegistrationPage(
                2006L, buildPageReq(otherClub.getId(), null, null, null)), CLUB_SCOPE_DENIED);

        PageResult<ClubPointActivityRegistrationDO> adminPage = registrationService.getAdminRegistrationPage(
                buildPageReq(null, null, null, null));
        assertEquals(2L, adminPage.getTotal());
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
                .setDeptIdSnapshot(31L)
                .setUserNameSnapshot(userName)
                .setDeptNameSnapshot("Ops")
                .setMobileSnapshot("1380000" + userId)
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setStatus(ClubPointMemberStatusEnum.ACTIVE.getStatus())
                .setJoinTime(BASE_TIME.minusDays(1))
                .setOperatorUserId(900L)
                .setActiveUniqueKey(club.getId() + ":" + userId));
    }

    private void insertLeader(Long clubId, Long userId) {
        leaderMapper.insert(new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(ClubPointLeaderStatusEnum.ACTIVE.getStatus())
                .setAssignedTime(BASE_TIME.minusDays(1))
                .setAssignedBy(900L)
                .setReason("assign")
                .setClubNameSnapshot("club")
                .setUserNameSnapshot("leader")
                .setActiveUniqueKey(clubId + ":" + userId));
    }

    private ClubPointActivityDO insertActivity(ClubPointClubDO club, Integer status,
                                               LocalDateTime registrationDeadline,
                                               LocalDateTime cancelDeadlineTime) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle("Registration Activity")
                .setLocation("Park")
                .setDescription("Registration activity desc")
                .setLevel(2)
                .setStatus(status)
                .setStartTime(BASE_TIME.plusDays(4))
                .setEndTime(BASE_TIME.plusDays(4).plusHours(2))
                .setRegistrationDeadline(registrationDeadline)
                .setCancelDeadlineTime(cancelDeadlineTime)
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

    private static ClubPointRegistrationCreateReqBO buildCreateReq(Long activityId, Long userId,
                                                                    LocalDateTime operationTime) {
        return new ClubPointRegistrationCreateReqBO()
                .setActivityId(activityId)
                .setUserId(userId)
                .setOperationTime(operationTime);
    }

    private static ClubPointRegistrationPageReqBO buildPageReq(Long clubId, Long activityId,
                                                               Integer status, Long userId) {
        ClubPointRegistrationPageReqBO reqBO = new ClubPointRegistrationPageReqBO()
                .setClubId(clubId)
                .setActivityId(activityId)
                .setStatus(status)
                .setUserId(userId);
        reqBO.setPageNo(1);
        reqBO.setPageSize(10);
        return reqBO;
    }

}

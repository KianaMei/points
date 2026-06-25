package cn.iocoder.yudao.module.clubpoints.controller.activity;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.ClubPointActivityAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.ClubPointAttendanceAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.ClubPointRegistrationAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivityReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminAttendanceCorrectReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminAttendanceSupplementReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminSpecialAbsenceReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.ClubPointActivityAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.ClubPointAttendanceAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.ClubPointRegistrationAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppAttendanceCheckReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationCancelReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.ClubPointActivityLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo.LeaderActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo.LeaderActivitySaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceCorrectionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityReviewRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceCorrectionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointAttendanceRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointRegistrationServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ATTENDANCE_CORRECT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.ATTENDANCE_SUPPLEMENT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.SPECIAL_ABSENCE_MARK;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Import({
        ClubPointActivityAppController.class,
        ClubPointRegistrationAppController.class,
        ClubPointAttendanceAppController.class,
        ClubPointActivityLeaderController.class,
        ClubPointActivityAdminController.class,
        ClubPointAttendanceAdminController.class,
        ClubPointRegistrationAdminController.class,
        ClubPointActivityQueryServiceImpl.class,
        ClubPointActivityServiceImpl.class,
        ClubPointRegistrationServiceImpl.class,
        ClubPointAttendanceServiceImpl.class,
        ClubScopeServiceImpl.class,
        ClubAuditServiceImpl.class,
        ClubAttachmentServiceImpl.class,
        ClubPointRuleServiceImpl.class
})
class ClubPointActivityControllerTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);

    @Resource
    private ClubPointActivityAppController appActivityController;
    @Resource
    private ClubPointRegistrationAppController appRegistrationController;
    @Resource
    private ClubPointAttendanceAppController appAttendanceController;
    @Resource
    private ClubPointActivityLeaderController leaderActivityController;
    @Resource
    private ClubPointActivityAdminController adminActivityController;
    @Resource
    private ClubPointAttendanceAdminController adminAttendanceController;
    @Resource
    private ClubPointRegistrationAdminController adminRegistrationController;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointAttendanceRecordMapper attendanceRecordMapper;
    @Resource
    private ClubPointAttendanceCorrectionMapper correctionMapper;
    @Resource
    private ClubPointActivityReviewRecordMapper reviewRecordMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @MockBean
    private FileService fileService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void appActivityEndpointsShouldUseJoinedClubAndSelfRegistrationScope() {
        login(6101L, "员工6101");
        ClubPointClubDO joinedClub = insertClub("CLUB-M6-7101", "员工已加入俱乐部");
        ClubPointClubDO otherClub = insertClub("CLUB-M6-7102", "其他俱乐部");
        insertMember(joinedClub, 6101L, "员工6101");
        ClubPointActivityDO published = insertActivity(joinedClub, "可见活动",
                ClubPointActivityStatusEnum.PUBLISHED.getStatus(), BASE_TIME.plusDays(4));
        ClubPointActivityDO draft = insertActivity(joinedClub, "草稿活动",
                ClubPointActivityStatusEnum.DRAFT.getStatus(), BASE_TIME.plusDays(5));
        ClubPointActivityDO otherPublished = insertActivity(otherClub, "其他俱乐部活动",
                ClubPointActivityStatusEnum.PUBLISHED.getStatus(), BASE_TIME.plusDays(6));
        ClubPointActivityRegistrationDO registration = insertRegistration(published, 6101L);
        insertAttendanceRecord(registration, ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType(),
                ClubPointAttendanceSourceTypeEnum.SELF.getSourceType(), BASE_TIME.plusDays(4).minusMinutes(5));

        AppActivityPageReqVO pageReqVO = new AppActivityPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        PageResult<AppActivityRespVO> page = appActivityController.getActivityPage(pageReqVO).getCheckedData();

        assertEquals(1L, page.getTotal());
        assertEquals(published.getId(), page.getList().get(0).getId());
        assertFalse(page.getList().stream().anyMatch(item -> draft.getId().equals(item.getId())));
        assertFalse(page.getList().stream().anyMatch(item -> otherPublished.getId().equals(item.getId())));

        AppActivityRespVO detail = appActivityController.getActivity(published.getId()).getCheckedData();
        assertTrue(detail.getRegistered());
        assertEquals(registration.getId(), detail.getRegistrationId());
        assertEquals(ClubPointRegistrationStatusEnum.REGISTERED.getStatus(), detail.getRegistrationStatus());
        assertEquals(1, detail.getCheckInStatus());
        assertEquals(0, detail.getCheckOutStatus());
        assertServiceException(() -> appActivityController.getActivity(otherPublished.getId()), CLUB_SCOPE_DENIED);
    }

    @Test
    void appRegistrationAndAttendanceEndpointsShouldWriteThroughServices() {
        login(6102L, "员工6102");
        ClubPointClubDO club = insertClub("CLUB-M6-7111", "员工报名俱乐部");
        insertMember(club, 6102L, "员工6102");
        ClubPointActivityDO activity = insertCurrentWindowActivity(club, "当前窗口活动");
        ClubPointActivityDO cancelActivity = insertCurrentWindowActivity(club, "取消报名活动");

        Long registrationId = appRegistrationController.createRegistration(
                new AppRegistrationCreateReqVO().setActivityId(activity.getId())).getCheckedData();
        Long cancelRegistrationId = appRegistrationController.createRegistration(
                new AppRegistrationCreateReqVO().setActivityId(cancelActivity.getId())).getCheckedData();
        appRegistrationController.cancelRegistration(new AppRegistrationCancelReqVO()
                .setRegistrationId(cancelRegistrationId)
                .setReason("临时有事"));

        Long checkInId = appAttendanceController.checkIn(new AppAttendanceCheckReqVO()
                .setRegistrationId(registrationId)
                .setRemark("到场")).getCheckedData();
        Long checkOutId = appAttendanceController.checkOut(new AppAttendanceCheckReqVO()
                .setRegistrationId(registrationId)
                .setRemark("离场")).getCheckedData();

        ClubPointActivityRegistrationDO canceled = registrationMapper.selectById(cancelRegistrationId);
        assertEquals(ClubPointRegistrationStatusEnum.CANCELED.getStatus(), canceled.getStatus());
        assertEquals("临时有事", canceled.getCancelReason());
        assertNotNull(attendanceRecordMapper.selectById(checkInId));
        assertNotNull(attendanceRecordMapper.selectById(checkOutId));
        assertEquals(2L, attendanceRecordMapper.selectCount());
    }

    @Test
    void leaderActivityManagementEndpointsShouldUseManagedClubScope() {
        login(6201L, "负责人6201");
        ClubPointClubDO managedClub = insertClub("CLUB-M6-7121", "负责俱乐部");
        ClubPointClubDO otherClub = insertClub("CLUB-M6-7122", "非负责俱乐部");
        insertLeader(managedClub, 6201L, "负责人6201");

        Long activityId = leaderActivityController.createActivity(buildLeaderSaveReq(managedClub.getId(), null))
                .getCheckedData();
        leaderActivityController.updateActivity(buildLeaderSaveReq(managedClub.getId(), activityId)
                .setTitle("负责人修改后的活动"));
        leaderActivityController.submitActivity(activityId);

        LeaderActivityPageReqVO pageReqVO = new LeaderActivityPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        pageReqVO.setClubId(managedClub.getId());
        PageResult<AppActivityRespVO> page = leaderActivityController.getActivityPage(pageReqVO).getCheckedData();
        assertEquals(1L, page.getTotal());
        assertEquals(activityId, page.getList().get(0).getId());
        assertEquals("负责人修改后的活动", activityMapper.selectById(activityId).getTitle());
        assertEquals(ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus(),
                activityMapper.selectById(activityId).getStatus());

        ClubPointActivityDO published = insertActivity(managedClub, "负责人取消活动",
                ClubPointActivityStatusEnum.PUBLISHED.getStatus(), BASE_TIME.plusDays(8));
        leaderActivityController.cancelActivity(published.getId(), "天气原因");
        assertEquals(ClubPointActivityStatusEnum.CANCELED.getStatus(),
                activityMapper.selectById(published.getId()).getStatus());

        assertServiceException(() -> leaderActivityController.createActivity(
                buildLeaderSaveReq(otherClub.getId(), null)), CLUB_SCOPE_DENIED);
    }

    @Test
    void adminReviewAndAttendanceCorrectionEndpointsShouldUseGlobalScope() {
        login(1L, "管理员");
        seedActivityRules();
        ClubPointClubDO club = insertClub("CLUB-M6-7131", "管理员审核俱乐部");
        insertLeader(club, 6202L, "负责人6202");
        login(6202L, "负责人6202");
        Long activityId = leaderActivityController.createActivity(buildLeaderSaveReq(club.getId(), null))
                .getCheckedData();
        leaderActivityController.submitActivity(activityId);

        login(1L, "管理员");
        adminActivityController.reviewActivity(new AdminActivityReviewReqVO()
                .setId(activityId)
                .setApproved(true)
                .setReason("审核通过"));

        assertEquals(ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                activityMapper.selectById(activityId).getStatus());
        ClubPointActivityReviewRecordDO reviewRecord = reviewRecordMapper.selectOne(null);
        assertEquals(1, reviewRecord.getResult());

        ClubPointActivityRegistrationDO registration = insertRegistration(activityMapper.selectById(activityId), 6103L);
        Long supplementCorrectionId = adminAttendanceController.supplementAttendance(
                new AdminAttendanceSupplementReqVO()
                        .setRegistrationId(registration.getId())
                        .setTargetType(ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType())
                        .setRecordTime(BASE_TIME.plusDays(4))
                        .setReason("管理员补录")).getCheckedData();
        ClubPointAttendanceCorrectionDO supplementCorrection = correctionMapper.selectById(supplementCorrectionId);
        ClubPointAttendanceRecordDO record = attendanceRecordMapper.selectById(
                supplementCorrection.getAttendanceRecordId());

        Long correctCorrectionId = adminAttendanceController.correctAttendance(new AdminAttendanceCorrectReqVO()
                .setAttendanceRecordId(record.getId())
                .setNewRecordTime(BASE_TIME.plusDays(4).plusMinutes(3))
                .setReason("管理员修正")).getCheckedData();
        adminRegistrationController.markSpecialAbsence(new AdminSpecialAbsenceReqVO()
                .setRegistrationId(registration.getId())
                .setReason("特殊请假"));

        assertEquals(BASE_TIME.plusDays(4).plusMinutes(3),
                attendanceRecordMapper.selectById(record.getId()).getRecordTime());
        assertNotNull(correctionMapper.selectById(correctCorrectionId));
        ClubPointActivityRegistrationDO updatedRegistration = registrationMapper.selectById(registration.getId());
        assertTrue(updatedRegistration.getSpecialAbsenceFlag());
        Set<String> auditActions = auditLogMapper.selectList().stream()
                .map(ClubAuditLogDO::getActionType)
                .collect(Collectors.toSet());
        assertTrue(auditActions.contains(ATTENDANCE_SUPPLEMENT));
        assertTrue(auditActions.contains(ATTENDANCE_CORRECT));
        assertTrue(auditActions.contains(SPECIAL_ABSENCE_MARK));
    }

    @Test
    void endpointsShouldUseDocumentedActivityPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/app/activity",
                ClubPointActivityAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/app/registration",
                ClubPointRegistrationAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/app/attendance",
                ClubPointAttendanceAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/leader/activity",
                ClubPointActivityLeaderController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/activity",
                ClubPointActivityAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/attendance",
                ClubPointAttendanceAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/registration",
                ClubPointRegistrationAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertGetMapping(ClubPointActivityAppController.class, "getActivityPage",
                new Class<?>[]{AppActivityPageReqVO.class}, "/page", null);
        assertGetMapping(ClubPointActivityAppController.class, "getActivity",
                new Class<?>[]{Long.class}, "/get", null);
        assertPostMapping(ClubPointRegistrationAppController.class, "createRegistration",
                new Class<?>[]{AppRegistrationCreateReqVO.class}, "/create",
                "@ss.hasPermission('clubpoints:registration:create')");
        assertPostMapping(ClubPointRegistrationAppController.class, "cancelRegistration",
                new Class<?>[]{AppRegistrationCancelReqVO.class}, "/cancel",
                "@ss.hasPermission('clubpoints:registration:cancel')");
        assertPostMapping(ClubPointAttendanceAppController.class, "checkIn",
                new Class<?>[]{AppAttendanceCheckReqVO.class}, "/check-in",
                "@ss.hasPermission('clubpoints:attendance:check-in')");
        assertPostMapping(ClubPointAttendanceAppController.class, "checkOut",
                new Class<?>[]{AppAttendanceCheckReqVO.class}, "/check-out",
                "@ss.hasPermission('clubpoints:attendance:check-out')");
        assertGetMapping(ClubPointActivityLeaderController.class, "getActivityPage",
                new Class<?>[]{LeaderActivityPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:activity:query')");
        assertPostMapping(ClubPointActivityLeaderController.class, "createActivity",
                new Class<?>[]{LeaderActivitySaveReqVO.class}, "/create",
                "@ss.hasPermission('clubpoints:activity:create')");
        assertPutMapping(ClubPointActivityLeaderController.class, "updateActivity",
                new Class<?>[]{LeaderActivitySaveReqVO.class}, "/update",
                "@ss.hasPermission('clubpoints:activity:update')");
        assertPostMapping(ClubPointActivityLeaderController.class, "submitActivity",
                new Class<?>[]{Long.class}, "/submit",
                "@ss.hasPermission('clubpoints:activity:submit')");
        assertPostMapping(ClubPointActivityLeaderController.class, "cancelActivity",
                new Class<?>[]{Long.class, String.class}, "/cancel",
                "@ss.hasPermission('clubpoints:activity:cancel')");
        assertPostMapping(ClubPointActivityAdminController.class, "reviewActivity",
                new Class<?>[]{AdminActivityReviewReqVO.class}, "/review",
                "@ss.hasPermission('clubpoints:activity:review')");
        assertPostMapping(ClubPointAttendanceAdminController.class, "supplementAttendance",
                new Class<?>[]{AdminAttendanceSupplementReqVO.class}, "/supplement",
                "@ss.hasPermission('clubpoints:attendance:correct')");
        assertPostMapping(ClubPointAttendanceAdminController.class, "correctAttendance",
                new Class<?>[]{AdminAttendanceCorrectReqVO.class}, "/correct",
                "@ss.hasPermission('clubpoints:attendance:correct')");
        assertPostMapping(ClubPointRegistrationAdminController.class, "markSpecialAbsence",
                new Class<?>[]{AdminSpecialAbsenceReqVO.class}, "/mark-special-absence",
                "@ss.hasPermission('clubpoints:registration:special-absence')");
    }

    private static void assertGetMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                         String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPostMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                          String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PostMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPutMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                         String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(PutMapping.class).value()[0]);
        assertPermission(method, expectedPermission);
    }

    private static void assertPermission(Method method, String expectedPermission) {
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        if (expectedPermission == null) {
            assertFalse(method.isAnnotationPresent(PreAuthorize.class));
        } else {
            assertNotNull(preAuthorize);
            assertEquals(expectedPermission, preAuthorize.value());
        }
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
                .setActiveUniqueKey(club.getId() + ":" + userId));
    }

    private void insertLeader(ClubPointClubDO club, Long userId, String userName) {
        leaderMapper.insert(new ClubLeaderDO()
                .setClubId(club.getId())
                .setUserId(userId)
                .setStatus(ClubPointLeaderStatusEnum.ACTIVE.getStatus())
                .setAssignedTime(BASE_TIME.minusDays(1))
                .setAssignedBy(900L)
                .setReason("assign")
                .setClubNameSnapshot(club.getName())
                .setUserNameSnapshot(userName)
                .setActiveUniqueKey(club.getId() + ":" + userId));
    }

    private ClubPointActivityDO insertActivity(ClubPointClubDO club, String title, Integer status,
                                               LocalDateTime startTime) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(club.getId())
                .setClubCodeSnapshot(club.getCode())
                .setClubNameSnapshot(club.getName())
                .setTitle(title)
                .setLocation("Gym")
                .setDescription("Activity desc")
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
                .setSnapshotJson(snapshotJson(2, 8, 2))
                .setRemark("activity remark");
        activityMapper.insert(activity);
        return activity;
    }

    private ClubPointActivityDO insertCurrentWindowActivity(ClubPointClubDO club, String title) {
        LocalDateTime now = LocalDateTime.now();
        ClubPointActivityDO activity = insertActivity(club, title,
                ClubPointActivityStatusEnum.PUBLISHED.getStatus(), now.plusMinutes(10));
        activity.setRegistrationDeadline(now.plusMinutes(5))
                .setCancelDeadlineTime(now.plusMinutes(5))
                .setCheckinStartTime(now.minusMinutes(5))
                .setCheckinEndTime(now.plusMinutes(5))
                .setCheckoutStartTime(now.minusMinutes(5))
                .setCheckoutEndTime(now.plusMinutes(5));
        activityMapper.updateById(activity);
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
                                                               Integer targetType, Integer sourceType,
                                                               LocalDateTime recordTime) {
        ClubPointAttendanceRecordDO record = new ClubPointAttendanceRecordDO()
                .setRegistrationId(registration.getId())
                .setActivityId(registration.getActivityId())
                .setUserId(registration.getUserId())
                .setTargetType(targetType)
                .setRecordTime(recordTime)
                .setSourceType(sourceType)
                .setOperatorUserId(registration.getUserId())
                .setClientIp("127.0.0.1")
                .setRemark("attendance");
        attendanceRecordMapper.insert(record);
        return record;
    }

    private LeaderActivitySaveReqVO buildLeaderSaveReq(Long clubId, Long id) {
        return new LeaderActivitySaveReqVO()
                .setId(id)
                .setClubId(clubId)
                .setTitle("负责人活动")
                .setLocation("Room A")
                .setDescription("Leader activity")
                .setLevel(2)
                .setStartTime(BASE_TIME.plusDays(4))
                .setEndTime(BASE_TIME.plusDays(4).plusHours(2))
                .setRegistrationDeadline(BASE_TIME.plusDays(3))
                .setCancelDeadlineTime(BASE_TIME.plusDays(3).plusHours(12))
                .setCheckinStartTime(BASE_TIME.plusDays(4).minusMinutes(30))
                .setCheckinEndTime(BASE_TIME.plusDays(4).plusMinutes(30))
                .setCheckoutMode(1)
                .setCheckoutStartTime(BASE_TIME.plusDays(4).plusHours(1))
                .setCheckoutEndTime(BASE_TIME.plusDays(4).plusHours(3))
                .setBasePoints(8)
                .setFullExtraPoints(2)
                .setReason("leader operation");
    }

    private void seedActivityRules() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("M6-API-RULE-001")
                .setName("M6 API rules")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(BASE_TIME.minusDays(1))
                .setPublishedTime(BASE_TIME.minusDays(1));
        ruleVersionMapper.insert(version);
        insertRuleItem(version.getId(), ACTIVITY_MEDIUM_BASE.getCode(), "Medium base", 1, 10, 8, 2);
        insertRuleItem(version.getId(), ACTIVITY_FULL_EXTRA.getCode(), "Full extra", 1, 11, 2, 3);
    }

    private void insertRuleItem(Long versionId, String code, String name, Integer itemType, Integer category,
                                Integer defaultPoints, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(itemType)
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(20)
                .setDefaultPoints(defaultPoints)
                .setStatus(1)
                .setSort(sort));
    }

    private static String snapshotJson(Integer level, Integer basePoints, Integer fullExtraPoints) {
        return "{\"level\":" + level + ",\"basePoints\":" + basePoints
                + ",\"fullExtraPoints\":" + fullExtraPoints + "}";
    }

    private static void login(Long userId, String nickname) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        java.util.Map<String, String> info = new java.util.HashMap<>();
        info.put(INFO_KEY_NICKNAME, nickname);
        SecurityFrameworkUtils.setLoginUser(new LoginUser()
                .setId(userId)
                .setUserType(ADMIN.getValue())
                .setInfo(info), request);
    }

    private static void assertServiceException(Runnable runnable, ErrorCode errorCode) {
        try {
            runnable.run();
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertEquals(errorCode.getCode(), ex.getCode());
            assertEquals(errorCode.getMsg(), ex.getMessage());
        }
    }

}

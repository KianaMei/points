package cn.iocoder.yudao.module.clubpoints.controller.dashboard;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dashboard.ClubPointDashboardAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dashboard.vo.AdminDashboardSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dashboard.ClubPointDashboardAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.dashboard.vo.AppDashboardSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.dashboard.ClubPointDashboardLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.dashboard.vo.LeaderDashboardSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.dispute.ClubPointDisputeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.ClubPointDashboardServiceImpl;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyMessageMapper;
import cn.iocoder.yudao.module.system.service.notify.NotifyMessageServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointDashboardAppController.class,
        ClubPointDashboardLeaderController.class,
        ClubPointDashboardAdminController.class,
        ClubPointDashboardServiceImpl.class,
        NotifyMessageServiceImpl.class
})
class ClubPointDashboardControllerTest extends BaseDbUnitTest {

    private static final int STATUS_ACTIVE = 1;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 26, 9, 0);

    @Resource
    private ClubPointDashboardAppController appController;
    @Resource
    private ClubPointDashboardLeaderController leaderController;
    @Resource
    private ClubPointDashboardAdminController adminController;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private ClubLeaderMapper clubLeaderMapper;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointContributionMaterialMapper contributionMaterialMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper redemptionApplicationMapper;
    @Resource
    private ClubPointDisputeMapper disputeMapper;
    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void appDashboardShouldSummarizeLoginUserOnly() {
        login(100L, "员工A");
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(100L)
                .setAvailablePoints(88)
                .setFrozenPoints(12)
                .setAnnualEarnedPoints(140)
                .setTotalPositivePoints(160)
                .setTotalNegativePoints(20)
                .setNetPoints(140)
                .setVersion(1));
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(101L)
                .setAvailablePoints(999)
                .setFrozenPoints(0)
                .setAnnualEarnedPoints(999)
                .setTotalPositivePoints(999)
                .setTotalNegativePoints(0)
                .setNetPoints(999)
                .setVersion(1));
        clubMemberMapper.insert(buildMember(100L, 400L, STATUS_ACTIVE));
        clubMemberMapper.insert(buildMember(100L, 401L, STATUS_ACTIVE));
        clubMemberMapper.insert(buildMember(100L, 402L, 2));
        clubMemberMapper.insert(buildMember(101L, 403L, STATUS_ACTIVE));

        Long futureActivityId = insertActivity(400L, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                "未来活动", BASE_TIME.plusDays(1), BASE_TIME.plusDays(1).plusHours(2));
        Long endedActivityId = insertActivity(400L, ClubPointActivityStatusEnum.ENDED.getStatus(),
                "已结束活动", BASE_TIME.minusDays(1), BASE_TIME.minusDays(1).plusHours(2));
        insertRegistration(futureActivityId, 400L, 100L, ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(endedActivityId, 400L, 100L, ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(futureActivityId, 400L, 101L, ClubPointRegistrationStatusEnum.REGISTERED.getStatus());

        insertRedemptionApplication(100L, ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus());
        insertRedemptionApplication(100L, ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus());
        insertRedemptionApplication(101L, ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus());
        insertNotifyMessage(100L, false);
        insertNotifyMessage(100L, true);
        insertNotifyMessage(101L, false);

        AppDashboardSummaryRespVO summary = appController.getSummary().getCheckedData();

        assertEquals(88, summary.getAvailablePoints());
        assertEquals(12, summary.getFrozenPoints());
        assertEquals(140, summary.getTotalEarnedPoints());
        assertEquals(2, summary.getJoinedClubCount());
        assertEquals(1, summary.getRegisteredActivityCount());
        assertEquals(1, summary.getPendingRedemptionCount());
        assertEquals(1, summary.getUnreadNotifyCount());
        assertEquals(3, summary.getTodoCount());
        assertTrue(summary.getTodoItems().stream().anyMatch(item ->
                "app_notify_unread".equals(item.getCode()) && item.getCount() == 1
                        && "/clubpoints/app/notify".equals(item.getPath())));
    }

    @Test
    void leaderDashboardShouldSummarizeManagedClubOnly() {
        login(900L, "负责人");
        clubLeaderMapper.insert(buildLeader(900L, 400L, STATUS_ACTIVE));
        clubLeaderMapper.insert(buildLeader(900L, 401L, 2));
        clubLeaderMapper.insert(buildLeader(901L, 402L, STATUS_ACTIVE));
        insertActivity(400L, ClubPointActivityStatusEnum.DRAFT.getStatus(),
                "负责俱乐部草稿", BASE_TIME.plusDays(1), BASE_TIME.plusDays(1).plusHours(2));
        insertActivity(400L, ClubPointActivityStatusEnum.REJECTED.getStatus(),
                "负责俱乐部驳回", BASE_TIME.plusDays(2), BASE_TIME.plusDays(2).plusHours(2));
        insertActivity(402L, ClubPointActivityStatusEnum.DRAFT.getStatus(),
                "其他负责人草稿", BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(2));
        Long endedActivityId = insertActivity(400L, ClubPointActivityStatusEnum.ENDED.getStatus(),
                "负责俱乐部缺签到", BASE_TIME.minusDays(1), BASE_TIME.minusDays(1).plusHours(2));
        Long otherEndedActivityId = insertActivity(402L, ClubPointActivityStatusEnum.ENDED.getStatus(),
                "其他俱乐部缺签到", BASE_TIME.minusDays(1), BASE_TIME.minusDays(1).plusHours(2));
        insertRegistration(endedActivityId, 400L, 100L, ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertRegistration(otherEndedActivityId, 402L, 101L, ClubPointRegistrationStatusEnum.REGISTERED.getStatus());
        insertContribution(400L, ClubPointContributionMaterialStatusEnum.DRAFT.getStatus(), false);
        insertContribution(400L, ClubPointContributionMaterialStatusEnum.WITHDRAWN.getStatus(), false);
        insertContribution(400L, ClubPointContributionMaterialStatusEnum.REJECTED.getStatus(), false);
        insertContribution(400L, ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(), false);
        insertContribution(400L, ClubPointContributionMaterialStatusEnum.DRAFT.getStatus(), true);
        insertContribution(402L, ClubPointContributionMaterialStatusEnum.DRAFT.getStatus(), false);

        LeaderDashboardSummaryRespVO summary = leaderController.getSummary().getCheckedData();

        assertEquals(1, summary.getManagedClubCount());
        assertEquals(1, summary.getDraftActivityCount());
        assertEquals(1, summary.getRejectedActivityCount());
        assertEquals(1, summary.getAttendanceExceptionCount());
        assertEquals(3, summary.getPendingContributionSubmitCount());
        assertEquals(6, summary.getTodoCount());
        assertTrue(summary.getTodoItems().stream().anyMatch(item ->
                "leader_contribution_to_submit".equals(item.getCode()) && item.getCount() == 3
                        && "/clubpoints/leader/contribution".equals(item.getPath())));
    }

    @Test
    void adminDashboardShouldCountGlobalPendingBusinessTodos() {
        login(1L, "管理员");
        insertActivity(400L, ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus(),
                "待审核活动一", BASE_TIME.plusDays(1), BASE_TIME.plusDays(1).plusHours(2));
        insertActivity(401L, ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus(),
                "待审核活动二", BASE_TIME.plusDays(2), BASE_TIME.plusDays(2).plusHours(2));
        insertActivity(402L, ClubPointActivityStatusEnum.PUBLISHED.getStatus(),
                "已发布活动", BASE_TIME.plusDays(3), BASE_TIME.plusDays(3).plusHours(2));
        insertContribution(400L, ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(), false);
        insertContribution(401L, ClubPointContributionMaterialStatusEnum.APPROVED.getStatus(), false);
        insertRedemptionApplication(100L, ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus());
        insertRedemptionApplication(101L, ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus());
        insertDispute(100L, ClubPointDisputeStatusEnum.PENDING.getStatus());
        insertDispute(101L, ClubPointDisputeStatusEnum.REPLIED.getStatus());

        AdminDashboardSummaryRespVO summary = adminController.getSummary().getCheckedData();

        assertEquals(2, summary.getPendingActivityReviewCount());
        assertEquals(1, summary.getPendingContributionReviewCount());
        assertEquals(1, summary.getPendingRedemptionReviewCount());
        assertEquals(1, summary.getPendingDisputeCount());
        assertEquals(5, summary.getTodoCount());
        assertEquals(4, summary.getTodoItems().size());
        assertTrue(summary.getTodoItems().stream().anyMatch(item ->
                "admin_activity_review".equals(item.getCode()) && item.getCount() == 2
                        && "/clubpoints/admin/activity".equals(item.getPath())));
    }

    @Test
    void endpointsShouldUseDocumentedDashboardPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/app/dashboard",
                ClubPointDashboardAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/leader/dashboard",
                ClubPointDashboardLeaderController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/admin/dashboard",
                ClubPointDashboardAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertGetMapping(ClubPointDashboardAppController.class, "getSummary", null);
        assertGetMapping(ClubPointDashboardLeaderController.class, "getSummary",
                "@ss.hasPermission('clubpoints:leader')");
        assertGetMapping(ClubPointDashboardAdminController.class, "getSummary",
                "@ss.hasPermission('clubpoints:dashboard:query')");
    }

    private static void assertGetMapping(Class<?> controllerClass, String methodName, String expectedPermission)
            throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName);
        assertEquals("/summary", method.getAnnotation(GetMapping.class).value()[0]);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        if (expectedPermission == null) {
            assertFalse(method.isAnnotationPresent(PreAuthorize.class));
        } else {
            assertNotNull(preAuthorize);
            assertEquals(expectedPermission, preAuthorize.value());
        }
    }

    private Long insertActivity(Long clubId, Integer status, String title,
                                LocalDateTime startTime, LocalDateTime endTime) {
        ClubPointActivityDO activity = new ClubPointActivityDO()
                .setClubId(clubId)
                .setClubCodeSnapshot("CLUB-" + clubId)
                .setClubNameSnapshot("俱乐部" + clubId)
                .setTitle(title)
                .setDescription(title)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setCheckinStartTime(startTime.minusMinutes(30))
                .setCheckinEndTime(startTime.plusMinutes(30))
                .setCheckoutStartTime(endTime.minusMinutes(30))
                .setCheckoutEndTime(endTime.plusMinutes(30))
                .setLocation("活动室")
                .setLevel(1)
                .setStatus(status)
                .setRegistrationDeadline(startTime.minusHours(1))
                .setCancelDeadlineTime(startTime.minusHours(2))
                .setCheckoutMode(1)
                .setCurrentConfigVersionId(1L)
                .setCreatorUserId(900L);
        activityMapper.insert(activity);
        return activity.getId();
    }

    private void insertRegistration(Long activityId, Long clubId, Long userId, Integer status) {
        registrationMapper.insert(new ClubPointActivityRegistrationDO()
                .setActivityId(activityId)
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(status)
                .setRegisterTime(BASE_TIME.minusHours(1))
                .setNoAbsenceDeduct(false)
                .setSpecialAbsenceFlag(false)
                .setUserNameSnapshot("员工" + userId)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setMobileSnapshot("13800000000")
                .setClubNameSnapshot("俱乐部" + clubId)
                .setActivityTitleSnapshot("活动" + activityId)
                .setActivityStartTimeSnapshot(BASE_TIME)
                .setActivityEndTimeSnapshot(BASE_TIME.plusHours(2))
                .setActiveUniqueKey(clubId + ":" + activityId + ":" + userId));
    }

    private void insertContribution(Long clubId, Integer status, Boolean directCreated) {
        ClubPointContributionMaterialDO material = new ClubPointContributionMaterialDO()
                .setRequestNo("REQ-" + clubId + "-" + status + "-" + directCreated + "-" + System.nanoTime())
                .setClubId(clubId)
                .setClubNameSnapshot("俱乐部" + clubId)
                .setTitle("材料")
                .setDescription("材料")
                .setType(1)
                .setSubmitterUserId(900L)
                .setRuleVersionId(1L)
                .setStatus(status)
                .setSubmitTime(BASE_TIME)
                .setLocked(false)
                .setDirectCreated(directCreated)
                .setSnapshotJson("{}");
        contributionMaterialMapper.insert(material);
    }

    private void insertRedemptionApplication(Long userId, Integer status) {
        redemptionApplicationMapper.insert(new ClubPointRedemptionApplicationDO()
                .setApplicationNo("APP-" + userId + "-" + status + "-" + System.nanoTime())
                .setRequestNo("REQ-APP-" + userId + "-" + status + "-" + System.nanoTime())
                .setBatchId(1L)
                .setGiftId(1L)
                .setEligibilitySnapshotId(1L)
                .setUserId(userId)
                .setPointsCost(10)
                .setQuantity(1)
                .setFreezeId(1L)
                .setStockLockId(1L)
                .setQualificationRankSnapshot(1)
                .setBeforeNetPoints(100)
                .setBeforeFrozenPoints(0)
                .setBeforeAvailablePoints(100)
                .setBatchSnapshotJson("{}")
                .setGiftSnapshotJson("{}")
                .setStatus(status)
                .setApplyTime(BASE_TIME)
                .setIdempotencyKey("IDEMP-" + userId + "-" + status + "-" + System.nanoTime()));
    }

    private void insertDispute(Long userId, Integer status) {
        disputeMapper.insert(new ClubPointDisputeDO()
                .setUserId(userId)
                .setTargetType(1)
                .setTargetId(1L)
                .setTitle("积分异议")
                .setContent("少算积分")
                .setStatus(status)
                .setSubmitTime(BASE_TIME));
    }

    private void insertNotifyMessage(Long userId, Boolean readStatus) {
        NotifyMessageDO message = new NotifyMessageDO();
        message.setUserId(userId);
        message.setUserType(ADMIN.getValue());
        message.setTemplateId(1L);
        message.setTemplateCode("club_points_changed");
        message.setTemplateNickname("system");
        message.setTemplateContent("积分通知");
        message.setTemplateType(2);
        message.setTemplateParams(Collections.singletonMap("content", "积分通知"));
        message.setReadStatus(readStatus);
        notifyMessageMapper.insert(message);
    }

    private static ClubMemberDO buildMember(Long userId, Long clubId, Integer status) {
        return new ClubMemberDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setDeptIdSnapshot(10L)
                .setUserNameSnapshot("员工" + userId)
                .setDeptNameSnapshot("综合部")
                .setMobileSnapshot("13800000000")
                .setClubCodeSnapshot("CLUB-" + clubId)
                .setClubNameSnapshot("俱乐部" + clubId)
                .setStatus(status)
                .setJoinTime(BASE_TIME)
                .setActiveUniqueKey(status == STATUS_ACTIVE ? clubId + ":" + userId : null);
    }

    private static ClubLeaderDO buildLeader(Long userId, Long clubId, Integer status) {
        return new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(status)
                .setAssignedTime(BASE_TIME)
                .setAssignedBy(1L)
                .setReason("任命负责人")
                .setClubNameSnapshot("俱乐部" + clubId)
                .setUserNameSnapshot("负责人" + userId)
                .setActiveUniqueKey(status == STATUS_ACTIVE ? clubId + ":" + userId : null);
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

}

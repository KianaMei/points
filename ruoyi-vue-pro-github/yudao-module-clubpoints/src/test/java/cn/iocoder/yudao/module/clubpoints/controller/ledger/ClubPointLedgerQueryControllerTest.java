package cn.iocoder.yudao.module.clubpoints.controller.ledger;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.ClubPointLedgerAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.ClubPointLedgerAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.ClubPointLedgerLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerMemberSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerMemberSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Import({
        ClubPointLedgerAppController.class,
        ClubPointLedgerLeaderController.class,
        ClubPointLedgerAdminController.class,
        ClubPointLedgerQueryServiceImpl.class,
        ClubScopeServiceImpl.class
})
class ClubPointLedgerQueryControllerTest extends BaseDbUnitTest {

    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_VALID = ClubPointTransactionStatusEnum.VALID.getStatus();
    private static final int DIRECTION_INCREASE = ClubPointTransactionDirectionEnum.INCREASE.getDirection();
    private static final int DIRECTION_DECREASE = ClubPointTransactionDirectionEnum.DECREASE.getDirection();
    private static final int SOURCE_ACTIVITY = ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType();
    private static final int SOURCE_ADJUSTMENT = ClubPointTransactionSourceTypeEnum.ADJUSTMENT.getType();
    private static final int CATEGORY_BASIC = ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory();
    private static final int CATEGORY_DEDUCTION = ClubPointCategoryEnum.DEDUCTION.getCategory();
    private static final int CATEGORY_ANNUAL_CLEARING = ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory();

    @Resource
    private ClubPointLedgerAppController appController;
    @Resource
    private ClubPointLedgerLeaderController leaderController;
    @Resource
    private ClubPointLedgerAdminController adminController;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private ClubLeaderMapper clubLeaderMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void appLedgerEndpointsShouldUseLoginUserOnly() {
        login(100L, "员工A");
        accountMapper.insert(buildAccount(100L, 30, 6, 24, 4, 20, 30));
        accountMapper.insert(buildAccount(101L, 99, 0, 99, 0, 99, 99));
        Long ownTransactionId = insertTransaction("TX-M4-7001", 100L, "员工A", DIRECTION_INCREASE, 8,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "篮球俱乐部", "本人活动积分");
        insertTransaction("TX-M4-7002", 101L, "员工B", DIRECTION_INCREASE, 99,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "篮球俱乐部", "别人活动积分");
        insertTransaction("TX-M4-7003", 100L, "员工A", DIRECTION_DECREASE, 2,
                CATEGORY_ANNUAL_CLEARING, SOURCE_ADJUSTMENT, null, null, "年度清零");

        AppLedgerSummaryRespVO summary = appController.getSummary().getCheckedData();
        assertEquals(20, summary.getAvailablePoints());
        assertEquals(4, summary.getFrozenPoints());
        assertEquals(30, summary.getTotalPositivePoints());
        assertEquals(6, summary.getTotalNegativePoints());
        assertEquals(2, summary.getAnnualClearedPoints());
        assertNotNull(summary.getLastTransactionTime());

        AppLedgerTransactionPageReqVO pageReqVO = new AppLedgerTransactionPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        PageResult<AppLedgerTransactionRespVO> page = appController.getTransactionPage(pageReqVO).getCheckedData();
        assertEquals(2L, page.getTotal());
        Set<Long> ids = page.getList().stream().map(AppLedgerTransactionRespVO::getId).collect(Collectors.toSet());
        assertTrue(ids.contains(ownTransactionId));
        assertFalse(page.getList().stream().anyMatch(item -> Long.valueOf(101L).equals(item.getUserId())));

        Set<String> appReqFields = java.util.Arrays.stream(AppLedgerTransactionPageReqVO.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertFalse(appReqFields.contains("userId"));
    }

    @Test
    void leaderLedgerEndpointsShouldOnlyExposeManagedClubIssuedSource() {
        login(900L, "负责人");
        clubLeaderMapper.insert(buildLeader(900L, 400L, STATUS_ACTIVE));
        clubMemberMapper.insert(buildMember(100L, 400L, STATUS_ACTIVE));
        clubMemberMapper.insert(buildMember(101L, 400L, STATUS_ACTIVE));
        accountMapper.insert(buildAccount(100L, 1000, 200, 800, 0, 800, 1000));
        accountMapper.insert(buildAccount(101L, 500, 0, 500, 0, 500, 500));
        Long managedIncreaseId = insertTransaction("TX-M4-7010", 100L, "员工A", DIRECTION_INCREASE, 12,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "篮球俱乐部", "本俱乐部发分");
        Long managedDeductionId = insertTransaction("TX-M4-7011", 100L, "员工A", DIRECTION_DECREASE, 3,
                CATEGORY_DEDUCTION, SOURCE_ACTIVITY, 400L, "篮球俱乐部", "本俱乐部扣分");
        insertTransaction("TX-M4-7012", 100L, "员工A", DIRECTION_INCREASE, 200,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 401L, "足球俱乐部", "其他俱乐部发分");
        insertTransaction("TX-M4-7013", 100L, "员工A", DIRECTION_INCREASE, 50,
                CATEGORY_BASIC, SOURCE_ADJUSTMENT, null, null, "无俱乐部调整");
        insertTransaction("TX-M4-7014", 101L, "员工B", DIRECTION_INCREASE, 7,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "篮球俱乐部", "本俱乐部给员工B发分");

        LeaderLedgerTransactionPageReqVO transactionReqVO = new LeaderLedgerTransactionPageReqVO();
        transactionReqVO.setPageNo(1);
        transactionReqVO.setPageSize(10);
        transactionReqVO.setClubId(400L);
        PageResult<LeaderLedgerTransactionRespVO> transactionPage = leaderController.getTransactionPage(transactionReqVO)
                .getCheckedData();
        assertEquals(3L, transactionPage.getTotal());
        Set<Long> transactionIds = transactionPage.getList().stream()
                .map(LeaderLedgerTransactionRespVO::getId)
                .collect(Collectors.toSet());
        assertTrue(transactionIds.contains(managedIncreaseId));
        assertTrue(transactionIds.contains(managedDeductionId));
        assertFalse(transactionPage.getList().stream().anyMatch(item -> Long.valueOf(401L).equals(item.getIssuingClubId())));
        assertFalse(transactionPage.getList().stream().anyMatch(item -> item.getIssuingClubId() == null));

        LeaderLedgerMemberSummaryPageReqVO summaryReqVO = new LeaderLedgerMemberSummaryPageReqVO();
        summaryReqVO.setPageNo(1);
        summaryReqVO.setPageSize(10);
        summaryReqVO.setClubId(400L);
        PageResult<LeaderLedgerMemberSummaryRespVO> summaryPage = leaderController.getMemberSummaryPage(summaryReqVO)
                .getCheckedData();
        LeaderLedgerMemberSummaryRespVO user100 = summaryPage.getList().stream()
                .filter(item -> Long.valueOf(100L).equals(item.getUserId()))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals(12, user100.getClubPositivePoints());
        assertEquals(3, user100.getClubNegativePoints());
        assertEquals(9, user100.getClubNetPoints());
        assertEquals(managedIncreaseId, user100.getLastTransactionId());

        LeaderLedgerTransactionPageReqVO otherClubReqVO = new LeaderLedgerTransactionPageReqVO();
        otherClubReqVO.setPageNo(1);
        otherClubReqVO.setPageSize(10);
        otherClubReqVO.setClubId(401L);
        assertServiceException(() -> leaderController.getTransactionPage(otherClubReqVO), CLUB_SCOPE_DENIED);
    }

    @Test
    void adminLedgerEndpointsShouldExposeGlobalTransactionsAndAccounts() {
        login(1L, "管理员");
        accountMapper.insert(buildAccount(100L, 30, 0, 30, 0, 30, 30));
        accountMapper.insert(buildAccount(101L, 20, 0, 20, 0, 20, 20));
        Long firstId = insertTransaction("TX-M4-7020", 100L, "员工A", DIRECTION_INCREASE, 30,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 400L, "篮球俱乐部", "管理员全局流水一");
        Long secondId = insertTransaction("TX-M4-7021", 101L, "员工B", DIRECTION_INCREASE, 20,
                CATEGORY_BASIC, SOURCE_ACTIVITY, 401L, "足球俱乐部", "管理员全局流水二");

        AdminLedgerAccountPageReqVO accountReqVO = new AdminLedgerAccountPageReqVO();
        accountReqVO.setPageNo(1);
        accountReqVO.setPageSize(10);
        PageResult<AdminLedgerAccountRespVO> accountPage = adminController.getAccountPage(accountReqVO).getCheckedData();
        assertEquals(2L, accountPage.getTotal());

        AdminLedgerTransactionPageReqVO transactionReqVO = new AdminLedgerTransactionPageReqVO();
        transactionReqVO.setPageNo(1);
        transactionReqVO.setPageSize(10);
        PageResult<AdminLedgerTransactionRespVO> transactionPage = adminController.getTransactionPage(transactionReqVO)
                .getCheckedData();
        Set<Long> ids = transactionPage.getList().stream()
                .map(AdminLedgerTransactionRespVO::getId)
                .collect(Collectors.toSet());
        assertTrue(ids.contains(firstId));
        assertTrue(ids.contains(secondId));
    }

    @Test
    void endpointsShouldUseDocumentedLedgerPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/app/ledger",
                ClubPointLedgerAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/leader/ledger",
                ClubPointLedgerLeaderController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/ledger",
                ClubPointLedgerAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertGetMapping(ClubPointLedgerAppController.class, "getSummary", new Class<?>[]{}, "/summary", null);
        assertGetMapping(ClubPointLedgerAppController.class, "getTransactionPage",
                new Class<?>[]{AppLedgerTransactionPageReqVO.class}, "/page", null);
        assertGetMapping(ClubPointLedgerLeaderController.class, "getMemberSummaryPage",
                new Class<?>[]{LeaderLedgerMemberSummaryPageReqVO.class}, "/member-summary-page",
                "@ss.hasPermission('clubpoints:leader')");
        assertGetMapping(ClubPointLedgerLeaderController.class, "getTransactionPage",
                new Class<?>[]{LeaderLedgerTransactionPageReqVO.class}, "/transaction-page",
                "@ss.hasPermission('clubpoints:leader')");
        assertGetMapping(ClubPointLedgerAdminController.class, "getAccountPage",
                new Class<?>[]{AdminLedgerAccountPageReqVO.class}, "/account-page",
                "@ss.hasPermission('clubpoints:ledger:query')");
        assertGetMapping(ClubPointLedgerAdminController.class, "getTransactionPage",
                new Class<?>[]{AdminLedgerTransactionPageReqVO.class}, "/transaction-page",
                "@ss.hasPermission('clubpoints:ledger:query')");
    }

    private static void assertGetMapping(Class<?> controllerClass, String methodName, Class<?>[] parameterTypes,
                                         String expectedPath, String expectedPermission) throws NoSuchMethodException {
        Method method = controllerClass.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        if (expectedPermission == null) {
            assertFalse(method.isAnnotationPresent(PreAuthorize.class));
        } else {
            assertNotNull(preAuthorize);
            assertEquals(expectedPermission, preAuthorize.value());
        }
    }

    private Long insertTransaction(String transactionNo, Long userId, String userName, Integer direction,
                                   Integer points, Integer pointCategory, Integer sourceType, Long issuingClubId,
                                   String issuingClubName, String reason) {
        ClubPointTransactionDO transaction = new ClubPointTransactionDO()
                .setTransactionNo(transactionNo)
                .setUserId(userId)
                .setUserNameSnapshot(userName)
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setDirection(direction)
                .setPoints(points)
                .setPointCategory(pointCategory)
                .setPointTypeCode("TEST")
                .setStatus(STATUS_VALID)
                .setSourceType(sourceType)
                .setSourceId(1L)
                .setSourceTitleSnapshot("测试来源")
                .setIssuingClubId(issuingClubId)
                .setIssuingClubCodeSnapshot(issuingClubId == null ? null : "CLUB-" + issuingClubId)
                .setIssuingClubNameSnapshot(issuingClubName)
                .setRuleVersionId(1L)
                .setRuleItemCodeSnapshot("TEST")
                .setEvidenceType(1)
                .setMaterialSummary("材料摘要")
                .setReason(reason)
                .setOccurredAt(LocalDateTime.of(2026, 6, 1, 10, 0).plusMinutes(points))
                .setBusinessYear(2026)
                .setBusinessMonth(202606)
                .setIdempotencyKey("IDEMP-" + transactionNo)
                .setOperatorUserId(900L);
        transactionMapper.insert(transaction);
        return transaction.getId();
    }

    private static ClubPointAccountDO buildAccount(Long userId, Integer totalPositivePoints,
                                                   Integer totalNegativePoints, Integer netPoints,
                                                   Integer frozenPoints, Integer availablePoints,
                                                   Integer annualEarnedPoints) {
        return new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(totalPositivePoints)
                .setTotalNegativePoints(totalNegativePoints)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(annualEarnedPoints)
                .setVersion(1);
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
                .setJoinTime(LocalDateTime.now())
                .setActiveUniqueKey(status == STATUS_ACTIVE ? clubId + ":" + userId : null);
    }

    private static ClubLeaderDO buildLeader(Long userId, Long clubId, Integer status) {
        return new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(userId)
                .setStatus(status)
                .setAssignedTime(LocalDateTime.now())
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

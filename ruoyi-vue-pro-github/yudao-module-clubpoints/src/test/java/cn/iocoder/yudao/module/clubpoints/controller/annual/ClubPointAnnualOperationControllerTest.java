package cn.iocoder.yudao.module.clubpoints.controller.annual;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.ClubPointAnnualAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearingRecordPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearingRecordRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualIncentiveOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualIncentiveSuggestReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingGenerateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.ClubPointBudgetAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.budget.vo.AdminBudgetSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.ClubPointDisputeAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo.AdminDisputeHandleReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo.AdminDisputePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo.AdminDisputeRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.ClubPointDisputeAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo.AppDisputeCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo.AppDisputePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo.AppDisputeRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointIncentiveRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointIncentiveRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.budget.ClubPointBudgetRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.dispute.ClubPointDisputeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointBudgetSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeRelatedActionTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualRankingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointIncentiveServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentService;
import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.budget.ClubPointBudgetServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.dispute.ClubPointDisputeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.BUDGET_DISABLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.INCENTIVE_CONFIRM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointDisputeAppController.class,
        ClubPointDisputeAdminController.class,
        ClubPointAnnualAdminController.class,
        ClubPointBudgetAdminController.class,
        ClubPointDisputeServiceImpl.class,
        ClubPointAnnualClearingServiceImpl.class,
        ClubPointAnnualRankingServiceImpl.class,
        ClubPointIncentiveServiceImpl.class,
        ClubPointBudgetServiceImpl.class,
        ClubScopeServiceImpl.class,
        ClubPointAnnualOperationControllerTest.TestLedgerService.class,
        ClubPointAnnualOperationControllerTest.TestAttachmentService.class,
        ClubPointAnnualOperationControllerTest.TestAuditService.class,
        ClubPointAnnualOperationControllerTest.TestNotifyService.class
})
class ClubPointAnnualOperationControllerTest extends BaseDbUnitTest {

    private static final Long ADMIN_ID = 9001L;
    private static final Long USER_ID = 9101L;
    private static final Long OTHER_USER_ID = 9102L;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 12, 20, 9, 0);

    @Resource
    private ClubPointDisputeAppController disputeAppController;
    @Resource
    private ClubPointDisputeAdminController disputeAdminController;
    @Resource
    private ClubPointAnnualAdminController annualAdminController;
    @Resource
    private ClubPointBudgetAdminController budgetAdminController;
    @Resource
    private ClubPointDisputeMapper disputeMapper;
    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;
    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubPointIncentiveRecordMapper incentiveRecordMapper;
    @Resource
    private ClubPointBudgetRecordMapper budgetRecordMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;

    @BeforeEach
    void setUp() {
        TestLedgerService.reset();
        TestAttachmentService.reset();
        TestAuditService.reset();
        TestNotifyService.reset();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endpointsShouldUseDocumentedAnnualOperationPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/app/dispute",
                ClubPointDisputeAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/dispute",
                ClubPointDisputeAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/annual",
                ClubPointAnnualAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/budget",
                ClubPointBudgetAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertPostMapping(ClubPointDisputeAppController.class, "createDispute",
                new Class<?>[]{AppDisputeCreateReqVO.class}, "/create", null);
        assertGetMapping(ClubPointDisputeAppController.class, "getMyPage",
                new Class<?>[]{AppDisputePageReqVO.class}, "/my-page", null);
        assertGetMapping(ClubPointDisputeAppController.class, "getDispute",
                new Class<?>[]{Long.class}, "/get", null);

        assertGetMapping(ClubPointDisputeAdminController.class, "getDisputePage",
                new Class<?>[]{AdminDisputePageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:dispute:handle')");
        assertPostMapping(ClubPointDisputeAdminController.class, "handleDispute",
                new Class<?>[]{AdminDisputeHandleReqVO.class}, "/handle",
                "@ss.hasPermission('clubpoints:dispute:handle')");

        assertPostMapping(ClubPointAnnualAdminController.class, "clearAnnualPoints",
                new Class<?>[]{AdminAnnualClearReqVO.class}, "/clear",
                "@ss.hasPermission('clubpoints:annual:clear')");
        assertGetMapping(ClubPointAnnualAdminController.class, "getClearingRecordPage",
                new Class<?>[]{AdminAnnualClearingRecordPageReqVO.class}, "/clearing-record-page",
                "@ss.hasPermission('clubpoints:annual:query')");
        assertPostMapping(ClubPointAnnualAdminController.class, "generateRanking",
                new Class<?>[]{AdminAnnualRankingGenerateReqVO.class}, "/ranking-generate",
                "@ss.hasPermission('clubpoints:annual:manage')");
        assertGetMapping(ClubPointAnnualAdminController.class, "getRankingPage",
                new Class<?>[]{AdminAnnualRankingPageReqVO.class}, "/ranking-page",
                "@ss.hasPermission('clubpoints:annual:query')");
        assertPostMapping(ClubPointAnnualAdminController.class, "suggestIncentives",
                new Class<?>[]{AdminAnnualIncentiveSuggestReqVO.class}, "/incentive-suggest",
                "@ss.hasPermission('clubpoints:annual:manage')");
        assertPostMapping(ClubPointAnnualAdminController.class, "confirmIncentive",
                new Class<?>[]{AdminAnnualIncentiveOperationReqVO.class}, "/incentive-confirm",
                "@ss.hasPermission('clubpoints:annual:manage')");
        assertPostMapping(ClubPointAnnualAdminController.class, "cancelIncentive",
                new Class<?>[]{AdminAnnualIncentiveOperationReqVO.class}, "/incentive-cancel",
                "@ss.hasPermission('clubpoints:annual:manage')");

        assertGetMapping(ClubPointBudgetAdminController.class, "getBudgetPage",
                new Class<?>[]{AdminBudgetPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:budget:manage')");
        assertPostMapping(ClubPointBudgetAdminController.class, "createBudget",
                new Class<?>[]{AdminBudgetSaveReqVO.class}, "/create",
                "@ss.hasPermission('clubpoints:budget:manage')");
        assertPutMapping(ClubPointBudgetAdminController.class, "updateBudget",
                new Class<?>[]{AdminBudgetSaveReqVO.class}, "/update",
                "@ss.hasPermission('clubpoints:budget:manage')");
        assertPostMapping(ClubPointBudgetAdminController.class, "disableBudget",
                new Class<?>[]{AdminBudgetOperationReqVO.class}, "/disable",
                "@ss.hasPermission('clubpoints:budget:manage')");
    }

    @Test
    void requestVOsShouldNotExposeOperatorOrCurrentUserFields() {
        assertNoFields(AppDisputeCreateReqVO.class, "userId", "operatorUserId", "operatorGlobalScope");
        assertNoFields(AdminDisputeHandleReqVO.class, "operatorUserId", "operatorNameSnapshot", "operatorRoleSnapshot",
                "operatorGlobalScope");
        assertNoFields(AdminAnnualClearReqVO.class, "operatorUserId", "operatorNameSnapshot", "operatorRoleSnapshot",
                "operatorGlobalScope");
        assertNoFields(AdminAnnualRankingGenerateReqVO.class, "operatorUserId", "operatorGlobalScope");
        assertNoFields(AdminAnnualIncentiveSuggestReqVO.class, "operatorUserId", "operatorGlobalScope");
        assertNoFields(AdminAnnualIncentiveOperationReqVO.class, "operatorUserId", "operatorNameSnapshot",
                "operatorRoleSnapshot", "operatorGlobalScope");
        assertNoFields(AdminBudgetSaveReqVO.class, "operatorUserId", "operatorNameSnapshot", "operatorRoleSnapshot",
                "operatorGlobalScope");
        assertNoFields(AdminBudgetOperationReqVO.class, "operatorUserId", "operatorNameSnapshot",
                "operatorRoleSnapshot", "operatorGlobalScope");
    }

    @Test
    void appDisputeEndpointsShouldUseLoginUserToCreateListAndGetOwnDisputes() {
        login(USER_ID, "Employee");
        Long disputeId = disputeAppController.createDispute(new AppDisputeCreateReqVO()
                .setTargetType(ClubPointDisputeTargetTypeEnum.OTHER.getType())
                .setTargetId(7001L)
                .setContent("points are wrong")).getCheckedData();

        assertEquals(USER_ID, disputeMapper.selectById(disputeId).getUserId());
        assertEquals(ClubPointDisputeStatusEnum.PENDING.getStatus(), disputeMapper.selectById(disputeId).getStatus());

        login(OTHER_USER_ID, "Other");
        disputeAppController.createDispute(new AppDisputeCreateReqVO()
                .setTargetType(ClubPointDisputeTargetTypeEnum.OTHER.getType())
                .setTargetId(7002L)
                .setContent("other dispute")).checkError();

        login(USER_ID, "Employee");
        PageResult<AppDisputeRespVO> myPage = disputeAppController.getMyPage(appDisputePage()).getCheckedData();
        assertEquals(1L, myPage.getTotal());
        assertEquals(disputeId, myPage.getList().get(0).getId());
        assertEquals("points are wrong", myPage.getList().get(0).getContent());

        AppDisputeRespVO detail = disputeAppController.getDispute(disputeId).getCheckedData();
        assertEquals(disputeId, detail.getId());
        assertEquals(USER_ID, detail.getUserId());
    }

    @Test
    void adminDisputeEndpointShouldListAndHandleWithGlobalAudit() {
        login(USER_ID, "Employee");
        Long disputeId = disputeAppController.createDispute(new AppDisputeCreateReqVO()
                .setTargetType(ClubPointDisputeTargetTypeEnum.OTHER.getType())
                .setTargetId(7003L)
                .setContent("need reply")).getCheckedData();

        login(ADMIN_ID, "Admin");
        PageResult<AdminDisputeRespVO> page = disputeAdminController.getDisputePage(adminDisputePage())
                .getCheckedData();
        assertEquals(1L, page.getTotal());
        assertEquals(disputeId, page.getList().get(0).getId());

        disputeAdminController.handleDispute(new AdminDisputeHandleReqVO()
                .setId(disputeId)
                .setReplyContent("checked")
                .setRelatedActionType(ClubPointDisputeRelatedActionTypeEnum.NO_ACTION.getType())
                .setReason("handled by admin")).checkError();

        assertEquals(ClubPointDisputeStatusEnum.REPLIED.getStatus(), disputeMapper.selectById(disputeId).getStatus());
        assertEquals("checked", disputeMapper.selectById(disputeId).getReplyContent());
        assertEquals(1, TestAuditService.requests.size());
        assertEquals("admin", TestAuditService.requests.get(0).getOperatorRoleSnapshot());
        assertEquals(1, TestNotifyService.disputeNotifications.size());
    }

    @Test
    void annualEndpointShouldClearQueryGenerateRankingsAndConfirmIncentives() {
        login(ADMIN_ID, "Admin");
        insertAccount(USER_ID, 120, 20);
        insertPublishedRuleVersion();

        AdminAnnualClearRespVO clearResult = annualAdminController.clearAnnualPoints(new AdminAnnualClearReqVO()
                .setYear(2026)
                .setReason("manual annual clearing")).getCheckedData();
        assertEquals(1, clearResult.getTotalCount());
        assertEquals(1, clearResult.getSuccessCount());
        assertEquals(1L, clearingRecordMapper.selectListByYearAndStatus(2026,
                ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus()).size());

        PageResult<AdminAnnualClearingRecordRespVO> clearingPage =
                annualAdminController.getClearingRecordPage(annualClearingPage()).getCheckedData();
        assertEquals(1L, clearingPage.getTotal());
        assertEquals(100, clearingPage.getList().get(0).getClearablePoints());

        annualAdminController.generateRanking(new AdminAnnualRankingGenerateReqVO()
                .setYear(2026)).checkError();
        ClubPointAnnualRankingRecordDO ranking = insertRankingRecord();

        PageResult<AdminAnnualRankingRespVO> rankingPage =
                annualAdminController.getRankingPage(annualRankingPage()).getCheckedData();
        assertEquals(1L, rankingPage.getTotal());
        assertEquals(ranking.getId(), rankingPage.getList().get(0).getId());
        assertEquals("Running Club", rankingPage.getList().get(0).getClubNameSnapshot());

        Integer suggested = annualAdminController.suggestIncentives(new AdminAnnualIncentiveSuggestReqVO()
                .setYear(2026)).getCheckedData();
        assertEquals(1, suggested);
        ClubPointIncentiveRecordDO incentive = incentiveRecordMapper.selectBySourceTypeAndSourceId(
                ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType(), ranking.getId());
        assertNotNull(incentive);

        annualAdminController.confirmIncentive(new AdminAnnualIncentiveOperationReqVO()
                .setId(incentive.getId())
                .setReason("confirm incentive")).checkError();

        ClubPointIncentiveRecordDO confirmed = incentiveRecordMapper.selectById(incentive.getId());
        assertEquals(ClubPointIncentiveStatusEnum.CONFIRMED.getStatus(), confirmed.getStatus());
        assertEquals(ADMIN_ID, confirmed.getConfirmedBy());
        assertEquals(INCENTIVE_CONFIRM, TestAuditService.requests.get(TestAuditService.requests.size() - 1)
                .getActionType());
    }

    @Test
    void budgetEndpointShouldCreateUpdatePageAndDisableBudgetRecords() {
        login(ADMIN_ID, "Admin");

        Long budgetId = budgetAdminController.createBudget(new AdminBudgetSaveReqVO()
                .setCategory(ClubPointBudgetCategoryEnum.ACTIVITY.getCategory())
                .setBudgetAmountCent(10_000L)
                .setActualAmountCent(8_000L)
                .setOccurDate(LocalDate.of(2026, 6, 1))
                .setSourceType(ClubPointBudgetSourceTypeEnum.MANUAL.getType())
                .setDescription("activity budget")
                .setRemark("first version")
                .setReason("create budget")).getCheckedData();

        budgetAdminController.updateBudget(new AdminBudgetSaveReqVO()
                .setId(budgetId)
                .setCategory(ClubPointBudgetCategoryEnum.OTHER.getCategory())
                .setBudgetAmountCent(12_000L)
                .setActualAmountCent(9_000L)
                .setOccurDate(LocalDate.of(2026, 6, 2))
                .setSourceType(ClubPointBudgetSourceTypeEnum.MANUAL.getType())
                .setDescription("updated budget")
                .setRemark("updated")
                .setReason("update budget")).checkError();

        PageResult<AdminBudgetRespVO> page = budgetAdminController.getBudgetPage(budgetPage()).getCheckedData();
        assertEquals(1L, page.getTotal());
        assertEquals(budgetId, page.getList().get(0).getId());
        assertEquals(12_000L, page.getList().get(0).getBudgetAmountCent());

        budgetAdminController.disableBudget(new AdminBudgetOperationReqVO()
                .setId(budgetId)
                .setReason("disable budget")).checkError();
        assertNull(budgetRecordMapper.selectById(budgetId));
        assertEquals(BUDGET_DISABLE, TestAuditService.requests.get(TestAuditService.requests.size() - 1)
                .getActionType());
    }

    private static AppDisputePageReqVO appDisputePage() {
        AppDisputePageReqVO reqVO = new AppDisputePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminDisputePageReqVO adminDisputePage() {
        AdminDisputePageReqVO reqVO = new AdminDisputePageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminAnnualClearingRecordPageReqVO annualClearingPage() {
        AdminAnnualClearingRecordPageReqVO reqVO = new AdminAnnualClearingRecordPageReqVO()
                .setYear(2026);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminAnnualRankingPageReqVO annualRankingPage() {
        AdminAnnualRankingPageReqVO reqVO = new AdminAnnualRankingPageReqVO()
                .setYear(2026);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminBudgetPageReqVO budgetPage() {
        AdminBudgetPageReqVO reqVO = new AdminBudgetPageReqVO()
                .setYear(2026);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private void insertAccount(Long userId, Integer netPoints, Integer frozenPoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(netPoints)
                .setTotalNegativePoints(0)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(Math.max(netPoints - frozenPoints, 0))
                .setAnnualEarnedPoints(netPoints)
                .setVersion(1));
    }

    private void insertPublishedRuleVersion() {
        ruleVersionMapper.insert(new ClubPointRuleVersionDO()
                .setName("2026 rule")
                .setVersionNo("2026")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setContent("rule")
                .setRemark("rule"));
    }

    private ClubPointAnnualRankingRecordDO insertRankingRecord() {
        ClubPointAnnualRankingRecordDO ranking = new ClubPointAnnualRankingRecordDO()
                .setYear(2026)
                .setClubId(3001L)
                .setClubCodeSnapshot("RUN")
                .setClubNameSnapshot("Running Club")
                .setActivityPoints(80)
                .setContributionPoints(30)
                .setRewardPoints(20)
                .setReversedPoints(0)
                .setTotalIssuedPoints(130)
                .setRankNo(1)
                .setIncentiveAmountCent(200_000L)
                .setConfirmStatus(1)
                .setGeneratedTime(BASE_TIME)
                .setSnapshotJson("{\"year\":2026}");
        rankingRecordMapper.insert(ranking);
        return ranking;
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
            assertNull(preAuthorize);
            return;
        }
        assertNotNull(preAuthorize);
        assertEquals(expectedPermission, preAuthorize.value());
    }

    private static void assertNoFields(Class<?> reqVOClass, String... forbiddenFields) {
        Set<String> fieldNames = Arrays.stream(reqVOClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        for (String forbiddenField : forbiddenFields) {
            assertFalse(fieldNames.contains(forbiddenField), reqVOClass.getSimpleName() + " exposes " + forbiddenField);
        }
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

    static class TestLedgerService implements ClubPointLedgerService {

        static List<ClubPointLedgerCreateReqBO> createRequests = new ArrayList<>();

        static void reset() {
            createRequests = new ArrayList<>();
        }

        @Override
        public Long createTransaction(ClubPointLedgerCreateReqBO reqBO) {
            createRequests.add(reqBO);
            return 50000L + createRequests.size();
        }

        @Override
        public Long reverseTransaction(ClubPointLedgerReverseReqBO reqBO) {
            return 51000L;
        }

        @Override
        public Long adjustPoints(ClubPointLedgerAdjustReqBO reqBO) {
            return 52000L;
        }

        @Override
        public Long rebuildUserAccount(ClubPointAccountRebuildReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long rebuildAllAccounts(ClubPointAccountRebuildAllReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

    }

    static class TestAttachmentService implements ClubAttachmentService {

        static List<ClubAttachmentBindReqBO> binds = new ArrayList<>();

        static void reset() {
            binds = new ArrayList<>();
        }

        @Override
        public Long bindAttachment(ClubAttachmentBindReqBO reqBO) {
            binds.add(reqBO);
            return 60000L + binds.size();
        }

        @Override
        public int lockBizAttachments(String bizType, Long bizId) {
            return 0;
        }

        @Override
        public void validateCanDelete(Long attachmentId) {
        }

        @Override
        public void deleteAttachment(Long attachmentId) {
        }

    }

    static class TestAuditService implements ClubAuditService {

        static List<ClubAuditCreateReqBO> requests = new ArrayList<>();

        static void reset() {
            requests = new ArrayList<>();
        }

        @Override
        public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
            requests.add(reqBO);
            return 70000L + requests.size();
        }

    }

    static class TestNotifyService implements ClubNotifyService {

        static List<String> disputeNotifications = new ArrayList<>();

        static void reset() {
            disputeNotifications = new ArrayList<>();
        }

        @Override
        public void notifyActivityReviewResult(Long userId, String activityTitle, String result, String reason) {
        }

        @Override
        public void notifyPointsChanged(Long userId, String reason, String direction, Integer points,
                                        Integer availablePoints) {
        }

        @Override
        public void notifyRedemptionReviewResult(Long userId, String applicationNo, String result, String reason) {
        }

        @Override
        public void notifyDisputeReplied(Long userId, String title, String replyContent) {
            disputeNotifications.add(userId + ":" + title + ":" + replyContent);
        }

    }

}

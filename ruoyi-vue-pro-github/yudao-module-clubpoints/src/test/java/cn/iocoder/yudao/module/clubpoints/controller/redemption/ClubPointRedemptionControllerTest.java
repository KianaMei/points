package cn.iocoder.yudao.module.clubpoints.controller.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionApplicationAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionBatchAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionGiftAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionEligibilityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionEligibilityRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftStatusReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.ClubPointRedemptionAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplicationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplyReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionBatchPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionBatchRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionCancelReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionGiftPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionGiftRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionGiftMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionReviewResultEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountRebuildReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionApplicationServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionBatchServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionEligibilityServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionGiftServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REDEMPTION_REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointRedemptionAppController.class,
        ClubPointRedemptionBatchAdminController.class,
        ClubPointRedemptionGiftAdminController.class,
        ClubPointRedemptionApplicationAdminController.class,
        ClubPointRedemptionBatchServiceImpl.class,
        ClubPointRedemptionGiftServiceImpl.class,
        ClubPointRedemptionEligibilityServiceImpl.class,
        ClubPointRedemptionApplicationServiceImpl.class,
        ClubPointFreezeServiceImpl.class,
        ClubScopeServiceImpl.class,
        ClubPointRedemptionControllerTest.TestLedgerService.class,
        ClubPointRedemptionControllerTest.TestAuditService.class,
        ClubPointRedemptionControllerTest.TestNotifyService.class
})
class ClubPointRedemptionControllerTest extends BaseDbUnitTest {

    private static final Long ADMIN_ID = 9001L;
    private static final Long USER_ID = 9101L;
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 20, 9, 0);

    @Resource
    private ClubPointRedemptionAppController appController;
    @Resource
    private ClubPointRedemptionBatchAdminController batchAdminController;
    @Resource
    private ClubPointRedemptionGiftAdminController giftAdminController;
    @Resource
    private ClubPointRedemptionApplicationAdminController applicationAdminController;
    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper applicationMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private DeptApi deptApi;

    @BeforeEach
    void setUp() {
        TestAuditService.reset();
        TestNotifyService.reset();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endpointsShouldUseDocumentedRedemptionPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/app/redemption",
                ClubPointRedemptionAppController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/redemption-batch",
                ClubPointRedemptionBatchAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/redemption-gift",
                ClubPointRedemptionGiftAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/redemption-application",
                ClubPointRedemptionApplicationAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertGetMapping(ClubPointRedemptionAppController.class, "getBatchPage",
                new Class<?>[]{AppRedemptionBatchPageReqVO.class}, "/batch-page", null);
        assertGetMapping(ClubPointRedemptionAppController.class, "getGiftPage",
                new Class<?>[]{AppRedemptionGiftPageReqVO.class}, "/gift-page", null);
        assertPostMapping(ClubPointRedemptionAppController.class, "apply",
                new Class<?>[]{AppRedemptionApplyReqVO.class}, "/apply",
                "@ss.hasPermission('clubpoints:redemption:apply')");
        assertPostMapping(ClubPointRedemptionAppController.class, "cancel",
                new Class<?>[]{AppRedemptionCancelReqVO.class}, "/cancel",
                "@ss.hasPermission('clubpoints:redemption:cancel-own')");
        assertGetMapping(ClubPointRedemptionAppController.class, "getMyPage",
                new Class<?>[]{AppRedemptionApplicationPageReqVO.class}, "/my-page", null);

        assertGetMapping(ClubPointRedemptionBatchAdminController.class, "getBatchPage",
                new Class<?>[]{AdminRedemptionBatchPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:redemption-batch:manage')");
        assertPostMapping(ClubPointRedemptionBatchAdminController.class, "createBatch",
                new Class<?>[]{AdminRedemptionBatchSaveReqVO.class}, "/create",
                "@ss.hasPermission('clubpoints:redemption-batch:manage')");
        assertPutMapping(ClubPointRedemptionBatchAdminController.class, "updateBatch",
                new Class<?>[]{AdminRedemptionBatchSaveReqVO.class}, "/update",
                "@ss.hasPermission('clubpoints:redemption-batch:manage')");
        assertPostMapping(ClubPointRedemptionBatchAdminController.class, "openBatch",
                new Class<?>[]{AdminRedemptionBatchOperationReqVO.class}, "/open",
                "@ss.hasPermission('clubpoints:redemption-batch:manage')");
        assertPostMapping(ClubPointRedemptionBatchAdminController.class, "closeBatch",
                new Class<?>[]{AdminRedemptionBatchOperationReqVO.class}, "/close",
                "@ss.hasPermission('clubpoints:redemption-batch:manage')");
        assertGetMapping(ClubPointRedemptionBatchAdminController.class, "getEligibilityPage",
                new Class<?>[]{AdminRedemptionEligibilityPageReqVO.class}, "/eligibility-page",
                "@ss.hasPermission('clubpoints:redemption-batch:manage')");

        assertGetMapping(ClubPointRedemptionGiftAdminController.class, "getGiftPage",
                new Class<?>[]{AdminRedemptionGiftPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:redemption-gift:manage')");
        assertPostMapping(ClubPointRedemptionGiftAdminController.class, "createGift",
                new Class<?>[]{AdminRedemptionGiftSaveReqVO.class}, "/create",
                "@ss.hasPermission('clubpoints:redemption-gift:manage')");
        assertPutMapping(ClubPointRedemptionGiftAdminController.class, "updateGift",
                new Class<?>[]{AdminRedemptionGiftSaveReqVO.class}, "/update",
                "@ss.hasPermission('clubpoints:redemption-gift:manage')");
        assertPostMapping(ClubPointRedemptionGiftAdminController.class, "updateGiftStatus",
                new Class<?>[]{AdminRedemptionGiftStatusReqVO.class}, "/update-status",
                "@ss.hasPermission('clubpoints:redemption-gift:manage')");

        assertGetMapping(ClubPointRedemptionApplicationAdminController.class, "getApplicationPage",
                new Class<?>[]{AdminRedemptionApplicationPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:redemption:review')");
        assertPostMapping(ClubPointRedemptionApplicationAdminController.class, "reviewApplication",
                new Class<?>[]{AdminRedemptionReviewReqVO.class}, "/review",
                "@ss.hasPermission('clubpoints:redemption:review')");
    }

    @Test
    void requestVOsShouldNotExposeFrontendOperatorFields() {
        assertNoOperatorFields(AdminRedemptionBatchSaveReqVO.class);
        assertNoOperatorFields(AdminRedemptionBatchOperationReqVO.class);
        assertNoOperatorFields(AdminRedemptionGiftSaveReqVO.class);
        assertNoOperatorFields(AdminRedemptionGiftStatusReqVO.class);
        assertNoOperatorFields(AdminRedemptionReviewReqVO.class);
        assertNoOperatorFields(AppRedemptionApplyReqVO.class);
        assertNoOperatorFields(AppRedemptionCancelReqVO.class);
    }

    @Test
    void adminRedemptionEndpointsShouldManageBatchGiftAndEligibilitySnapshots() {
        login(ADMIN_ID, "Admin");
        Long batchId = batchAdminController.createBatch(buildBatchSaveReq(null, "M9 API Batch"))
                .getCheckedData();
        batchAdminController.updateBatch(buildBatchSaveReq(batchId, "M9 API Batch Updated")).checkError();
        batchAdminController.openBatch(new AdminRedemptionBatchOperationReqVO()
                .setId(batchId)
                .setReason("open batch")).checkError();

        PageResult<AdminRedemptionBatchRespVO> batchPage = batchAdminController.getBatchPage(pageBatch())
                .getCheckedData();
        assertEquals(1L, batchPage.getTotal());
        assertEquals(batchId, batchPage.getList().get(0).getId());
        assertEquals("M9 API Batch Updated", batchPage.getList().get(0).getName());
        assertEquals(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus(), batchPage.getList().get(0).getStatus());

        Long giftId = giftAdminController.createGift(buildGiftSaveReq(null, batchId, "Gym Bottle"))
                .getCheckedData();
        giftAdminController.updateGift(buildGiftSaveReq(giftId, batchId, "Gym Bottle Pro")).checkError();
        giftAdminController.updateGiftStatus(new AdminRedemptionGiftStatusReqVO()
                .setId(giftId)
                .setStatus(ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus())
                .setReason("on shelf")).checkError();

        PageResult<AdminRedemptionGiftRespVO> giftPage = giftAdminController.getGiftPage(pageGift(batchId))
                .getCheckedData();
        assertEquals(1L, giftPage.getTotal());
        assertEquals(giftId, giftPage.getList().get(0).getId());
        assertEquals("Gym Bottle Pro", giftPage.getList().get(0).getName());
        assertEquals(ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus(), giftPage.getList().get(0).getStatus());

        insertEligibility(batchId, USER_ID, true, 1);
        PageResult<AdminRedemptionEligibilityRespVO> eligibilityPage =
                batchAdminController.getEligibilityPage(pageEligibility(batchId, true)).getCheckedData();
        assertEquals(1L, eligibilityPage.getTotal());
        assertEquals(USER_ID, eligibilityPage.getList().get(0).getUserId());
        assertTrue(eligibilityPage.getList().get(0).getQualified());
    }

    @Test
    void appRedemptionEndpointsShouldUseLoginUserToApplyListAndCancelOwnApplication() {
        ClubPointRedemptionBatchDO batch = insertOpenBatch();
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(), ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        insertAccount(USER_ID, 100, 0);

        login(USER_ID, "Employee");
        PageResult<AppRedemptionBatchRespVO> batchPage = appController.getBatchPage(appBatchPage())
                .getCheckedData();
        assertEquals(1L, batchPage.getTotal());
        assertEquals(batch.getId(), batchPage.getList().get(0).getId());

        PageResult<AppRedemptionGiftRespVO> giftPage = appController.getGiftPage(appGiftPage(batch.getId()))
                .getCheckedData();
        assertEquals(1L, giftPage.getTotal());
        assertEquals(gift.getId(), giftPage.getList().get(0).getId());

        Long applicationId = appController.apply(new AppRedemptionApplyReqVO()
                .setBatchId(batch.getId())
                .setGiftId(gift.getId())
                .setQuantity(1)
                .setRequestNo("M9-API-APP-1")
                .setRemark("apply from app")).getCheckedData();

        ClubPointRedemptionApplicationDO application = applicationMapper.selectById(applicationId);
        assertEquals(USER_ID, application.getUserId());
        assertEquals(ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus(), application.getStatus());
        assertNotNull(application.getFreezeId());
        assertNotNull(application.getStockLockId());

        PageResult<AppRedemptionApplicationRespVO> myPage = appController.getMyPage(appApplicationPage())
                .getCheckedData();
        assertEquals(1L, myPage.getTotal());
        assertEquals(applicationId, myPage.getList().get(0).getId());
        assertEquals("M9 Open Batch", myPage.getList().get(0).getBatchNameSnapshot());
        assertEquals("Sports Towel", myPage.getList().get(0).getGiftNameSnapshot());

        appController.cancel(new AppRedemptionCancelReqVO()
                .setId(applicationId)
                .setReason("changed mind")).checkError();

        ClubPointRedemptionApplicationDO canceled = applicationMapper.selectById(applicationId);
        assertEquals(ClubPointRedemptionApplicationStatusEnum.CANCELED_BEFORE_REVIEW.getStatus(), canceled.getStatus());
        assertEquals("changed mind", canceled.getCancelReason());
        assertEquals(0, accountMapper.selectByUserId(USER_ID).getFrozenPoints());
        assertEquals(100, accountMapper.selectByUserId(USER_ID).getAvailablePoints());
        assertEquals(0, giftMapper.selectById(gift.getId()).getStockLocked());
    }

    @Test
    void appGiftPageShouldReturnEmptyWhenEligibilitySnapshotMissingOrNotQualified() {
        ClubPointRedemptionBatchDO batch = insertOpenBatch();
        insertGift(batch.getId(), ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus());

        login(USER_ID, "Employee");
        PageResult<AppRedemptionGiftRespVO> noSnapshotPage = appController.getGiftPage(appGiftPage(batch.getId()))
                .getCheckedData();
        assertEquals(0L, noSnapshotPage.getTotal());
        assertTrue(noSnapshotPage.getList().isEmpty());

        insertEligibility(batch.getId(), USER_ID, false, 101);
        PageResult<AppRedemptionGiftRespVO> notQualifiedPage = appController.getGiftPage(appGiftPage(batch.getId()))
                .getCheckedData();
        assertEquals(0L, notQualifiedPage.getTotal());
        assertTrue(notQualifiedPage.getList().isEmpty());
    }

    @Test
    void adminApplicationEndpointShouldListAndReviewWithGlobalPermissionBoundary() {
        ClubPointRedemptionBatchDO batch = insertOpenBatch();
        ClubPointRedemptionGiftDO gift = insertGift(batch.getId(), ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus());
        insertEligibility(batch.getId(), USER_ID, true, 1);
        insertAccount(USER_ID, 100, 0);
        login(USER_ID, "Employee");
        Long applicationId = appController.apply(new AppRedemptionApplyReqVO()
                .setBatchId(batch.getId())
                .setGiftId(gift.getId())
                .setQuantity(1)
                .setRequestNo("M9-API-REVIEW-1")).getCheckedData();

        login(ADMIN_ID, "Admin");
        PageResult<AdminRedemptionApplicationRespVO> applicationPage =
                applicationAdminController.getApplicationPage(adminApplicationPage()).getCheckedData();
        assertEquals(1L, applicationPage.getTotal());
        assertEquals(applicationId, applicationPage.getList().get(0).getId());

        applicationAdminController.reviewApplication(new AdminRedemptionReviewReqVO()
                .setId(applicationId)
                .setResult(ClubPointRedemptionReviewResultEnum.REJECTED.getResult())
                .setReason("gift damaged")).checkError();

        ClubPointRedemptionApplicationDO reviewed = applicationMapper.selectById(applicationId);
        assertEquals(ClubPointRedemptionApplicationStatusEnum.REJECTED.getStatus(), reviewed.getStatus());
        assertEquals(ADMIN_ID, reviewed.getReviewerUserId());
        assertEquals("gift damaged", reviewed.getReviewReason());
        assertEquals(1, TestAuditService.requests.size());
        assertEquals(REDEMPTION_REVIEW, TestAuditService.requests.get(0).getActionType());
        assertEquals("admin", TestAuditService.requests.get(0).getOperatorRoleSnapshot());
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

    private static void assertNoOperatorFields(Class<?> reqVOClass) {
        Set<String> fieldNames = Arrays.stream(reqVOClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertFalse(fieldNames.contains("operatorUserId"));
        assertFalse(fieldNames.contains("operatorNameSnapshot"));
        assertFalse(fieldNames.contains("operatorRoleSnapshot"));
        assertFalse(fieldNames.contains("operatorGlobalScope"));
    }

    private static AdminRedemptionBatchSaveReqVO buildBatchSaveReq(Long id, String name) {
        return new AdminRedemptionBatchSaveReqVO()
                .setId(id)
                .setYear(2026)
                .setName(name)
                .setOpenTime(BASE_TIME)
                .setCloseTime(BASE_TIME.plusDays(10))
                .setDescription("batch desc")
                .setQualificationRule("min 50 top 100")
                .setMinAvailablePoints(50)
                .setQualifiedCount(100)
                .setIncludeTieAtCutoff(true)
                .setRuleVersionId(8001L)
                .setRuleSnapshotJson("{\"rule\":\"redemption\"}")
                .setReason("save batch");
    }

    private static AdminRedemptionGiftSaveReqVO buildGiftSaveReq(Long id, Long batchId, String name) {
        return new AdminRedemptionGiftSaveReqVO()
                .setId(id)
                .setBatchId(batchId)
                .setName(name)
                .setDescription("gift desc")
                .setPointsCost(60)
                .setTierMinPoints(50)
                .setTierMaxPoints(200)
                .setReferenceAmountCent(1999L)
                .setStockTotal(5)
                .setImageFileId(7001L)
                .setSort(1)
                .setReason("save gift");
    }

    private static AdminRedemptionBatchPageReqVO pageBatch() {
        AdminRedemptionBatchPageReqVO reqVO = new AdminRedemptionBatchPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminRedemptionGiftPageReqVO pageGift(Long batchId) {
        AdminRedemptionGiftPageReqVO reqVO = new AdminRedemptionGiftPageReqVO()
                .setBatchId(batchId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminRedemptionEligibilityPageReqVO pageEligibility(Long batchId, Boolean qualified) {
        AdminRedemptionEligibilityPageReqVO reqVO = new AdminRedemptionEligibilityPageReqVO()
                .setBatchId(batchId)
                .setQualified(qualified);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AppRedemptionBatchPageReqVO appBatchPage() {
        AppRedemptionBatchPageReqVO reqVO = new AppRedemptionBatchPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AppRedemptionGiftPageReqVO appGiftPage(Long batchId) {
        AppRedemptionGiftPageReqVO reqVO = new AppRedemptionGiftPageReqVO()
                .setBatchId(batchId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AppRedemptionApplicationPageReqVO appApplicationPage() {
        AppRedemptionApplicationPageReqVO reqVO = new AppRedemptionApplicationPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminRedemptionApplicationPageReqVO adminApplicationPage() {
        AdminRedemptionApplicationPageReqVO reqVO = new AdminRedemptionApplicationPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private ClubPointRedemptionBatchDO insertOpenBatch() {
        ClubPointRedemptionBatchDO batch = new ClubPointRedemptionBatchDO()
                .setYear(2026)
                .setName("M9 Open Batch")
                .setStatus(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus())
                .setOpenTime(BASE_TIME.minusDays(1))
                .setCloseTime(BASE_TIME.plusDays(9))
                .setDescription("batch desc")
                .setMinAvailablePoints(50)
                .setQualifiedCount(100)
                .setIncludeTieAtCutoff(true)
                .setQualificationRuleJson("{\"qualificationRule\":\"min 50 top 100\"}")
                .setSnapshotGenerated(true)
                .setSnapshotGeneratedTime(BASE_TIME)
                .setRuleVersionId(8001L)
                .setRuleSnapshotJson("{\"rule\":\"redemption\"}");
        batchMapper.insert(batch);
        return batch;
    }

    private ClubPointRedemptionGiftDO insertGift(Long batchId, Integer status) {
        ClubPointRedemptionGiftDO gift = new ClubPointRedemptionGiftDO()
                .setBatchId(batchId)
                .setName("Sports Towel")
                .setDescription("gift desc")
                .setPointsCost(60)
                .setTierMinPoints(50)
                .setTierMaxPoints(200)
                .setReferenceAmountCent(1999L)
                .setStockTotal(5)
                .setStockLocked(0)
                .setStockUsed(0)
                .setStatus(status)
                .setImageFileId(7001L)
                .setSort(1)
                .setGiftSnapshotJson("{\"name\":\"Sports Towel\"}");
        giftMapper.insert(gift);
        return gift;
    }

    private void insertEligibility(Long batchId, Long userId, Boolean qualified, Integer rankNo) {
        eligibilitySnapshotMapper.insert(new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(batchId)
                .setUserId(userId)
                .setUserNameSnapshot("Employee " + userId)
                .setDeptNameSnapshot("Operations")
                .setNetPointsSnapshot(100)
                .setFrozenPointsSnapshot(0)
                .setAvailablePointsSnapshot(100)
                .setAnnualEarnedPointsSnapshot(100)
                .setRankNo(rankNo)
                .setQualified(qualified)
                .setQualificationReason(Boolean.TRUE.equals(qualified) ? "qualified" : "not qualified")
                .setTieAtCutoff(false)
                .setGeneratedTime(BASE_TIME));
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

        @Override
        public Long createTransaction(ClubPointLedgerCreateReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long reverseTransaction(ClubPointLedgerReverseReqBO reqBO) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long adjustPoints(ClubPointLedgerAdjustReqBO reqBO) {
            throw new UnsupportedOperationException();
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

    static class TestAuditService implements ClubAuditService {

        static List<ClubAuditCreateReqBO> requests = new ArrayList<>();

        static void reset() {
            requests = new ArrayList<>();
        }

        @Override
        public Long createAuditLog(ClubAuditCreateReqBO reqBO) {
            requests.add(reqBO);
            return 10000L + requests.size();
        }

    }

    static class TestNotifyService implements ClubNotifyService {

        static List<String> redemptionNotifications = new ArrayList<>();

        static void reset() {
            redemptionNotifications = new ArrayList<>();
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
            redemptionNotifications.add(userId + ":" + applicationNo + ":" + result + ":" + reason);
        }

        @Override
        public void notifyDisputeReplied(Long userId, String title, String replyContent) {
        }

    }

}

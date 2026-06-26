package cn.iocoder.yudao.module.clubpoints.controller.contribution;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.ClubPointContributionAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionDirectCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionFraudHandleReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionReviewPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo.AdminContributionViolationDeductReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.ClubPointContributionLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionItemReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo.LeaderContributionSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubPointClubMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.contribution.ClubPointContributionServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.service.notify.NotifySendService;
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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAttachmentConstants.ATTACHMENT_TYPE_URL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_DIRECT_CREATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_FRAUD_HANDLE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_REVIEW;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.CONTRIBUTION_VIOLATION_DEDUCT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum.ACTIVE_CONTRIBUTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum.DEDUCTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.PUBLICITY_SUGGESTION;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialTypeEnum.VIOLATION_DEDUCT;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum.DECREASE;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum.REVERSAL;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Import({
        ClubPointContributionLeaderController.class,
        ClubPointContributionAdminController.class,
        ClubPointContributionServiceImpl.class,
        ClubScopeServiceImpl.class,
        ClubAttachmentServiceImpl.class,
        ClubPointRuleServiceImpl.class,
        ClubAuditServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubNotifyServiceImpl.class
})
class ClubPointContributionControllerTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 7, 20, 9, 0);

    @Resource
    private ClubPointContributionLeaderController leaderController;
    @Resource
    private ClubPointContributionAdminController adminController;
    @Resource
    private ClubPointContributionMaterialMapper materialMapper;
    @Resource
    private ClubPointContributionItemMapper itemMapper;
    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @MockBean
    private FileService fileService;
    @MockBean
    private NotifySendService notifySendService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void leaderContributionEndpointsShouldSubmitWithdrawAndQueryManagedClubMaterials() {
        login(8201L, "负责人8201");
        ClubPointRuleVersionDO ruleVersion = seedContributionRules();
        ClubPointClubDO managedClub = insertClub("CLUB-M8-API-1", "M8 API Managed Club");
        ClubPointClubDO otherClub = insertClub("CLUB-M8-API-2", "M8 API Other Club");
        insertLeader(managedClub, 8201L, "负责人8201");

        Long materialId = leaderController.createContribution(buildLeaderSaveReq(
                managedClub.getId(), null, ruleVersion.getId(), "负责人材料")).getCheckedData();
        leaderController.updateContribution(buildLeaderSaveReq(managedClub.getId(), materialId,
                ruleVersion.getId(), "负责人材料已修改"));
        leaderController.submitContribution(materialId);

        ClubPointContributionMaterialDO submitted = materialMapper.selectById(materialId);
        assertEquals(ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus(), submitted.getStatus());
        assertEquals("负责人材料已修改", submitted.getTitle());
        assertEquals(1L, itemMapper.selectListByMaterialId(materialId).size());

        PageResult<LeaderContributionRespVO> page = leaderController.getContributionPage(
                buildLeaderPageReq(managedClub.getId())).getCheckedData();
        assertEquals(1L, page.getTotal());
        assertEquals(materialId, page.getList().get(0).getId());
        assertEquals(managedClub.getId(), page.getList().get(0).getClubId());

        LeaderContributionRespVO detail = leaderController.getContribution(materialId).getCheckedData();
        assertEquals(materialId, detail.getId());
        assertEquals(1, detail.getItems().size());
        assertEquals(8301L, detail.getItems().get(0).getUserId());

        Long withdrawId = leaderController.createContribution(buildLeaderSaveReq(
                managedClub.getId(), null, ruleVersion.getId(), "待撤回材料")).getCheckedData();
        leaderController.submitContribution(withdrawId);
        leaderController.withdrawContribution(withdrawId, "补充材料后重提");
        assertEquals(ClubPointContributionMaterialStatusEnum.WITHDRAWN.getStatus(),
                materialMapper.selectById(withdrawId).getStatus());

        assertServiceException(() -> leaderController.createContribution(buildLeaderSaveReq(
                otherClub.getId(), null, ruleVersion.getId(), "越权材料")), CLUB_SCOPE_DENIED);
    }

    @Test
    void adminContributionEndpointsShouldReviewDirectViolationAndFraud() {
        login(1L, "管理员");
        ClubPointRuleVersionDO ruleVersion = seedContributionRules();
        ClubPointClubDO club = insertClub("CLUB-M8-API-3", "M8 API Admin Club");
        insertLeader(club, 8202L, "负责人8202");

        login(8202L, "负责人8202");
        Long reviewMaterialId = leaderController.createContribution(buildLeaderSaveReq(
                club.getId(), null, ruleVersion.getId(), "审核材料")).getCheckedData();
        leaderController.submitContribution(reviewMaterialId);

        login(1L, "管理员");
        PageResult<AdminContributionRespVO> reviewPage = adminController.getReviewPage(
                buildAdminReviewPageReq(club.getId())).getCheckedData();
        assertEquals(1L, reviewPage.getTotal());
        assertEquals(reviewMaterialId, reviewPage.getList().get(0).getId());
        assertEquals(1, adminController.getContribution(reviewMaterialId).getCheckedData().getItems().size());

        adminController.reviewContribution(new AdminContributionReviewReqVO()
                .setId(reviewMaterialId)
                .setResult(1)
                .setReason("审核通过"));
        ClubPointContributionItemDO reviewedItem = itemMapper.selectListByMaterialId(reviewMaterialId).get(0);
        assertNotNull(reviewedItem.getTransactionId());

        Long directMaterialId = adminController.directCreate(new AdminContributionDirectCreateReqVO()
                .setRequestNo("M8-API-DIRECT-1")
                .setClubId(club.getId())
                .setType(PUBLICITY_SUGGESTION.getType())
                .setUserId(8302L)
                .setUserNameSnapshot("员工8302")
                .setDeptNameSnapshot("Operations")
                .setPoints(20)
                .setRuleVersionId(ruleVersion.getId())
                .setReason("管理员代录")
                .setAttachments(Arrays.asList(urlAttachment("direct")))).getCheckedData();
        assertEquals(20, accountMapper.selectByUserId(8302L).getAvailablePoints());

        Long violationMaterialId = adminController.violationDeduct(new AdminContributionViolationDeductReqVO()
                .setRequestNo("M8-API-VIOLATION-1")
                .setClubId(club.getId())
                .setUserId(8302L)
                .setUserNameSnapshot("员工8302")
                .setDeptNameSnapshot("Operations")
                .setPoints(7)
                .setRuleVersionId(ruleVersion.getId())
                .setReason("违规扣分")
                .setAttachments(Arrays.asList(urlAttachment("violation")))).getCheckedData();
        assertEquals(VIOLATION_DEDUCT.getType(), materialMapper.selectById(violationMaterialId).getType());
        assertEquals(13, accountMapper.selectByUserId(8302L).getAvailablePoints());

        Long fraudOriginalMaterialId = adminController.directCreate(new AdminContributionDirectCreateReqVO()
                .setRequestNo("M8-API-FRAUD-ORIGINAL")
                .setClubId(club.getId())
                .setType(PUBLICITY_SUGGESTION.getType())
                .setUserId(8303L)
                .setUserNameSnapshot("员工8303")
                .setDeptNameSnapshot("Operations")
                .setPoints(15)
                .setRuleVersionId(ruleVersion.getId())
                .setReason("原始虚假积分")
                .setAttachments(Arrays.asList(urlAttachment("fraud-original")))).getCheckedData();
        adminController.directCreate(new AdminContributionDirectCreateReqVO()
                .setRequestNo("M8-API-FRAUD-BONUS")
                .setClubId(club.getId())
                .setType(PUBLICITY_SUGGESTION.getType())
                .setUserId(8303L)
                .setUserNameSnapshot("员工8303")
                .setDeptNameSnapshot("Operations")
                .setPoints(5)
                .setRuleVersionId(ruleVersion.getId())
                .setReason("剩余可用积分")
                .setAttachments(Arrays.asList(urlAttachment("fraud-bonus"))));
        ClubPointContributionItemDO originalItem = itemMapper.selectListByMaterialId(fraudOriginalMaterialId).get(0);

        Long fraudMaterialId = adminController.handleFraud(new AdminContributionFraudHandleReqVO()
                .setRequestNo("M8-API-FRAUD-HANDLE")
                .setOriginalMaterialId(fraudOriginalMaterialId)
                .setRuleVersionId(ruleVersion.getId())
                .setReason("弄虚作假")
                .setAttachments(Arrays.asList(urlAttachment("fraud-handle")))).getCheckedData();

        ClubPointContributionMaterialDO fraudMaterial = materialMapper.selectById(fraudMaterialId);
        assertEquals(ClubPointContributionMaterialTypeEnum.FRAUD_HANDLE.getType(), fraudMaterial.getType());
        ClubPointTransactionDO reverseTransaction = transactionMapper.selectByReverseOfTransactionId(
                originalItem.getTransactionId());
        assertNotNull(reverseTransaction);
        assertEquals(REVERSAL.getType(), reverseTransaction.getSourceType());
        assertEquals(DECREASE.getDirection(), reverseTransaction.getDirection());
        ClubPointAccountDO fraudAccount = accountMapper.selectByUserId(8303L);
        assertEquals(0, fraudAccount.getAvailablePoints());

        assertEquals(directMaterialId, adminController.directCreate(new AdminContributionDirectCreateReqVO()
                .setRequestNo("M8-API-DIRECT-1")
                .setClubId(club.getId())
                .setType(PUBLICITY_SUGGESTION.getType())
                .setUserId(8302L)
                .setUserNameSnapshot("员工8302")
                .setDeptNameSnapshot("Operations")
                .setPoints(20)
                .setRuleVersionId(ruleVersion.getId())
                .setReason("管理员代录")
                .setAttachments(Arrays.asList(urlAttachment("direct-repeat")))).getCheckedData());

        Set<String> auditActions = auditLogMapper.selectList().stream()
                .map(ClubAuditLogDO::getActionType)
                .collect(Collectors.toSet());
        assertTrue(auditActions.contains(CONTRIBUTION_REVIEW));
        assertTrue(auditActions.contains(CONTRIBUTION_DIRECT_CREATE));
        assertTrue(auditActions.contains(CONTRIBUTION_VIOLATION_DEDUCT));
        assertTrue(auditActions.contains(CONTRIBUTION_FRAUD_HANDLE));
    }

    @Test
    void endpointsShouldUseDocumentedContributionPathsAndPermissions() throws Exception {
        assertEquals("/clubpoints/leader/contribution",
                ClubPointContributionLeaderController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertEquals("/clubpoints/contribution",
                ClubPointContributionAdminController.class.getAnnotation(RequestMapping.class).value()[0]);

        assertGetMapping(ClubPointContributionLeaderController.class, "getContributionPage",
                new Class<?>[]{LeaderContributionPageReqVO.class}, "/page",
                "@ss.hasPermission('clubpoints:contribution:query')");
        assertGetMapping(ClubPointContributionLeaderController.class, "getContribution",
                new Class<?>[]{Long.class}, "/get",
                "@ss.hasPermission('clubpoints:contribution:query')");
        assertPostMapping(ClubPointContributionLeaderController.class, "createContribution",
                new Class<?>[]{LeaderContributionSaveReqVO.class}, "/create",
                "@ss.hasPermission('clubpoints:contribution:submit')");
        assertPutMapping(ClubPointContributionLeaderController.class, "updateContribution",
                new Class<?>[]{LeaderContributionSaveReqVO.class}, "/update",
                "@ss.hasPermission('clubpoints:contribution:submit')");
        assertPostMapping(ClubPointContributionLeaderController.class, "submitContribution",
                new Class<?>[]{Long.class}, "/submit",
                "@ss.hasPermission('clubpoints:contribution:submit')");
        assertPostMapping(ClubPointContributionLeaderController.class, "withdrawContribution",
                new Class<?>[]{Long.class, String.class}, "/withdraw",
                "@ss.hasPermission('clubpoints:contribution:withdraw')");
        assertGetMapping(ClubPointContributionAdminController.class, "getReviewPage",
                new Class<?>[]{AdminContributionReviewPageReqVO.class}, "/review-page",
                "@ss.hasPermission('clubpoints:contribution:review')");
        assertGetMapping(ClubPointContributionAdminController.class, "getContribution",
                new Class<?>[]{Long.class}, "/get",
                "@ss.hasPermission('clubpoints:contribution:review')");
        assertPostMapping(ClubPointContributionAdminController.class, "reviewContribution",
                new Class<?>[]{AdminContributionReviewReqVO.class}, "/review",
                "@ss.hasPermission('clubpoints:contribution:review')");
        assertPostMapping(ClubPointContributionAdminController.class, "directCreate",
                new Class<?>[]{AdminContributionDirectCreateReqVO.class}, "/direct-create",
                "@ss.hasPermission('clubpoints:contribution:direct-create')");
        assertPostMapping(ClubPointContributionAdminController.class, "violationDeduct",
                new Class<?>[]{AdminContributionViolationDeductReqVO.class}, "/violation-deduct",
                "@ss.hasPermission('clubpoints:contribution:violation-deduct')");
        assertPostMapping(ClubPointContributionAdminController.class, "handleFraud",
                new Class<?>[]{AdminContributionFraudHandleReqVO.class}, "/fraud-handle",
                "@ss.hasPermission('clubpoints:contribution:fraud-handle')");
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
        assertNotNull(preAuthorize);
        assertEquals(expectedPermission, preAuthorize.value());
    }

    private static LeaderContributionPageReqVO buildLeaderPageReq(Long clubId) {
        LeaderContributionPageReqVO reqVO = new LeaderContributionPageReqVO()
                .setClubId(clubId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static AdminContributionReviewPageReqVO buildAdminReviewPageReq(Long clubId) {
        AdminContributionReviewPageReqVO reqVO = new AdminContributionReviewPageReqVO()
                .setClubId(clubId);
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        return reqVO;
    }

    private static LeaderContributionSaveReqVO buildLeaderSaveReq(Long clubId, Long id, Long ruleVersionId,
                                                                  String title) {
        return new LeaderContributionSaveReqVO()
                .setId(id)
                .setClubId(clubId)
                .setType(PUBLICITY_SUGGESTION.getType())
                .setTitle(title)
                .setDescription("材料说明")
                .setRuleVersionId(ruleVersionId)
                .setItems(Arrays.asList(new LeaderContributionItemReqVO()
                        .setUserId(8301L)
                        .setUserNameSnapshot("员工8301")
                        .setDeptNameSnapshot("Operations")
                        .setPoints(6)
                        .setReason("宣传建议")
                        .setMaterialSummary("summary")))
                .setAttachments(Arrays.asList(urlAttachment(title)));
    }

    private static AttachmentInputVO urlAttachment(String name) {
        return new AttachmentInputVO()
                .setType(ATTACHMENT_TYPE_URL)
                .setUrl("https://example.invalid/" + name)
                .setName(name);
    }

    private ClubPointRuleVersionDO seedContributionRules() {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("M8-API-RULE-001")
                .setName("M8 API rules")
                .setStatus(ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus())
                .setEffectiveTime(BASE_TIME.minusDays(1))
                .setPublishedTime(BASE_TIME.minusDays(1));
        ruleVersionMapper.insert(version);
        insertPointRuleItem(version.getId(), PUBLICITY_SUGGESTION.getRuleItemCode(), "Publicity",
                ACTIVE_CONTRIBUTION.getCategory(), 30, 1);
        insertPointRuleItem(version.getId(), VIOLATION_DEDUCT.getRuleItemCode(), "Violation",
                DEDUCTION.getCategory(), 10, 2);
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(version.getId())
                .setItemCode(ClubPointRuleItemCodeEnum.FRAUD_CLEAR_ALL.getCode())
                .setItemName("Fraud clear all")
                .setItemType(ClubPointRuleItemTypeEnum.SWITCH.getType())
                .setCategory(DEDUCTION.getCategory())
                .setIntValue(1)
                .setStatus(1)
                .setSort(3));
        return version;
    }

    private void insertPointRuleItem(Long versionId, String code, String name, Integer category,
                                     Integer maxPoints, Integer sort) {
        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(versionId)
                .setItemCode(code)
                .setItemName(name)
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(maxPoints)
                .setStatus(1)
                .setSort(sort));
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

    private void insertLeader(ClubPointClubDO club, Long userId, String userName) {
        leaderMapper.insert(new ClubLeaderDO()
                .setClubId(club.getId())
                .setUserId(userId)
                .setStatus(ClubPointLeaderStatusEnum.ACTIVE.getStatus())
                .setAssignedTime(BASE_TIME.minusDays(1))
                .setAssignedBy(1L)
                .setReason("assign")
                .setClubNameSnapshot(club.getName())
                .setUserNameSnapshot(userName)
                .setActiveUniqueKey(club.getId() + ":" + userId));
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

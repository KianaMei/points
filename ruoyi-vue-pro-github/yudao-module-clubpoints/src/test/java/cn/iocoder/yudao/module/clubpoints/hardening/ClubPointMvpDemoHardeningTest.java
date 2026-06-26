package cn.iocoder.yudao.module.clubpoints.hardening;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.ClubPointActivityAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminActivityReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.ClubPointAnnualAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearingRecordPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualClearingRecordRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingGenerateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo.AdminAnnualRankingRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.ClubPointAuditAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo.AdminAuditPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo.AdminAuditRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.ClubPointClubAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.ClubPointClubLeaderAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.ClubPointClubMemberAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubMemberSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionApplicationAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionBatchAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.ClubPointRedemptionGiftAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionApplicationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionBatchSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftStatusReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionReviewReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.ClubPointReportAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.ClubPointRuleAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleItemSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.RuleVersionSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.ClubPointSettlementAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.ClubPointAttendanceAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.ClubPointRegistrationAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppAttendanceCheckReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.ClubPointLedgerAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.ClubPointRedemptionAppController;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplicationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplicationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo.AppRedemptionApplyReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.ClubPointActivityLeaderController;
import cn.iocoder.yudao.module.clubpoints.controller.leader.activity.vo.LeaderActivitySaveReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionGiftStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionReviewResultEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointReportExportTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointRegistrationServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualRankingServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointIncentiveServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.attachment.ClubAttachmentServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointLeaderServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointMemberServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointFreezeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.notify.ClubNotifyService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionApplicationServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionBatchServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionEligibilityServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionGiftServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.report.ClubPointReportServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementAdminServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementJobService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementServiceImpl;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Import({
        ClubPointRuleAdminController.class,
        ClubPointClubAdminController.class,
        ClubPointClubMemberAdminController.class,
        ClubPointClubLeaderAdminController.class,
        ClubPointActivityLeaderController.class,
        ClubPointActivityAdminController.class,
        ClubPointRegistrationAppController.class,
        ClubPointAttendanceAppController.class,
        ClubPointLedgerAppController.class,
        ClubPointSettlementAdminController.class,
        ClubPointRedemptionBatchAdminController.class,
        ClubPointRedemptionGiftAdminController.class,
        ClubPointRedemptionApplicationAdminController.class,
        ClubPointRedemptionAppController.class,
        ClubPointAnnualAdminController.class,
        ClubPointAuditAdminController.class,
        ClubPointReportAdminController.class,
        ClubPointRuleServiceImpl.class,
        ClubPointClubQueryServiceImpl.class,
        ClubPointClubServiceImpl.class,
        ClubPointMemberServiceImpl.class,
        ClubPointLeaderServiceImpl.class,
        ClubPointActivityQueryServiceImpl.class,
        ClubPointActivityServiceImpl.class,
        ClubPointRegistrationServiceImpl.class,
        ClubPointAttendanceServiceImpl.class,
        ClubPointActivitySettlementAdminServiceImpl.class,
        ClubPointActivitySettlementJobService.class,
        ClubPointActivitySettlementServiceImpl.class,
        ClubPointLedgerServiceImpl.class,
        ClubPointLedgerQueryServiceImpl.class,
        ClubPointFreezeServiceImpl.class,
        ClubPointRedemptionBatchServiceImpl.class,
        ClubPointRedemptionGiftServiceImpl.class,
        ClubPointRedemptionEligibilityServiceImpl.class,
        ClubPointRedemptionApplicationServiceImpl.class,
        ClubPointAnnualClearingServiceImpl.class,
        ClubPointAnnualRankingServiceImpl.class,
        ClubPointIncentiveServiceImpl.class,
        ClubAuditServiceImpl.class,
        ClubAttachmentServiceImpl.class,
        ClubScopeServiceImpl.class,
        ClubPointReportServiceImpl.class
})
class ClubPointMvpDemoHardeningTest extends BaseDbUnitTest {

    private static final long ADMIN_ID = 1L;
    private static final long EMPLOYEE_ID = 1001L;
    private static final long LEADER_ID = 9001L;
    private static final int YEAR = 2026;

    @Resource
    private ClubPointRuleAdminController ruleAdminController;
    @Resource
    private ClubPointClubAdminController clubAdminController;
    @Resource
    private ClubPointClubMemberAdminController clubMemberAdminController;
    @Resource
    private ClubPointClubLeaderAdminController clubLeaderAdminController;
    @Resource
    private ClubPointActivityLeaderController activityLeaderController;
    @Resource
    private ClubPointActivityAdminController activityAdminController;
    @Resource
    private ClubPointRegistrationAppController registrationAppController;
    @Resource
    private ClubPointAttendanceAppController attendanceAppController;
    @Resource
    private ClubPointSettlementAdminController settlementAdminController;
    @Resource
    private ClubPointLedgerAppController ledgerAppController;
    @Resource
    private ClubPointRedemptionBatchAdminController redemptionBatchAdminController;
    @Resource
    private ClubPointRedemptionGiftAdminController redemptionGiftAdminController;
    @Resource
    private ClubPointRedemptionApplicationAdminController redemptionApplicationAdminController;
    @Resource
    private ClubPointRedemptionAppController redemptionAppController;
    @Resource
    private ClubPointAnnualAdminController annualAdminController;
    @Resource
    private ClubPointAuditAdminController auditAdminController;
    @Resource
    private ClubPointReportAdminController reportAdminController;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper redemptionApplicationMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private DeptApi deptApi;
    @MockBean
    private ClubNotifyService clubNotifyService;
    @MockBean
    private FileService fileService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mvpDemoFlowShouldRunThroughApisWithoutManualDatabaseMutation() throws Exception {
        mockExternalUsers();
        login(ADMIN_ID, "管理员");
        Long ruleVersionId = createAndPublishDemoRules();
        Long clubId = createClubMemberAndLeader();

        login(LEADER_ID, "负责人9001");
        Long activityId = activityLeaderController.createActivity(buildLeaderActivitySaveReq(clubId, ruleVersionId))
                .getCheckedData();
        activityLeaderController.submitActivity(activityId).checkError();

        login(ADMIN_ID, "管理员");
        activityAdminController.reviewActivity(new AdminActivityReviewReqVO()
                .setId(activityId)
                .setApproved(true)
                .setReason("M12.7 管理员审核通过")).checkError();
        assertEquals(ClubPointActivityStatusEnum.PUBLISHED.getStatus(), activityMapper.selectById(activityId).getStatus());

        login(EMPLOYEE_ID, "员工1001");
        Long registrationId = registrationAppController.createRegistration(new AppRegistrationCreateReqVO()
                .setActivityId(activityId)).getCheckedData();
        attendanceAppController.checkIn(new AppAttendanceCheckReqVO()
                .setRegistrationId(registrationId)
                .setRemark("M12.7 签到")).checkError();
        attendanceAppController.checkOut(new AppAttendanceCheckReqVO()
                .setRegistrationId(registrationId)
                .setRemark("M12.7 签退")).checkError();

        login(ADMIN_ID, "管理员");
        settlementAdminController.runSettlement(new AdminSettlementRunReqVO()
                .setActivityId(activityId)
                .setForce(true)
                .setReason("M12.7 强制结算演示活动")).getCheckedData();
        assertEquals(ClubPointActivityStatusEnum.SETTLED.getStatus(), activityMapper.selectById(activityId).getStatus());

        login(EMPLOYEE_ID, "员工1001");
        AppLedgerSummaryRespVO earnedSummary = ledgerAppController.getSummary().getCheckedData();
        assertEquals(10, earnedSummary.getAvailablePoints());
        PageResult<AppLedgerTransactionRespVO> earnedTransactions =
                ledgerAppController.getTransactionPage(appLedgerPage()).getCheckedData();
        assertEquals(2L, earnedTransactions.getTotal());
        assertTrue(earnedTransactions.getList().stream()
                .anyMatch(item -> ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType()
                        .equals(item.getSourceType())));

        login(ADMIN_ID, "管理员");
        Long batchId = redemptionBatchAdminController.createBatch(buildRedemptionBatchSaveReq(ruleVersionId))
                .getCheckedData();
        redemptionBatchAdminController.openBatch(new AdminRedemptionBatchOperationReqVO()
                .setId(batchId)
                .setReason("M12.7 开启兑换批次")).checkError();
        Long giftId = redemptionGiftAdminController.createGift(buildRedemptionGiftSaveReq(batchId)).getCheckedData();
        redemptionGiftAdminController.updateGiftStatus(new AdminRedemptionGiftStatusReqVO()
                .setId(giftId)
                .setStatus(ClubPointRedemptionGiftStatusEnum.ON_SHELF.getStatus())
                .setReason("M12.7 礼品上架")).checkError();

        login(EMPLOYEE_ID, "员工1001");
        Long applicationId = redemptionAppController.apply(new AppRedemptionApplyReqVO()
                .setBatchId(batchId)
                .setGiftId(giftId)
                .setQuantity(1)
                .setRequestNo("M12-DEMO-APPLY-1")
                .setRemark("M12.7 员工申请兑换")).getCheckedData();
        PageResult<AppRedemptionApplicationRespVO> myRedemptionPage =
                redemptionAppController.getMyPage(appRedemptionPage()).getCheckedData();
        assertEquals(1L, myRedemptionPage.getTotal());

        login(ADMIN_ID, "管理员");
        PageResult<AdminRedemptionApplicationRespVO> adminRedemptionPage =
                redemptionApplicationAdminController.getApplicationPage(adminRedemptionPage()).getCheckedData();
        assertEquals(1L, adminRedemptionPage.getTotal());
        redemptionApplicationAdminController.reviewApplication(new AdminRedemptionReviewReqVO()
                .setId(applicationId)
                .setResult(ClubPointRedemptionReviewResultEnum.APPROVED.getResult())
                .setReason("M12.7 管理员审核兑换通过")).checkError();
        ClubPointRedemptionApplicationDO reviewedApplication = redemptionApplicationMapper.selectById(applicationId);
        assertEquals(ClubPointRedemptionApplicationStatusEnum.APPROVED_AND_ISSUED.getStatus(),
                reviewedApplication.getStatus());

        login(EMPLOYEE_ID, "员工1001");
        AppLedgerSummaryRespVO redeemedSummary = ledgerAppController.getSummary().getCheckedData();
        assertEquals(5, redeemedSummary.getAvailablePoints());

        login(ADMIN_ID, "管理员");
        annualAdminController.generateRanking(new AdminAnnualRankingGenerateReqVO().setYear(YEAR)).checkError();
        PageResult<AdminAnnualRankingRespVO> rankingPage =
                annualAdminController.getRankingPage(annualRankingPage()).getCheckedData();
        assertEquals(1L, rankingPage.getTotal());
        assertEquals(clubId, rankingPage.getList().get(0).getClubId());
        assertEquals(10, rankingPage.getList().get(0).getTotalIssuedPoints());

        AdminAnnualClearRespVO clearResult = annualAdminController.clearAnnualPoints(new AdminAnnualClearReqVO()
                .setYear(YEAR)
                .setReason("M12.7 年度清零")).getCheckedData();
        assertEquals(1, clearResult.getTotalCount());
        assertEquals(1, clearResult.getSuccessCount());
        PageResult<AdminAnnualClearingRecordRespVO> clearingPage =
                annualAdminController.getClearingRecordPage(annualClearingPage()).getCheckedData();
        assertEquals(1L, clearingPage.getTotal());
        assertEquals(5, clearingPage.getList().get(0).getClearablePoints());

        ClubPointAccountDO accountAfterClearing = accountMapper.selectByUserId(EMPLOYEE_ID);
        assertEquals(0, accountAfterClearing.getAvailablePoints());
        assertEquals(0, accountAfterClearing.getNetPoints());

        PageResult<AdminAuditRespVO> auditPage = auditAdminController.getAuditPage(auditPage()).getCheckedData();
        assertTrue(auditPage.getTotal() >= 1);
        PageResult<AdminReportPointDetailRespVO> pointDetailPage =
                reportAdminController.getPointDetailPage(reportPointDetailPage()).getCheckedData();
        assertTrue(pointDetailPage.getTotal() >= 4);
        assertFalse(pointDetailPage.getList().isEmpty());
    }

    private Long createAndPublishDemoRules() {
        Long ruleVersionId = ruleAdminController.createRuleVersion(new RuleVersionSaveReqVO()
                .setVersionNo("M12-DEMO-RULE-001")
                .setName("M12.7 MVP 演示规则")
                .setPublicityTime(LocalDateTime.of(YEAR, 1, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(YEAR, 1, 1, 0, 0))
                .setSummary("M12.7 演示规则")
                .setContent("M12.7 演示规则正文")).getCheckedData();
        savePointRule(ruleVersionId, ClubPointRuleItemCodeEnum.ACTIVITY_MEDIUM_BASE, 8,
                ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), 1);
        savePointRule(ruleVersionId, ClubPointRuleItemCodeEnum.ACTIVITY_FULL_EXTRA, 2,
                ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(), 2);
        savePointRule(ruleVersionId, ClubPointRuleItemCodeEnum.ABSENCE_SINGLE_DEDUCT, 3,
                ClubPointCategoryEnum.DEDUCTION.getCategory(), 3);
        saveIntRule(ruleVersionId, ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_THRESHOLD, 3,
                ClubPointCategoryEnum.DEDUCTION.getCategory(), 4);
        savePointRule(ruleVersionId, ClubPointRuleItemCodeEnum.ABSENCE_MONTHLY_DEDUCT, 20,
                ClubPointCategoryEnum.DEDUCTION.getCategory(), 5);
        savePointRule(ruleVersionId, ClubPointRuleItemCodeEnum.REDEMPTION_MIN_POINTS, 5,
                ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory(), 6);
        ruleAdminController.publishRuleVersion(new RuleOperationReqVO()
                .setId(ruleVersionId)
                .setReason("M12.7 发布演示规则"), loginRequest(ADMIN_ID, "管理员")).checkError();
        return ruleVersionId;
    }

    private Long createClubMemberAndLeader() {
        Long clubId = clubAdminController.createClub(new AdminClubSaveReqVO()
                .setCode("M12-DEMO-CLUB")
                .setName("M12.7 演示俱乐部")
                .setDescription("M12.7 MVP demo club")
                .setContactText("demo contact")
                .setSort(1)
                .setReason("M12.7 创建俱乐部")).getCheckedData();
        clubMemberAdminController.addMember(new AdminClubMemberSaveReqVO()
                .setClubId(clubId)
                .setUserId(EMPLOYEE_ID)
                .setReason("M12.7 添加员工成员")).checkError();
        clubLeaderAdminController.assignLeader(new AdminClubMemberSaveReqVO()
                .setClubId(clubId)
                .setUserId(LEADER_ID)
                .setReason("M12.7 设置负责人")).checkError();
        return clubId;
    }

    private static LeaderActivitySaveReqVO buildLeaderActivitySaveReq(Long clubId, Long ruleVersionId) {
        LocalDateTime now = LocalDateTime.now();
        return new LeaderActivitySaveReqVO()
                .setClubId(clubId)
                .setTitle("M12.7 MVP 演示活动")
                .setLocation("演示活动室")
                .setDescription("M12.7 MVP demo activity")
                .setLevel(2)
                .setStartTime(now.plusMinutes(10))
                .setEndTime(now.plusMinutes(20))
                .setRegistrationDeadline(now.plusMinutes(5))
                .setCancelDeadlineTime(now.plusMinutes(5))
                .setCheckinStartTime(now.minusMinutes(5))
                .setCheckinEndTime(now.plusMinutes(15))
                .setCheckoutMode(1)
                .setCheckoutStartTime(now.minusMinutes(5))
                .setCheckoutEndTime(now.plusMinutes(15))
                .setRuleVersionId(ruleVersionId)
                .setBasePoints(8)
                .setFullExtraPoints(2)
                .setReason("M12.7 负责人创建演示活动");
    }

    private static AdminRedemptionBatchSaveReqVO buildRedemptionBatchSaveReq(Long ruleVersionId) {
        LocalDateTime now = LocalDateTime.now();
        return new AdminRedemptionBatchSaveReqVO()
                .setYear(YEAR)
                .setName("M12.7 MVP 演示兑换批次")
                .setOpenTime(now.minusMinutes(5))
                .setCloseTime(now.plusDays(7))
                .setDescription("M12.7 redemption batch")
                .setQualificationRule("可用积分不少于 1 分")
                .setMinAvailablePoints(1)
                .setQualifiedCount(100)
                .setIncludeTieAtCutoff(true)
                .setRuleVersionId(ruleVersionId)
                .setRuleSnapshotJson("{\"rule\":\"M12.7\"}")
                .setReason("M12.7 创建兑换批次");
    }

    private static AdminRedemptionGiftSaveReqVO buildRedemptionGiftSaveReq(Long batchId) {
        return new AdminRedemptionGiftSaveReqVO()
                .setBatchId(batchId)
                .setName("M12.7 演示礼品")
                .setDescription("M12.7 redemption gift")
                .setPointsCost(5)
                .setTierMinPoints(1)
                .setTierMaxPoints(100)
                .setReferenceAmountCent(1000L)
                .setStockTotal(5)
                .setImageFileId(1L)
                .setSort(1)
                .setReason("M12.7 创建礼品");
    }

    private void savePointRule(Long ruleVersionId, ClubPointRuleItemCodeEnum code, Integer points,
                               Integer category, Integer sort) {
        ruleAdminController.saveRuleItem(new RuleItemSaveReqVO()
                .setRuleVersionId(ruleVersionId)
                .setItemCode(code.getCode())
                .setItemName(code.getName())
                .setItemType(ClubPointRuleItemTypeEnum.POINTS.getType())
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(100)
                .setDefaultPoints(points)
                .setStatus(1)
                .setSort(sort)).checkError();
    }

    private void saveIntRule(Long ruleVersionId, ClubPointRuleItemCodeEnum code, Integer intValue,
                             Integer category, Integer sort) {
        ruleAdminController.saveRuleItem(new RuleItemSaveReqVO()
                .setRuleVersionId(ruleVersionId)
                .setItemCode(code.getCode())
                .setItemName(code.getName())
                .setItemType(ClubPointRuleItemTypeEnum.THRESHOLD.getType())
                .setCategory(category)
                .setMinPoints(0)
                .setMaxPoints(100)
                .setDefaultPoints(0)
                .setIntValue(intValue)
                .setStatus(1)
                .setSort(sort)).checkError();
    }

    private void mockExternalUsers() {
        AdminUserRespDTO employee = new AdminUserRespDTO()
                .setId(EMPLOYEE_ID)
                .setNickname("员工1001")
                .setDeptId(201L)
                .setMobile("13900001001");
        AdminUserRespDTO leader = new AdminUserRespDTO()
                .setId(LEADER_ID)
                .setNickname("负责人9001")
                .setDeptId(202L)
                .setMobile("13900009001");
        DeptRespDTO employeeDept = new DeptRespDTO().setId(201L).setName("综合部");
        DeptRespDTO leaderDept = new DeptRespDTO().setId(202L).setName("运营部");
        Map<Long, AdminUserRespDTO> users = new HashMap<>();
        users.put(EMPLOYEE_ID, employee);
        users.put(LEADER_ID, leader);
        Map<Long, DeptRespDTO> depts = new HashMap<>();
        depts.put(201L, employeeDept);
        depts.put(202L, leaderDept);

        doNothing().when(adminUserApi).validateUser(EMPLOYEE_ID);
        doNothing().when(adminUserApi).validateUser(LEADER_ID);
        when(adminUserApi.getUser(EMPLOYEE_ID)).thenReturn(employee);
        when(adminUserApi.getUser(LEADER_ID)).thenReturn(leader);
        when(adminUserApi.getUserMap(any(Collection.class))).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, AdminUserRespDTO> result = new HashMap<>();
            for (Long id : ids) {
                if (users.containsKey(id)) {
                    result.put(id, users.get(id));
                }
            }
            return result;
        });
        when(deptApi.getDept(201L)).thenReturn(employeeDept);
        when(deptApi.getDept(202L)).thenReturn(leaderDept);
        when(deptApi.getDeptMap(any(Collection.class))).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            Map<Long, DeptRespDTO> result = new HashMap<>();
            for (Long id : ids) {
                if (depts.containsKey(id)) {
                    result.put(id, depts.get(id));
                }
            }
            return result;
        });
    }

    private static AppLedgerTransactionPageReqVO appLedgerPage() {
        AppLedgerTransactionPageReqVO reqVO = new AppLedgerTransactionPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static AppRedemptionApplicationPageReqVO appRedemptionPage() {
        AppRedemptionApplicationPageReqVO reqVO = new AppRedemptionApplicationPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static AdminRedemptionApplicationPageReqVO adminRedemptionPage() {
        AdminRedemptionApplicationPageReqVO reqVO = new AdminRedemptionApplicationPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static AdminAnnualRankingPageReqVO annualRankingPage() {
        AdminAnnualRankingPageReqVO reqVO = new AdminAnnualRankingPageReqVO().setYear(YEAR);
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static AdminAnnualClearingRecordPageReqVO annualClearingPage() {
        AdminAnnualClearingRecordPageReqVO reqVO = new AdminAnnualClearingRecordPageReqVO().setYear(YEAR);
        reqVO.setPageNo(1);
        reqVO.setPageSize(20);
        return reqVO;
    }

    private static AdminAuditPageReqVO auditPage() {
        AdminAuditPageReqVO reqVO = new AdminAuditPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(50);
        return reqVO;
    }

    private static AdminReportPointDetailPageReqVO reportPointDetailPage() {
        AdminReportPointDetailPageReqVO reqVO = new AdminReportPointDetailPageReqVO()
                .setYear(YEAR);
        reqVO.setPageNo(1);
        reqVO.setPageSize(50);
        return reqVO;
    }

    private static MockHttpServletRequest login(Long userId, String nickname) {
        return loginRequest(userId, nickname);
    }

    private static MockHttpServletRequest loginRequest(Long userId, String nickname) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        Map<String, String> info = new HashMap<>();
        info.put(INFO_KEY_NICKNAME, nickname);
        SecurityFrameworkUtils.setLoginUser(new LoginUser()
                .setId(userId)
                .setUserType(ADMIN.getValue())
                .setInfo(info), request);
        return request;
    }

}

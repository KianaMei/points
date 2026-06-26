package cn.iocoder.yudao.module.clubpoints.controller.report;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.ClubPointReportAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportExportReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportBudgetPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportClubRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportLedgerSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportRedemptionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointReportExportTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.report.ClubPointReportServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.security.core.LoginUser.INFO_KEY_NICKNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointReportAdminController.class,
        ClubPointReportServiceImpl.class,
        ClubAuditServiceImpl.class
})
class ClubPointReportControllerTest extends BaseDbUnitTest {

    private static final String REPORT_QUERY_PERMISSION = "@ss.hasPermission('clubpoints:report:query')";
    private static final String REPORT_EXPORT_PERMISSION = "@ss.hasPermission('clubpoints:report:export')";
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 1, 10, 0);

    @Resource
    private ClubPointReportAdminController adminController;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endpointsShouldUseReportQueryPathsAndPermission() throws Exception {
        assertEquals("/clubpoints/report",
                ClubPointReportAdminController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertGetMapping("getPointDetailPage", new Class<?>[]{AdminReportPointDetailPageReqVO.class},
                "/point-detail-page", REPORT_QUERY_PERMISSION);
        assertGetMapping("getLedgerSummaryPage", new Class<?>[]{AdminReportLedgerSummaryPageReqVO.class},
                "/ledger-summary-page", REPORT_QUERY_PERMISSION);
        assertGetMapping("getRedemptionPage", new Class<?>[]{AdminReportRedemptionPageReqVO.class},
                "/redemption-page", REPORT_QUERY_PERMISSION);
        assertGetMapping("getClubRankingPage", new Class<?>[]{AdminReportClubRankingPageReqVO.class},
                "/club-ranking-page", REPORT_QUERY_PERMISSION);
        assertGetMapping("getBudgetPage", new Class<?>[]{AdminReportBudgetPageReqVO.class},
                "/budget-page", REPORT_QUERY_PERMISSION);
        assertGetMapping("exportExcel", new Class<?>[]{AdminReportExportReqVO.class, javax.servlet.http.HttpServletResponse.class},
                "/export-excel", REPORT_EXPORT_PERMISSION);
    }

    @Test
    void exportExcelShouldWriteAuditBeforeReturningExcelResponse() throws Exception {
        login(9001L, "导出管理员");
        insertTransaction();

        MockHttpServletResponse response = new MockHttpServletResponse();
        adminController.exportExcel(new AdminReportExportReqVO()
                .setReportType(ClubPointReportExportTypeEnum.POINT_DETAIL.getType())
                .setUserId(100L)
                .setYear(2026), response);

        assertEquals("application/vnd.ms-excel;charset=UTF-8", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains(".xls"));
        assertTrue(response.getContentAsByteArray().length > 0);

        ClubAuditLogDO auditLog = auditLogMapper.selectList().get(0);
        assertEquals(ClubAuditActionTypeConstants.REPORT_EXPORT, auditLog.getActionType());
        assertEquals("REPORT", auditLog.getBizType());
        assertEquals(9001L, auditLog.getOperatorUserId());
        assertEquals("导出管理员", auditLog.getOperatorNameSnapshot());
        assertEquals("admin", auditLog.getOperatorRoleSnapshot());
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"reportType\":1"));
        assertTrue(auditLog.getTargetSnapshotJson().contains("\"rowCount\":1"));
    }

    private static void assertGetMapping(String methodName, Class<?>[] parameterTypes, String expectedPath,
                                         String expectedPermission) throws NoSuchMethodException {
        Method method = ClubPointReportAdminController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(expectedPermission, preAuthorize.value());
    }

    private void insertTransaction() {
        transactionMapper.insert(new ClubPointTransactionDO()
                .setTransactionNo("TX-RPT-EXPORT-1001")
                .setUserId(100L)
                .setUserNameSnapshot("Export User")
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("Ops")
                .setDirection(ClubPointTransactionDirectionEnum.INCREASE.getDirection())
                .setPoints(12)
                .setPointCategory(ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory())
                .setPointTypeCode("REPORT_EXPORT_TEST")
                .setStatus(ClubPointTransactionStatusEnum.VALID.getStatus())
                .setSourceType(ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType())
                .setSourceId(1L)
                .setSourceTitleSnapshot("Report Export Source")
                .setIssuingClubId(400L)
                .setIssuingClubCodeSnapshot("CLUB-400")
                .setIssuingClubNameSnapshot("Club 400")
                .setRuleVersionId(1L)
                .setRuleItemCodeSnapshot("REPORT_EXPORT_TEST")
                .setEvidenceType(1)
                .setMaterialSummary("Report export material")
                .setReason("Report export reason")
                .setOccurredAt(BASE_TIME)
                .setBusinessYear(2026)
                .setBusinessMonth(202606)
                .setIdempotencyKey("IDEMP-TX-RPT-EXPORT-1001")
                .setOperatorUserId(9001L));
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

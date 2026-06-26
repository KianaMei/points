package cn.iocoder.yudao.module.clubpoints.controller.report;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.ClubPointReportAdminController;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportBudgetPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportClubRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportLedgerSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportRedemptionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.service.report.ClubPointReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import({
        ClubPointReportAdminController.class,
        ClubPointReportServiceImpl.class
})
class ClubPointReportControllerTest extends BaseDbUnitTest {

    private static final String REPORT_QUERY_PERMISSION = "@ss.hasPermission('clubpoints:report:query')";

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
    }

    private static void assertGetMapping(String methodName, Class<?>[] parameterTypes, String expectedPath,
                                         String expectedPermission) throws NoSuchMethodException {
        Method method = ClubPointReportAdminController.class.getMethod(methodName, parameterTypes);
        assertEquals(expectedPath, method.getAnnotation(GetMapping.class).value()[0]);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals(expectedPermission, preAuthorize.value());
    }

}

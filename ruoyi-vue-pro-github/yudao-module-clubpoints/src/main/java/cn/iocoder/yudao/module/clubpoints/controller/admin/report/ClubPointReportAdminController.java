package cn.iocoder.yudao.module.clubpoints.controller.admin.report;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportBudgetPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportBudgetRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportClubRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportClubRankingRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportExportReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportLedgerSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportLedgerSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportRedemptionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportRedemptionRespVO;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointReportExportTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.report.ClubPointReportService;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportBudgetBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportBudgetPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportClubRankingBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportClubRankingPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportExportReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportExportResultBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportLedgerSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportLedgerSummaryPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportPointDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportPointDetailPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportRedemptionBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportRedemptionPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 俱乐部积分报表")
@RestController
@RequestMapping("/clubpoints/report")
@Validated
public class ClubPointReportAdminController {

    @Resource
    private ClubPointReportService reportService;

    @GetMapping("/point-detail-page")
    @Operation(summary = "积分明细报表分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:report:query')")
    public CommonResult<PageResult<AdminReportPointDetailRespVO>> getPointDetailPage(
            @Valid AdminReportPointDetailPageReqVO pageReqVO) {
        PageResult<ClubPointReportPointDetailBO> pageResult = reportService.getPointDetailPage(
                BeanUtils.toBean(pageReqVO, ClubPointReportPointDetailPageReqBO.class));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminReportPointDetailRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/ledger-summary-page")
    @Operation(summary = "积分总台账报表分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:report:query')")
    public CommonResult<PageResult<AdminReportLedgerSummaryRespVO>> getLedgerSummaryPage(
            @Valid AdminReportLedgerSummaryPageReqVO pageReqVO) {
        PageResult<ClubPointReportLedgerSummaryBO> pageResult = reportService.getLedgerSummaryPage(
                BeanUtils.toBean(pageReqVO, ClubPointReportLedgerSummaryPageReqBO.class));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminReportLedgerSummaryRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/redemption-page")
    @Operation(summary = "兑换记录报表分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:report:query')")
    public CommonResult<PageResult<AdminReportRedemptionRespVO>> getRedemptionPage(
            @Valid AdminReportRedemptionPageReqVO pageReqVO) {
        PageResult<ClubPointReportRedemptionBO> pageResult = reportService.getRedemptionPage(
                BeanUtils.toBean(pageReqVO, ClubPointReportRedemptionPageReqBO.class));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminReportRedemptionRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/club-ranking-page")
    @Operation(summary = "俱乐部发放积分排名报表分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:report:query')")
    public CommonResult<PageResult<AdminReportClubRankingRespVO>> getClubRankingPage(
            @Valid AdminReportClubRankingPageReqVO pageReqVO) {
        PageResult<ClubPointReportClubRankingBO> pageResult = reportService.getClubRankingPage(
                BeanUtils.toBean(pageReqVO, ClubPointReportClubRankingPageReqBO.class));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminReportClubRankingRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/budget-page")
    @Operation(summary = "预算和经费统计报表分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:report:query')")
    public CommonResult<PageResult<AdminReportBudgetRespVO>> getBudgetPage(
            @Valid AdminReportBudgetPageReqVO pageReqVO) {
        PageResult<ClubPointReportBudgetBO> pageResult = reportService.getBudgetPage(
                BeanUtils.toBean(pageReqVO, ClubPointReportBudgetPageReqBO.class));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminReportBudgetRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出报表")
    @PreAuthorize("@ss.hasPermission('clubpoints:report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExcel(@Valid AdminReportExportReqVO exportReqVO, HttpServletResponse response)
            throws IOException {
        ClubPointReportExportResultBO exportResult = reportService.exportReport(buildExportReqBO(exportReqVO));
        writeExcel(response, exportResult);
    }

    private static ClubPointReportExportReqBO buildExportReqBO(AdminReportExportReqVO reqVO) {
        return BeanUtils.toBean(reqVO, ClubPointReportExportReqBO.class)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

    private static void writeExcel(HttpServletResponse response, ClubPointReportExportResultBO exportResult)
            throws IOException {
        Integer reportType = exportResult.getReportType();
        if (ClubPointReportExportTypeEnum.POINT_DETAIL.getType().equals(reportType)) {
            ExcelUtils.write(response, exportResult.getReportName() + ".xls", exportResult.getSheetName(),
                    AdminReportPointDetailRespVO.class,
                    BeanUtils.toBean(exportResult.getRows(), AdminReportPointDetailRespVO.class));
            return;
        }
        if (ClubPointReportExportTypeEnum.REDEMPTION.getType().equals(reportType)) {
            ExcelUtils.write(response, exportResult.getReportName() + ".xls", exportResult.getSheetName(),
                    AdminReportRedemptionRespVO.class,
                    BeanUtils.toBean(exportResult.getRows(), AdminReportRedemptionRespVO.class));
            return;
        }
        if (ClubPointReportExportTypeEnum.LEDGER_SUMMARY.getType().equals(reportType)) {
            ExcelUtils.write(response, exportResult.getReportName() + ".xls", exportResult.getSheetName(),
                    AdminReportLedgerSummaryRespVO.class,
                    BeanUtils.toBean(exportResult.getRows(), AdminReportLedgerSummaryRespVO.class));
            return;
        }
        if (ClubPointReportExportTypeEnum.CLUB_RANKING.getType().equals(reportType)) {
            ExcelUtils.write(response, exportResult.getReportName() + ".xls", exportResult.getSheetName(),
                    AdminReportClubRankingRespVO.class,
                    BeanUtils.toBean(exportResult.getRows(), AdminReportClubRankingRespVO.class));
            return;
        }
        ExcelUtils.write(response, exportResult.getReportName() + ".xls", exportResult.getSheetName(),
                AdminReportBudgetRespVO.class,
                BeanUtils.toBean(exportResult.getRows(), AdminReportBudgetRespVO.class));
    }

}

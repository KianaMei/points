package cn.iocoder.yudao.module.clubpoints.controller.admin.report;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportBudgetPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportBudgetRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportClubRankingPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportClubRankingRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportLedgerSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportLedgerSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportPointDetailRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportRedemptionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo.AdminReportRedemptionRespVO;
import cn.iocoder.yudao.module.clubpoints.service.report.ClubPointReportService;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportBudgetBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportBudgetPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportClubRankingBO;
import cn.iocoder.yudao.module.clubpoints.service.report.bo.ClubPointReportClubRankingPageReqBO;
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
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

}

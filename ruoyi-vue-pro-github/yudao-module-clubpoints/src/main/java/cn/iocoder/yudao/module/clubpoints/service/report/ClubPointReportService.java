package cn.iocoder.yudao.module.clubpoints.service.report;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
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

public interface ClubPointReportService {

    PageResult<ClubPointReportPointDetailBO> getPointDetailPage(ClubPointReportPointDetailPageReqBO reqBO);

    PageResult<ClubPointReportLedgerSummaryBO> getLedgerSummaryPage(ClubPointReportLedgerSummaryPageReqBO reqBO);

    PageResult<ClubPointReportRedemptionBO> getRedemptionPage(ClubPointReportRedemptionPageReqBO reqBO);

    PageResult<ClubPointReportClubRankingBO> getClubRankingPage(ClubPointReportClubRankingPageReqBO reqBO);

    PageResult<ClubPointReportBudgetBO> getBudgetPage(ClubPointReportBudgetPageReqBO reqBO);

    ClubPointReportExportResultBO exportReport(ClubPointReportExportReqBO reqBO);

}

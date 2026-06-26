package cn.iocoder.yudao.module.clubpoints.service.dashboard;

import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.AdminDashboardSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.AppDashboardSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.LeaderDashboardSummaryBO;

public interface ClubPointDashboardService {

    AppDashboardSummaryBO getAppSummary(Long userId);

    LeaderDashboardSummaryBO getLeaderSummary(Long userId);

    AdminDashboardSummaryBO getAdminSummary();

}

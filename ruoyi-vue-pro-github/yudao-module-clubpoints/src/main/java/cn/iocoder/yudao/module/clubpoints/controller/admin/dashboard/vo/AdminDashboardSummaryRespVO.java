package cn.iocoder.yudao.module.clubpoints.controller.admin.dashboard.vo;

import cn.iocoder.yudao.module.clubpoints.controller.shared.vo.DashboardTodoItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Schema(description = "管理后台 - 工作台汇总 Response VO")
@Data
@Accessors(chain = true)
public class AdminDashboardSummaryRespVO {

    private Integer pendingActivityReviewCount;
    private Integer pendingContributionReviewCount;
    private Integer pendingRedemptionReviewCount;
    private Integer pendingDisputeCount;
    private Integer todoCount;
    private List<DashboardTodoItemRespVO> todoItems;

}

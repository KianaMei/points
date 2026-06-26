package cn.iocoder.yudao.module.clubpoints.service.dashboard.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AdminDashboardSummaryBO {

    private Integer pendingActivityReviewCount;
    private Integer pendingContributionReviewCount;
    private Integer pendingRedemptionReviewCount;
    private Integer pendingDisputeCount;
    private Integer todoCount;
    private List<ClubPointDashboardTodoItemBO> todoItems;

}

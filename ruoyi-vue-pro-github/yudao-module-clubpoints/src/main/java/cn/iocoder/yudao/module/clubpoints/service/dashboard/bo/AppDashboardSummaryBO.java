package cn.iocoder.yudao.module.clubpoints.service.dashboard.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AppDashboardSummaryBO {

    private Integer availablePoints;
    private Integer frozenPoints;
    private Integer totalEarnedPoints;
    private Integer joinedClubCount;
    private Integer registeredActivityCount;
    private Integer pendingRedemptionCount;
    private Integer unreadNotifyCount;
    private Integer todoCount;
    private List<ClubPointDashboardTodoItemBO> todoItems;

}

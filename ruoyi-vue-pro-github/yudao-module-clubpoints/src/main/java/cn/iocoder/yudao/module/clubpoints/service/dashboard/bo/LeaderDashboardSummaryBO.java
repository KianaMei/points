package cn.iocoder.yudao.module.clubpoints.service.dashboard.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class LeaderDashboardSummaryBO {

    private Integer managedClubCount;
    private Integer draftActivityCount;
    private Integer rejectedActivityCount;
    private Integer attendanceExceptionCount;
    private Integer pendingContributionSubmitCount;
    private Integer todoCount;
    private List<ClubPointDashboardTodoItemBO> todoItems;

}

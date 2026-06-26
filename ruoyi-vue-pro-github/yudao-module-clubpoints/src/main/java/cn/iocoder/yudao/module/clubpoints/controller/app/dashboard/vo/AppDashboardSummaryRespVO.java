package cn.iocoder.yudao.module.clubpoints.controller.app.dashboard.vo;

import cn.iocoder.yudao.module.clubpoints.controller.shared.vo.DashboardTodoItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Schema(description = "员工端 - 工作台汇总 Response VO")
@Data
@Accessors(chain = true)
public class AppDashboardSummaryRespVO {

    private Integer availablePoints;
    private Integer frozenPoints;
    private Integer totalEarnedPoints;
    private Integer joinedClubCount;
    private Integer registeredActivityCount;
    private Integer pendingRedemptionCount;
    private Integer unreadNotifyCount;
    private Integer todoCount;
    private List<DashboardTodoItemRespVO> todoItems;

}

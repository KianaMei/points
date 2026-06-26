package cn.iocoder.yudao.module.clubpoints.controller.leader.dashboard.vo;

import cn.iocoder.yudao.module.clubpoints.controller.shared.vo.DashboardTodoItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Schema(description = "负责人端 - 工作台汇总 Response VO")
@Data
@Accessors(chain = true)
public class LeaderDashboardSummaryRespVO {

    private Integer managedClubCount;
    private Integer draftActivityCount;
    private Integer rejectedActivityCount;
    private Integer attendanceExceptionCount;
    private Integer pendingContributionSubmitCount;
    private Integer todoCount;
    private List<DashboardTodoItemRespVO> todoItems;

}

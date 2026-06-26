package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 报表导出 Request VO")
@Data
@Accessors(chain = true)
public class AdminReportExportReqVO {

    @Schema(description = "报表类型：1 积分明细；2 兑换记录；3 总台账；4 俱乐部排名；5 预算统计",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报表类型不能为空")
    private Integer reportType;

    @Schema(description = "员工编号")
    private Long userId;

    @Schema(description = "俱乐部编号")
    private Long clubId;

    @Schema(description = "年度")
    private Integer year;

    @Schema(description = "流水方向")
    private Integer direction;

    @Schema(description = "积分分类")
    private Integer pointCategory;

    @Schema(description = "来源类型")
    private Integer sourceType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "预算分类")
    private Integer category;

    @Schema(description = "来源编号；兑换记录导出时表示批次编号")
    private Long sourceId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "导出原因")
    private String reason;

}

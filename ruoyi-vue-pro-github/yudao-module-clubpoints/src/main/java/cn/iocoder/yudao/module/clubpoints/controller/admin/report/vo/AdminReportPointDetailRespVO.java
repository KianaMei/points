package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分明细报表 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class AdminReportPointDetailRespVO {

    @ExcelProperty("流水编号")
    private Long id;
    @ExcelProperty("流水号")
    private String transactionNo;
    @ExcelProperty("员工编号")
    private Long userId;
    @ExcelProperty("员工姓名")
    private String userNameSnapshot;
    @ExcelProperty("部门编号")
    private Long deptIdSnapshot;
    @ExcelProperty("部门名称")
    private String deptNameSnapshot;
    @ExcelProperty("方向")
    private Integer direction;
    @ExcelProperty("积分")
    private Integer points;
    @ExcelProperty("积分分类")
    private Integer pointCategory;
    @ExcelProperty("来源类型")
    private Integer sourceType;
    @ExcelProperty("来源编号")
    private Long sourceId;
    @ExcelProperty("来源明细编号")
    private Long sourceItemId;
    @ExcelProperty("来源标题")
    private String sourceTitleSnapshot;
    @ExcelProperty("发放俱乐部编号")
    private Long issuingClubId;
    @ExcelProperty("发放俱乐部")
    private String issuingClubNameSnapshot;
    @ExcelProperty("活动编号")
    private Long activityId;
    @ExcelProperty("活动标题")
    private String activityTitleSnapshot;
    @ExcelProperty("规则版本编号")
    private Long ruleVersionId;
    @ExcelProperty("规则项编号")
    private Long ruleItemId;
    @ExcelProperty("规则项编码")
    private String ruleItemCodeSnapshot;
    @ExcelProperty("证据类型")
    private Integer evidenceType;
    @ExcelProperty("材料摘要")
    private String materialSummary;
    @ExcelProperty("原因")
    private String reason;
    @ExcelProperty("发生时间")
    private LocalDateTime occurredTime;
    @ExcelProperty("创建时间")
    private LocalDateTime createdTime;

}

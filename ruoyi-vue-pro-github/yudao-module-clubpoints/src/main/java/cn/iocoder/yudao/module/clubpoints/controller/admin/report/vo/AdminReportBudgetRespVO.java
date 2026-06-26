package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Schema(description = "管理后台 - 预算统计报表 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class AdminReportBudgetRespVO {

    @ExcelProperty("记录编号")
    private Long id;
    @ExcelProperty("预算分类")
    private Integer category;
    @ExcelProperty("预算金额分")
    private Long budgetAmountCent;
    @ExcelProperty("实际金额分")
    private Long actualAmountCent;
    @ExcelProperty("发生日期")
    private LocalDate occurDate;
    @ExcelProperty("经办人编号")
    private Long handlerUserId;
    @ExcelProperty("来源类型")
    private Integer sourceType;
    @ExcelProperty("来源编号")
    private Long sourceId;
    @ExcelProperty("说明")
    private String description;
    @ExcelProperty("备注")
    private String remark;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分总台账报表 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class AdminReportLedgerSummaryRespVO {

    @ExcelProperty("员工编号")
    private Long userId;
    @ExcelProperty("员工姓名")
    private String userNameSnapshot;
    @ExcelProperty("部门编号")
    private Long deptIdSnapshot;
    @ExcelProperty("部门名称")
    private String deptNameSnapshot;
    @ExcelProperty("筛选正向积分")
    private Integer reportPositivePoints;
    @ExcelProperty("筛选负向积分")
    private Integer reportNegativePoints;
    @ExcelProperty("筛选净积分")
    private Integer reportNetPoints;
    @ExcelProperty("筛选流水数")
    private Integer transactionCount;
    @ExcelProperty("累计正向积分")
    private Integer totalPositivePoints;
    @ExcelProperty("累计负向积分")
    private Integer totalNegativePoints;
    @ExcelProperty("当前净积分")
    private Integer netPoints;
    @ExcelProperty("冻结积分")
    private Integer frozenPoints;
    @ExcelProperty("可用积分")
    private Integer availablePoints;
    @ExcelProperty("年度获得积分")
    private Integer annualEarnedPoints;
    @ExcelProperty("最后流水编号")
    private Long lastTransactionId;
    @ExcelProperty("最后流水时间")
    private LocalDateTime lastTransactionTime;

}

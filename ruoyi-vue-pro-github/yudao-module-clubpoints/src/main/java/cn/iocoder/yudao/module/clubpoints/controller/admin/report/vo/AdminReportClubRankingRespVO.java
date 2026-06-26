package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 俱乐部排名报表 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class AdminReportClubRankingRespVO {

    @ExcelProperty("记录编号")
    private Long id;
    @ExcelProperty("年度")
    private Integer year;
    @ExcelProperty("俱乐部编号")
    private Long clubId;
    @ExcelProperty("俱乐部编码")
    private String clubCodeSnapshot;
    @ExcelProperty("俱乐部名称")
    private String clubNameSnapshot;
    @ExcelProperty("活动积分")
    private Integer activityPoints;
    @ExcelProperty("非签到积分")
    private Integer contributionPoints;
    @ExcelProperty("奖励积分")
    private Integer rewardPoints;
    @ExcelProperty("撤销积分")
    private Integer reversedPoints;
    @ExcelProperty("总发放积分")
    private Integer totalIssuedPoints;
    @ExcelProperty("排名")
    private Integer rankNo;
    @ExcelProperty("激励金额分")
    private Long incentiveAmountCent;
    @ExcelProperty("确认状态")
    private Integer confirmStatus;
    @ExcelProperty("预算记录编号")
    private Long budgetRecordId;
    @ExcelProperty("生成时间")
    private LocalDateTime generatedTime;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 兑换记录报表 Response VO")
@Data
@Accessors(chain = true)
@ExcelIgnoreUnannotated
public class AdminReportRedemptionRespVO {

    @ExcelProperty("申请编号")
    private Long id;
    @ExcelProperty("申请单号")
    private String applicationNo;
    @ExcelProperty("请求号")
    private String requestNo;
    @ExcelProperty("批次编号")
    private Long batchId;
    @ExcelProperty("礼品编号")
    private Long giftId;
    @ExcelProperty("员工编号")
    private Long userId;
    @ExcelProperty("状态")
    private Integer status;
    @ExcelProperty("消耗积分")
    private Integer pointsCost;
    @ExcelProperty("数量")
    private Integer quantity;
    @ExcelProperty("冻结编号")
    private Long freezeId;
    @ExcelProperty("库存锁编号")
    private Long stockLockId;
    @ExcelProperty("扣减流水编号")
    private Long deductTransactionId;
    @ExcelProperty("批次快照")
    private String batchSnapshotJson;
    @ExcelProperty("礼品快照")
    private String giftSnapshotJson;
    @ExcelProperty("申请时间")
    private LocalDateTime applyTime;
    @ExcelProperty("取消时间")
    private LocalDateTime cancelTime;
    @ExcelProperty("审核时间")
    private LocalDateTime reviewTime;
    @ExcelProperty("审核原因")
    private String reviewReason;
    @ExcelProperty("发放时间")
    private LocalDateTime directIssueTime;

}

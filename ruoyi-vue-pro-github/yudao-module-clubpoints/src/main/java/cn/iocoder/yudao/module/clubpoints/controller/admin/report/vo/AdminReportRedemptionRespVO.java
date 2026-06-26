package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 兑换记录报表 Response VO")
@Data
@Accessors(chain = true)
public class AdminReportRedemptionRespVO {

    private Long id;
    private String applicationNo;
    private String requestNo;
    private Long batchId;
    private Long giftId;
    private Long userId;
    private Integer status;
    private Integer pointsCost;
    private Integer quantity;
    private Long freezeId;
    private Long stockLockId;
    private Long deductTransactionId;
    private String batchSnapshotJson;
    private String giftSnapshotJson;
    private LocalDateTime applyTime;
    private LocalDateTime cancelTime;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private LocalDateTime directIssueTime;

}

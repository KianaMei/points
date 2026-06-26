package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ClubPointReportRedemptionBO {

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

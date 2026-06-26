package cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AppRedemptionApplicationRespVO {

    private Long id;
    private String applicationNo;
    private String requestNo;
    private Long batchId;
    private String batchNameSnapshot;
    private Long giftId;
    private String giftNameSnapshot;
    private Integer pointsCostSnapshot;
    private Integer quantity;
    private Integer frozenPoints;
    private Integer status;
    private Integer qualificationRankSnapshot;
    private LocalDateTime applyTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private LocalDateTime directIssueTime;

}

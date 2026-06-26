package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminSettlementTransactionRespVO {

    private Long id;
    private Long userId;
    private String userNameSnapshot;
    private Integer direction;
    private Integer points;
    private Integer pointCategory;
    private Integer sourceType;
    private Long sourceId;
    private Long sourceItemId;
    private String sourceTitleSnapshot;
    private Long issuingClubId;
    private String issuingClubNameSnapshot;
    private Long activityId;
    private String activityTitleSnapshot;
    private String reason;
    private LocalDateTime occurredAt;
    private String idempotencyKey;

}

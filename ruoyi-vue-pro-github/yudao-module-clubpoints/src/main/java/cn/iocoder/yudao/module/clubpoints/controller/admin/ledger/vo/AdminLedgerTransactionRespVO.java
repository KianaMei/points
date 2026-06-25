package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分流水 Response VO")
@Data
@Accessors(chain = true)
public class AdminLedgerTransactionRespVO {

    private Long id;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer direction;
    private Integer points;
    private Integer pointCategory;
    private Integer sourceType;
    private Long sourceId;
    private Long issuingClubId;
    private String issuingClubNameSnapshot;
    private Long ruleVersionId;
    private Integer evidenceType;
    private String reason;
    private String materialSummary;
    private LocalDateTime occurredTime;
    private LocalDateTime createdTime;
    private Boolean reversed;
    private Long reverseTransactionId;

}

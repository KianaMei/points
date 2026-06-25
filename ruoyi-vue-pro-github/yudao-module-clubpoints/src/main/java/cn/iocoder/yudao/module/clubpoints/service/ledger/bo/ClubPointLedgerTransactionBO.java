package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分流水查询结果 BO
 */
@Data
@Accessors(chain = true)
public class ClubPointLedgerTransactionBO {

    private Long id;
    private String transactionNo;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer direction;
    private Integer points;
    private Integer pointCategory;
    private Integer sourceType;
    private Long sourceId;
    private Long sourceItemId;
    private String sourceTitleSnapshot;
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

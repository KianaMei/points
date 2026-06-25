package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分调整请求
 */
@Data
@Accessors(chain = true)
public class ClubPointLedgerAdjustReqBO {

    private String requestNo;
    private String transactionNo;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer adjustType;
    private Integer direction;
    private Integer points;
    private Long issuingClubId;
    private String issuingClubCodeSnapshot;
    private String issuingClubNameSnapshot;
    private Long ruleVersionId;
    private String ruleItemCode;
    private String reason;
    private String materialSummary;
    private String attachmentSnapshotJson;
    private LocalDateTime occurredAt;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}

package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分流水创建请求
 */
@Data
@Accessors(chain = true)
public class ClubPointLedgerCreateReqBO {

    private String transactionNo;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer direction;
    private Integer points;
    private Integer pointCategory;
    private String pointTypeCode;
    private Integer sourceType;
    private Long sourceId;
    private Long sourceItemId;
    private String sourceTitleSnapshot;
    private Long issuingClubId;
    private String issuingClubCodeSnapshot;
    private String issuingClubNameSnapshot;
    private Long activityId;
    private String activityTitleSnapshot;
    private Integer evidenceType;
    private String materialSummary;
    private String reason;
    private LocalDateTime occurredAt;
    private String idempotencyKey;
    private Long operatorUserId;
    private Long auditLogId;
    private String ruleItemCode;
    private Long ruleVersionId;
    private String sourceSnapshotJson;

}

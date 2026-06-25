package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分冻结转扣减请求
 */
@Data
@Accessors(chain = true)
public class ClubPointFreezeConvertReqBO {

    private Long freezeId;
    private String transactionNo;
    private String transactionIdempotencyKey;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private String sourceTitleSnapshot;
    private String reason;
    private LocalDateTime convertedAt;
    private String ruleItemCode;
    private Long ruleVersionId;
    private String sourceSnapshotJson;
    private Long operatorUserId;
    private Long auditLogId;

}

package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分流水撤销请求
 */
@Data
@Accessors(chain = true)
public class ClubPointLedgerReverseReqBO {

    private Long sourceTransactionId;
    private String transactionNo;
    private String reason;
    private LocalDateTime occurredAt;
    private String attachmentSnapshotJson;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}

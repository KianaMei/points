package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 负责俱乐部成员积分摘要 BO
 */
@Data
@Accessors(chain = true)
public class ClubPointLedgerMemberSummaryBO {

    private Long clubId;
    private String clubNameSnapshot;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer clubPositivePoints;
    private Integer clubNegativePoints;
    private Integer clubNetPoints;
    private Long lastTransactionId;
    private LocalDateTime lastTransactionTime;

}

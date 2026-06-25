package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 员工积分概览 BO
 */
@Data
@Accessors(chain = true)
public class ClubPointLedgerSummaryBO {

    private Integer availablePoints;
    private Integer frozenPoints;
    private Integer totalPositivePoints;
    private Integer totalNegativePoints;
    private Integer annualClearedPoints;
    private LocalDateTime lastTransactionTime;

}

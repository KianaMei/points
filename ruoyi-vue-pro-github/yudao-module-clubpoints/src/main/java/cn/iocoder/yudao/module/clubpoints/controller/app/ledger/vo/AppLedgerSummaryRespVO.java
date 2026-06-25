package cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "员工端 - 我的积分概览 Response VO")
@Data
@Accessors(chain = true)
public class AppLedgerSummaryRespVO {

    private Integer availablePoints;
    private Integer frozenPoints;
    private Integer totalPositivePoints;
    private Integer totalNegativePoints;
    private Integer annualClearedPoints;
    private LocalDateTime lastTransactionTime;

}

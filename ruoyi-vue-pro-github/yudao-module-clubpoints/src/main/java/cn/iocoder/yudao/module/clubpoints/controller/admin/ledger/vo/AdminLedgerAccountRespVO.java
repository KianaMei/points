package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分账户 Response VO")
@Data
@Accessors(chain = true)
public class AdminLedgerAccountRespVO {

    private Long id;
    private Long userId;
    private Integer totalPositivePoints;
    private Integer totalNegativePoints;
    private Integer netPoints;
    private Integer frozenPoints;
    private Integer availablePoints;
    private Integer annualEarnedPoints;
    private Long lastTransactionId;
    private LocalDateTime lastTransactionTime;
    private LocalDateTime lastRebuildTime;
    private Integer version;

}

package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ClubPointReportLedgerSummaryBO {

    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer reportPositivePoints;
    private Integer reportNegativePoints;
    private Integer reportNetPoints;
    private Integer transactionCount;
    private Integer totalPositivePoints;
    private Integer totalNegativePoints;
    private Integer netPoints;
    private Integer frozenPoints;
    private Integer availablePoints;
    private Integer annualEarnedPoints;
    private Long lastTransactionId;
    private LocalDateTime lastTransactionTime;

}

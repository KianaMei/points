package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分总台账报表 Response VO")
@Data
@Accessors(chain = true)
public class AdminReportLedgerSummaryRespVO {

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

package cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "负责人端 - 负责俱乐部成员积分摘要 Response VO")
@Data
@Accessors(chain = true)
public class LeaderLedgerMemberSummaryRespVO {

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

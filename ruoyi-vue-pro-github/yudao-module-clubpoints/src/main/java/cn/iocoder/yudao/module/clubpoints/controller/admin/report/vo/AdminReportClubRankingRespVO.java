package cn.iocoder.yudao.module.clubpoints.controller.admin.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 俱乐部排名报表 Response VO")
@Data
@Accessors(chain = true)
public class AdminReportClubRankingRespVO {

    private Long id;
    private Integer year;
    private Long clubId;
    private String clubCodeSnapshot;
    private String clubNameSnapshot;
    private Integer activityPoints;
    private Integer contributionPoints;
    private Integer rewardPoints;
    private Integer reversedPoints;
    private Integer totalIssuedPoints;
    private Integer rankNo;
    private Long incentiveAmountCent;
    private Integer confirmStatus;
    private Long budgetRecordId;
    private LocalDateTime generatedTime;

}

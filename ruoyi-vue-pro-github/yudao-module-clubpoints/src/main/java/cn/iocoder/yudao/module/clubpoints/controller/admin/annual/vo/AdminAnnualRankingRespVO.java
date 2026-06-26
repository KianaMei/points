package cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminAnnualRankingRespVO {

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
    private LocalDateTime generatedTime;

}

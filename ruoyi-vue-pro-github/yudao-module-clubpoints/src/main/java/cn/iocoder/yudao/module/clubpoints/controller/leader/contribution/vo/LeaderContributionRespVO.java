package cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "负责人端 - 非签到积分材料 Response VO")
@Data
@Accessors(chain = true)
public class LeaderContributionRespVO {

    private Long id;
    private Long clubId;
    private String clubNameSnapshot;
    private Integer type;
    private String title;
    private String description;
    private Integer status;
    private Long ruleVersionId;
    private Long submitterUserId;
    private LocalDateTime submitTime;
    private Long reviewerUserId;
    private LocalDateTime reviewTime;
    private String reviewReason;
    private Boolean locked;
    private Boolean directCreated;
    private String requestNo;
    private List<LeaderContributionItemRespVO> items;

}

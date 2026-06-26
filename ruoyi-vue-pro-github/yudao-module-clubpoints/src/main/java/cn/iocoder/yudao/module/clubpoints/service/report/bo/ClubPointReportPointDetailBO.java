package cn.iocoder.yudao.module.clubpoints.service.report.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ClubPointReportPointDetailBO {

    private Long id;
    private String transactionNo;
    private Long userId;
    private String userNameSnapshot;
    private Long deptIdSnapshot;
    private String deptNameSnapshot;
    private Integer direction;
    private Integer points;
    private Integer pointCategory;
    private Integer sourceType;
    private Long sourceId;
    private Long sourceItemId;
    private String sourceTitleSnapshot;
    private Long issuingClubId;
    private String issuingClubNameSnapshot;
    private Long activityId;
    private String activityTitleSnapshot;
    private Long ruleVersionId;
    private Long ruleItemId;
    private String ruleItemCodeSnapshot;
    private Integer evidenceType;
    private String materialSummary;
    private String reason;
    private LocalDateTime occurredTime;
    private LocalDateTime createdTime;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminSettlementRunRespVO {

    private Long id;
    private Long activityId;
    private Long clubId;
    private String activityTitle;
    private String clubName;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private Long jobRunId;
    private String runKey;
    private Integer status;
    private LocalDateTime settlementTime;
    private Long configVersionId;
    private Integer registrationCount;
    private Integer successCount;
    private Integer skipCount;
    private Integer failedCount;
    private String errorMessage;
    private Integer triggerSource;
    private Long operatorUserId;

}

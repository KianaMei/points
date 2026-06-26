package cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 任务运行 Response VO")
@Data
@Accessors(chain = true)
public class AdminJobRunRespVO {

    private Long id;
    private String taskType;
    private String bizType;
    private Long bizId;
    private String runKey;
    private String idempotencyKey;
    private Integer status;
    private LocalDateTime plannedTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer triggerSource;
    private Long handlerUserId;
    private Integer totalCount;
    private Integer successCount;
    private Integer skipCount;
    private Integer failedCount;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String errorType;
    private String errorMessage;
    private String resultJson;
    private String manualHandleReason;
    private LocalDateTime createTime;

}

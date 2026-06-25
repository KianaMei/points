package cn.iocoder.yudao.module.clubpoints.dal.dataobject.job;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("club_points_job_run")
@KeySequence("club_points_job_run_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubJobRunDO extends BaseDO {

    @TableId
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

}

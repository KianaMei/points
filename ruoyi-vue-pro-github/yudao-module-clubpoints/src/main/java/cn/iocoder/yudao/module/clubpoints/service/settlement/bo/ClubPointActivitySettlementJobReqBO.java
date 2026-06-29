package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动积分发放 Job 请求
 */
@Data
@Accessors(chain = true)
public class ClubPointActivitySettlementJobReqBO {

    private String runKey;
    private Long activityId;
    private Integer triggerSource;
    private Integer retryCount;
    private Long handlerUserId;
    private LocalDateTime plannedTime;
    private LocalDateTime settlementTime;

}

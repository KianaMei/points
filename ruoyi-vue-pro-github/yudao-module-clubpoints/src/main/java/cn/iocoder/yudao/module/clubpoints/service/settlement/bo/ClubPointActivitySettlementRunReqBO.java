package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动积分发放运行请求
 */
@Data
@Accessors(chain = true)
public class ClubPointActivitySettlementRunReqBO {

    private Long activityId;
    private Long jobRunId;
    private String runKey;
    private Integer triggerSource;
    private Long operatorUserId;
    private LocalDateTime settlementTime;

}

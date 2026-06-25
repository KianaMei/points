package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动结算运行请求
 */
@Data
@Accessors(chain = true)
public class ClubPointActivitySettlementRunReqBO {

    private Long activityId;
    private String runKey;
    private Integer triggerSource;
    private Long operatorUserId;
    private LocalDateTime settlementTime;

}

package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 兑换超时处理请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionTimeoutReqBO {

    private Boolean operatorGlobalScope;
    private LocalDateTime appliedBefore;
    private LocalDateTime timeoutTime;
    private String reason;

}

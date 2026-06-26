package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 兑换取消请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionCancelReqBO {

    private Long id;
    private Long userId;
    private String reason;
    private LocalDateTime cancelTime;

}

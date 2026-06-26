package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 兑换申请请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionApplyReqBO {

    private Long batchId;
    private Long giftId;
    private Long userId;
    private Integer quantity;
    private String requestNo;
    private LocalDateTime applyTime;
    private String remark;

}

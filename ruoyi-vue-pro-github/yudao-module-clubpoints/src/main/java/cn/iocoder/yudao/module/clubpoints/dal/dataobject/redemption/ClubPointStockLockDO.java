package cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Stock lock DO.
 */
@TableName("club_points_stock_lock")
@KeySequence("club_points_stock_lock_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointStockLockDO extends BaseDO {

    @TableId
    private Long id;
    private Long giftId;
    private Long applicationId;
    private Long userId;
    private Integer quantity;
    private Integer status;
    private LocalDateTime lockedTime;
    private LocalDateTime usedTime;
    private LocalDateTime releasedTime;
    private String releaseReason;
    private String idempotencyKey;

}

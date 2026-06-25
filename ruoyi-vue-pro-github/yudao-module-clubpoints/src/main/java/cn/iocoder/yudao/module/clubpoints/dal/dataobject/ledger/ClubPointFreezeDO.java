package cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分冻结记录 DO
 */
@TableName("club_points_freeze")
@KeySequence("club_points_freeze_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointFreezeDO extends BaseDO {

    @TableId
    private Long id;
    private String freezeNo;
    private Long userId;
    private Integer points;
    private Integer status;
    private Integer sourceType;
    private Long sourceId;
    private LocalDateTime frozenAt;
    private LocalDateTime convertedAt;
    private LocalDateTime releasedAt;
    private String releaseReason;
    private Long convertedTransactionId;
    private String idempotencyKey;

}

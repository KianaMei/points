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
 * 员工积分账户缓存 DO
 */
@TableName("club_points_point_account")
@KeySequence("club_points_point_account_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointAccountDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    private Integer totalPositivePoints;
    private Integer totalNegativePoints;
    private Integer netPoints;
    private Integer frozenPoints;
    private Integer availablePoints;
    private Integer annualEarnedPoints;
    private Long lastTransactionId;
    private LocalDateTime lastTransactionTime;
    private LocalDateTime lastRebuildTime;
    private Integer version;

}

package cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Redemption gift DO.
 */
@TableName("club_points_redemption_gift")
@KeySequence("club_points_redemption_gift_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionGiftDO extends BaseDO {

    @TableId
    private Long id;
    private Long batchId;
    private String name;
    private String description;
    private Integer pointsCost;
    private Integer tierMinPoints;
    private Integer tierMaxPoints;
    private Long referenceAmountCent;
    private Integer stockTotal;
    private Integer stockLocked;
    private Integer stockUsed;
    private Integer status;
    private Long imageFileId;
    private Integer sort;
    private String giftSnapshotJson;

}

package cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 非签到积分材料明细 DO
 */
@TableName("club_points_contribution_item")
@KeySequence("club_points_contribution_item_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointContributionItemDO extends BaseDO {

    @TableId
    private Long id;
    private Long materialId;
    private Long clubId;
    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private Integer pointCategory;
    private Long ruleItemId;
    private String ruleItemCode;
    private Integer direction;
    private Integer points;
    private String reason;
    private String materialSummary;
    private Integer dutyMonth;
    private Long recommendedUserId;
    private Integer awardLevel;
    private String approvalResultSnapshot;
    private Long transactionId;
    private String idempotencyKey;
    private String effectiveUniqueKey;

}

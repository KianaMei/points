package cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 积分规则项 DO
 */
@TableName("club_points_rule_item")
@KeySequence("club_points_rule_item_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRuleItemDO extends BaseDO {

    @TableId
    private Long id;
    private Long ruleVersionId;
    private String itemCode;
    private String itemName;
    private Integer itemType;
    private Integer category;
    private Integer minPoints;
    private Integer maxPoints;
    private Integer defaultPoints;
    private Integer intValue;
    private BigDecimal decimalValue;
    private String textValue;
    private String jsonValue;
    private Integer status;
    private Integer sort;
    private String remark;

}

package cn.iocoder.yudao.module.clubpoints.service.rule.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 积分规则项保存参数
 */
@Data
@Accessors(chain = true)
public class ClubPointRuleItemSaveReqBO {

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

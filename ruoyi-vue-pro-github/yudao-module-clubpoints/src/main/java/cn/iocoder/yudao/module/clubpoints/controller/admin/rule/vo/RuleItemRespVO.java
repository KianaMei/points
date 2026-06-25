package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 规则项 Response VO")
@Data
@Accessors(chain = true)
public class RuleItemRespVO {

    @Schema(description = "规则项 ID", example = "1")
    private Long id;
    @Schema(description = "规则版本 ID", example = "1")
    private Long ruleVersionId;
    @Schema(description = "规则项编码", example = "ACTIVITY_SMALL_BASE")
    private String itemCode;
    @Schema(description = "规则项名称", example = "小型活动基础分")
    private String itemName;
    @Schema(description = "值类型", example = "1")
    private Integer itemType;
    @Schema(description = "规则分类", example = "10")
    private Integer category;
    @Schema(description = "最小分值", example = "5")
    private Integer minPoints;
    @Schema(description = "最大分值", example = "10")
    private Integer maxPoints;
    @Schema(description = "默认分值", example = "5")
    private Integer defaultPoints;
    @Schema(description = "整数值", example = "1")
    private Integer intValue;
    @Schema(description = "金额或小数值", example = "100.00")
    private BigDecimal decimalValue;
    @Schema(description = "文本值")
    private String textValue;
    @Schema(description = "JSON 值")
    private String jsonValue;
    @Schema(description = "状态", example = "1")
    private Integer status;
    @Schema(description = "排序", example = "1")
    private Integer sort;
    @Schema(description = "备注")
    private String remark;

}

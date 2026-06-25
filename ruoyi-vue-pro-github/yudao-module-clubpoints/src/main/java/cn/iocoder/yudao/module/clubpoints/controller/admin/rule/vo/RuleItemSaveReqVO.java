package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 规则项保存 Request VO")
@Data
@Accessors(chain = true)
public class RuleItemSaveReqVO {

    @Schema(description = "规则项 ID，修改时必填", example = "1")
    private Long id;

    @Schema(description = "规则版本 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "规则版本 ID 不能为空")
    private Long ruleVersionId;

    @Schema(description = "规则项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ACTIVITY_SMALL_BASE")
    @NotBlank(message = "规则项编码不能为空")
    private String itemCode;

    @Schema(description = "规则项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "小型活动基础分")
    @NotBlank(message = "规则项名称不能为空")
    private String itemName;

    @Schema(description = "值类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "值类型不能为空")
    @InEnum(value = ClubPointRuleItemTypeEnum.class)
    private Integer itemType;

    @Schema(description = "规则分类", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "规则分类不能为空")
    private Integer category;

    @Schema(description = "最小分值", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "最小分值不能为空")
    private Integer minPoints;

    @Schema(description = "最大分值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "最大分值不能为空")
    private Integer maxPoints;

    @Schema(description = "默认分值", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "默认分值不能为空")
    private Integer defaultPoints;

    @Schema(description = "整数值", example = "1")
    private Integer intValue;

    @Schema(description = "金额或小数值", example = "100.00")
    private BigDecimal decimalValue;

    @Schema(description = "文本值", example = "配置说明")
    private String textValue;

    @Schema(description = "JSON 值")
    private String jsonValue;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @AssertTrue(message = "默认分值必须在最小值和最大值之间")
    public boolean isDefaultPointsInRange() {
        if (minPoints == null || maxPoints == null || defaultPoints == null) {
            return true;
        }
        return minPoints <= maxPoints && defaultPoints >= minPoints && defaultPoints <= maxPoints;
    }

}

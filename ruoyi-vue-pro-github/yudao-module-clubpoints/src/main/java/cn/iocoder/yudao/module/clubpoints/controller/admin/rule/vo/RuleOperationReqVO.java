package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 规则版本发布、撤回、停用 Request VO")
@Data
@Accessors(chain = true)
public class RuleOperationReqVO {

    @Schema(description = "规则版本 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "规则版本 ID 不能为空")
    private Long id;

    @Schema(description = "操作原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "发布新规则")
    @NotBlank(message = "操作原因不能为空")
    private String reason;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 规则版本保存 Request VO")
@Data
@Accessors(chain = true)
public class RuleVersionSaveReqVO {

    @Schema(description = "规则版本 ID，修改时必填", example = "1")
    private Long id;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V2026.01")
    @NotBlank(message = "版本号不能为空")
    private String versionNo;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026 俱乐部积分制度")
    @NotBlank(message = "规则名称不能为空")
    private String name;

    @Schema(description = "公示时间", example = "2026-01-20 00:00:00")
    private LocalDateTime publicityTime;

    @Schema(description = "生效时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-02-01 00:00:00")
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime effectiveTime;

    @Schema(description = "规则摘要", example = "规则摘要")
    private String summary;

    @Schema(description = "规则内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "规则正文")
    @NotBlank(message = "规则内容不能为空")
    private String content;

    @Schema(description = "附件或链接")
    @Valid
    private List<AttachmentInputVO> attachments;

    @Schema(description = "备注", example = "备注")
    private String remark;

}

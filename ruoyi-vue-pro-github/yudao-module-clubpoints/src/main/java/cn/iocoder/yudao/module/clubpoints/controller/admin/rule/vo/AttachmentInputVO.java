package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 俱乐部积分附件输入 VO")
@Data
@Accessors(chain = true)
public class AttachmentInputVO {

    @Schema(description = "附件类型，1 文件，2 外部链接", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "附件类型不能为空")
    private Integer type;

    @Schema(description = "文件 ID", example = "10")
    private Long fileId;

    @Schema(description = "外部链接", example = "https://example.invalid/rule")
    private String url;

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "制度.pdf")
    @NotBlank(message = "附件名称不能为空")
    private String name;

    @Schema(description = "备注", example = "公示附件")
    private String remark;

    @AssertTrue(message = "文件附件必须填写文件 ID")
    public boolean isFileAttachmentValid() {
        return type == null || !Integer.valueOf(1).equals(type) || fileId != null;
    }

    @AssertTrue(message = "链接附件必须填写 URL")
    public boolean isUrlAttachmentValid() {
        return type == null || !Integer.valueOf(2).equals(type) || StringUtils.hasText(url);
    }

}

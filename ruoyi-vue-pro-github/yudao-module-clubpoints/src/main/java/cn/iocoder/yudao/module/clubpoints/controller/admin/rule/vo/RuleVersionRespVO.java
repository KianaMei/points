package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 规则版本 Response VO")
@Data
@Accessors(chain = true)
public class RuleVersionRespVO {

    @Schema(description = "规则版本 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;
    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V2026.01")
    private String versionNo;
    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026 俱乐部积分制度")
    private String name;
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;
    @Schema(description = "公示时间")
    private LocalDateTime publicityTime;
    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;
    @Schema(description = "发布时间")
    private LocalDateTime publishedTime;
    @Schema(description = "停用时间")
    private LocalDateTime disabledTime;
    @Schema(description = "规则摘要")
    private String summary;
    @Schema(description = "规则内容")
    private String content;
    @Schema(description = "附件或链接")
    private List<AttachmentInputVO> attachments;
    @Schema(description = "备注")
    private String remark;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - 任务运行人工处理 Request VO")
@Data
@Accessors(chain = true)
public class AdminJobRunHandleReqVO {

    @Schema(description = "任务运行记录 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "任务运行记录 ID 不能为空")
    private Long id;

    @Schema(description = "处理原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "处理原因不能为空")
    @Size(max = 1024, message = "处理原因不能超过 1024 个字符")
    private String reason;

}

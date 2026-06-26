package cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo;

import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 管理员代录非签到积分 Request VO")
@Data
@Accessors(chain = true)
public class AdminContributionDirectCreateReqVO {

    @NotBlank(message = "请求号不能为空")
    private String requestNo;

    private Long clubId;

    @NotNull(message = "材料类型不能为空")
    private Integer type;

    @NotNull(message = "员工 ID 不能为空")
    private Long userId;

    @NotBlank(message = "员工姓名快照不能为空")
    private String userNameSnapshot;

    private String deptNameSnapshot;

    @NotNull(message = "积分不能为空")
    private Integer points;

    @NotNull(message = "规则版本 ID 不能为空")
    private Long ruleVersionId;

    @NotBlank(message = "原因不能为空")
    private String reason;

    @Valid
    @NotEmpty(message = "附件不能为空")
    private List<AttachmentInputVO> attachments;

}

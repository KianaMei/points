package cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo;

import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "负责人端 - 非签到积分材料保存 Request VO")
@Data
@Accessors(chain = true)
public class LeaderContributionSaveReqVO {

    private Long id;

    @NotNull(message = "俱乐部 ID 不能为空")
    private Long clubId;

    @NotNull(message = "材料类型不能为空")
    private Integer type;

    @NotBlank(message = "材料标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "规则版本 ID 不能为空")
    private Long ruleVersionId;

    @Valid
    @NotEmpty(message = "材料明细不能为空")
    private List<LeaderContributionItemReqVO> items;

    @Valid
    private List<AttachmentInputVO> attachments;

}

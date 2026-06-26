package cn.iocoder.yudao.module.clubpoints.controller.leader.contribution.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "负责人端 - 非签到积分材料明细 Request VO")
@Data
@Accessors(chain = true)
public class LeaderContributionItemReqVO {

    @NotNull(message = "员工 ID 不能为空")
    private Long userId;

    @NotBlank(message = "员工姓名快照不能为空")
    private String userNameSnapshot;

    private String deptNameSnapshot;

    @NotNull(message = "积分不能为空")
    private Integer points;

    @NotBlank(message = "发放原因不能为空")
    private String reason;

    private String materialSummary;
    private Integer dutyMonth;
    private Long recommendedUserId;
    private Integer awardLevel;
    private String approvalResultSnapshot;

}

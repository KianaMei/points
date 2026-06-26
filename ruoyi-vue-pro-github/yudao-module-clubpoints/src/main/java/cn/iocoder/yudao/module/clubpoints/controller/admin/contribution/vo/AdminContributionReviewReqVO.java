package cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 非签到积分材料审核 Request VO")
@Data
@Accessors(chain = true)
public class AdminContributionReviewReqVO {

    @NotNull(message = "材料 ID 不能为空")
    private Long id;

    @NotNull(message = "审核结果不能为空")
    private Integer result;

    private String reason;

}

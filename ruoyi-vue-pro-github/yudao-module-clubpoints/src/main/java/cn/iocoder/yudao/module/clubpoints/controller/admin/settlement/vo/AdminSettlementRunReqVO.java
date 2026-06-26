package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AdminSettlementRunReqVO {

    @NotNull
    private Long activityId;
    private Boolean force;
    @NotBlank
    private String reason;

}

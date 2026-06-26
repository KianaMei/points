package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AdminRedemptionGiftStatusReqVO {

    @NotNull
    private Long id;
    @NotNull
    private Integer status;
    private String reason;

}

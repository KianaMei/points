package cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AppRedemptionApplyReqVO {

    @NotNull
    private Long batchId;
    @NotNull
    private Long giftId;
    @NotNull
    private Integer quantity;
    @NotBlank
    private String requestNo;
    private String remark;

}

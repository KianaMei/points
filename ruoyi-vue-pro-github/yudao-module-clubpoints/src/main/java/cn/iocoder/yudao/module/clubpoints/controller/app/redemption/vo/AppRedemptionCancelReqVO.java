package cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AppRedemptionCancelReqVO {

    @NotNull
    private Long id;
    private String reason;

}

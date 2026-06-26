package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AdminRedemptionBatchOperationReqVO {

    @NotNull
    private Long id;
    private String reason;

}

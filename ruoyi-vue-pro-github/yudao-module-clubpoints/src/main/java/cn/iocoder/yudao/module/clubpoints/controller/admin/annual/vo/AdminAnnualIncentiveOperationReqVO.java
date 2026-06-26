package cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AdminAnnualIncentiveOperationReqVO {

    @NotNull
    private Long id;
    private String reason;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminRedemptionEligibilityPageReqVO extends PageParam {

    @NotNull
    private Long batchId;
    private Boolean qualified;
    private Long userId;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminRedemptionBatchPageReqVO extends PageParam {

    private Integer year;
    private Integer status;
    private String keyword;

}

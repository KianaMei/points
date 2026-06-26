package cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppRedemptionBatchPageReqVO extends PageParam {

    private Integer year;
    private String keyword;

}

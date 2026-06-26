package cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppDisputePageReqVO extends PageParam {

    private Integer status;

}

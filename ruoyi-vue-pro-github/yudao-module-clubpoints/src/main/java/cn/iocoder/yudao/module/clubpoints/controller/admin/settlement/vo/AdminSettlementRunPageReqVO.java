package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminSettlementRunPageReqVO extends PageParam {

    private Long activityId;
    private Integer status;

}

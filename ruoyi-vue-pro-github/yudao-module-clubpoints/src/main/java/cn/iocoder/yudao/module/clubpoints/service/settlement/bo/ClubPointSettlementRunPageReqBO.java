package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointSettlementRunPageReqBO extends PageParam {

    private Long activityId;
    private Integer status;

}

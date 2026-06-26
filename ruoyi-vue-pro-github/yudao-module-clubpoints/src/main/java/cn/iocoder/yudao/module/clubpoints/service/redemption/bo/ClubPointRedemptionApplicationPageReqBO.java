package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 兑换申请分页查询参数
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionApplicationPageReqBO extends PageParam {

    private Long batchId;
    private Long userId;
    private Integer status;

}

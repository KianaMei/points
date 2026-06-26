package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 兑换资格快照分页查询参数
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRedemptionEligibilityPageReqBO extends PageParam {

    private Long batchId;
    private Boolean qualified;
    private Long userId;

}

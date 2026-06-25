package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 积分账户分页查询 BO
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointAccountPageReqBO extends PageParam {

    private Long userId;
    private Long clubId;

}

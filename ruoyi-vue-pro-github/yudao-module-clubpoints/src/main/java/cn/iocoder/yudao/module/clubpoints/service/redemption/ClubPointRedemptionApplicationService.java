package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplyReqBO;

import java.util.List;

/**
 * 兑换申请服务
 */
public interface ClubPointRedemptionApplicationService {

    List<ClubPointRedemptionGiftDO> listAvailableGifts(Long batchId, Long userId);

    Long apply(ClubPointRedemptionApplyReqBO reqBO);

}

package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftSaveReqBO;

/**
 * 兑换礼品服务
 */
public interface ClubPointRedemptionGiftService {

    PageResult<ClubPointRedemptionGiftDO> getAdminGiftPage(boolean operatorGlobalScope,
                                                           ClubPointRedemptionGiftPageReqBO reqBO);

    Long createGift(ClubPointRedemptionGiftSaveReqBO reqBO);

    void updateGift(ClubPointRedemptionGiftSaveReqBO reqBO);

    void updateGiftStatus(Long giftId, Integer status, ClubPointRedemptionGiftOperationReqBO reqBO);

    void lockStock(Long giftId, Integer quantity);

    void releaseLockedStock(Long giftId, Integer quantity);

    void useLockedStock(Long giftId, Integer quantity);

}

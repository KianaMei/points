package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionApplyReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionReviewReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionTimeoutReqBO;

import java.util.List;

/**
 * 兑换申请服务
 */
public interface ClubPointRedemptionApplicationService {

    List<ClubPointRedemptionGiftDO> listAvailableGifts(Long batchId, Long userId);

    Long apply(ClubPointRedemptionApplyReqBO reqBO);

    List<ClubPointRedemptionApplicationDO> listPendingReviewApplications(boolean operatorGlobalScope);

    void review(ClubPointRedemptionReviewReqBO reqBO);

    void cancelOwnApplication(ClubPointRedemptionCancelReqBO reqBO);

    int timeoutPendingApplications(ClubPointRedemptionTimeoutReqBO reqBO);

}

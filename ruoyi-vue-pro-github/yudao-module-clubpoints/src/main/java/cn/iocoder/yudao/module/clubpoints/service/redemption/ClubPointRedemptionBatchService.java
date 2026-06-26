package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchSaveReqBO;

/**
 * 兑换批次服务
 */
public interface ClubPointRedemptionBatchService {

    PageResult<ClubPointRedemptionBatchDO> getAdminBatchPage(boolean operatorGlobalScope,
                                                             ClubPointRedemptionBatchPageReqBO reqBO);

    PageResult<ClubPointRedemptionBatchDO> getAppOpenBatchPage(ClubPointRedemptionBatchPageReqBO reqBO);

    Long createBatch(ClubPointRedemptionBatchSaveReqBO reqBO);

    void updateBatch(ClubPointRedemptionBatchSaveReqBO reqBO);

    void openBatch(Long batchId, ClubPointRedemptionBatchOperationReqBO reqBO);

    void closeBatch(Long batchId, ClubPointRedemptionBatchOperationReqBO reqBO);

    void validateBatchOpenForApply(Long batchId);

}

package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchSaveReqBO;

/**
 * 兑换批次服务
 */
public interface ClubPointRedemptionBatchService {

    Long createBatch(ClubPointRedemptionBatchSaveReqBO reqBO);

    void updateBatch(ClubPointRedemptionBatchSaveReqBO reqBO);

    void openBatch(Long batchId, ClubPointRedemptionBatchOperationReqBO reqBO);

    void closeBatch(Long batchId, ClubPointRedemptionBatchOperationReqBO reqBO);

    void validateBatchOpenForApply(Long batchId);

}

package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionEligibilityPageReqBO;

import java.util.List;

/**
 * 兑换资格快照服务
 */
public interface ClubPointRedemptionEligibilityService {

    PageResult<ClubPointRedemptionEligibilitySnapshotDO> getAdminSnapshotPage(
            boolean operatorGlobalScope, ClubPointRedemptionEligibilityPageReqBO reqBO);

    List<ClubPointRedemptionEligibilitySnapshotDO> listBatchSnapshots(Long batchId, Boolean qualified,
                                                                      boolean operatorGlobalScope);

    ClubPointRedemptionEligibilitySnapshotDO getUserSnapshot(Long batchId, Long userId);

    ClubPointRedemptionEligibilitySnapshotDO validateUserQualifiedForApply(Long batchId, Long userId);

}

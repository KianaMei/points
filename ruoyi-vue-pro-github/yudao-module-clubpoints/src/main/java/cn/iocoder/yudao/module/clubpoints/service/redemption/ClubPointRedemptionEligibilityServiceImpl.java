package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_ELIGIBILITY_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_ELIGIBILITY_NOT_QUALIFIED;

/**
 * 兑换资格快照服务实现
 */
@Service
public class ClubPointRedemptionEligibilityServiceImpl implements ClubPointRedemptionEligibilityService {

    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    public List<ClubPointRedemptionEligibilitySnapshotDO> listBatchSnapshots(Long batchId, Boolean qualified,
                                                                            boolean operatorGlobalScope) {
        clubScopeService.validateGlobal(operatorGlobalScope);
        validateBatchExists(batchId);
        return eligibilitySnapshotMapper.selectListByBatchIdAndQualified(batchId, qualified);
    }

    @Override
    public ClubPointRedemptionEligibilitySnapshotDO getUserSnapshot(Long batchId, Long userId) {
        ClubPointRedemptionEligibilitySnapshotDO snapshot =
                eligibilitySnapshotMapper.selectByBatchIdAndUserId(batchId, userId);
        if (snapshot == null) {
            throw exception(CLUB_REDEMPTION_ELIGIBILITY_NOT_EXISTS);
        }
        return snapshot;
    }

    @Override
    public ClubPointRedemptionEligibilitySnapshotDO validateUserQualifiedForApply(Long batchId, Long userId) {
        ClubPointRedemptionEligibilitySnapshotDO snapshot = getUserSnapshot(batchId, userId);
        if (!Boolean.TRUE.equals(snapshot.getQualified())) {
            throw exception(CLUB_REDEMPTION_ELIGIBILITY_NOT_QUALIFIED);
        }
        return snapshot;
    }

    private ClubPointRedemptionBatchDO validateBatchExists(Long batchId) {
        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw exception(CLUB_REDEMPTION_BATCH_NOT_EXISTS);
        }
        return batch;
    }

}

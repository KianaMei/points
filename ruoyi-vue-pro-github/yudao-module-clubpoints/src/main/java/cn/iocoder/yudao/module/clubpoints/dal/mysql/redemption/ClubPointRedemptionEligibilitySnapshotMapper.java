package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointRedemptionEligibilitySnapshotMapper
        extends BaseMapperX<ClubPointRedemptionEligibilitySnapshotDO> {

    default List<ClubPointRedemptionEligibilitySnapshotDO> selectListByBatchId(Long batchId) {
        return selectList(new LambdaQueryWrapperX<ClubPointRedemptionEligibilitySnapshotDO>()
                .eq(ClubPointRedemptionEligibilitySnapshotDO::getBatchId, batchId)
                .orderByAsc(ClubPointRedemptionEligibilitySnapshotDO::getRankNo)
                .orderByAsc(ClubPointRedemptionEligibilitySnapshotDO::getUserId));
    }

    default Long selectCountByBatchId(Long batchId) {
        return selectCount(new LambdaQueryWrapperX<ClubPointRedemptionEligibilitySnapshotDO>()
                .eq(ClubPointRedemptionEligibilitySnapshotDO::getBatchId, batchId));
    }

    default ClubPointRedemptionEligibilitySnapshotDO selectByBatchIdAndUserId(Long batchId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRedemptionEligibilitySnapshotDO>()
                .eq(ClubPointRedemptionEligibilitySnapshotDO::getBatchId, batchId)
                .eq(ClubPointRedemptionEligibilitySnapshotDO::getUserId, userId));
    }

}

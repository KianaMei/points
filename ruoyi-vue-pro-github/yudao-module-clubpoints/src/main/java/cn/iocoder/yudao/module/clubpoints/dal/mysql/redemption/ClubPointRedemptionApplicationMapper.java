package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ClubPointRedemptionApplicationMapper extends BaseMapperX<ClubPointRedemptionApplicationDO> {

    default PageResult<ClubPointRedemptionApplicationDO> selectPage(PageParam pageParam, Long batchId, Long userId,
                                                                   Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eqIfPresent(ClubPointRedemptionApplicationDO::getBatchId, batchId)
                .eqIfPresent(ClubPointRedemptionApplicationDO::getUserId, userId)
                .eqIfPresent(ClubPointRedemptionApplicationDO::getStatus, status)
                .orderByDesc(ClubPointRedemptionApplicationDO::getApplyTime)
                .orderByDesc(ClubPointRedemptionApplicationDO::getId));
    }

    default ClubPointRedemptionApplicationDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointRedemptionApplicationDO::getId, id);
    }

    default List<ClubPointRedemptionApplicationDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getStatus, status)
                .orderByAsc(ClubPointRedemptionApplicationDO::getApplyTime)
                .orderByAsc(ClubPointRedemptionApplicationDO::getId));
    }

    default List<ClubPointRedemptionApplicationDO> selectListByStatusAppliedBefore(Integer status,
                                                                                  LocalDateTime appliedBefore) {
        return selectList(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getStatus, status)
                .le(ClubPointRedemptionApplicationDO::getApplyTime, appliedBefore)
                .orderByAsc(ClubPointRedemptionApplicationDO::getApplyTime)
                .orderByAsc(ClubPointRedemptionApplicationDO::getId));
    }

    default ClubPointRedemptionApplicationDO selectByApplicationNo(String applicationNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getApplicationNo, applicationNo));
    }

    default ClubPointRedemptionApplicationDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getIdempotencyKey, idempotencyKey));
    }

}

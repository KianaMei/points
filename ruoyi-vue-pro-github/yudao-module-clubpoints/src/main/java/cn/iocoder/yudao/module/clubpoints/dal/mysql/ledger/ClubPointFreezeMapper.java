package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointFreezeMapper extends BaseMapperX<ClubPointFreezeDO> {

    default List<ClubPointFreezeDO> selectFrozenListForRebuild() {
        return selectList(new LambdaQueryWrapperX<ClubPointFreezeDO>()
                .eq(ClubPointFreezeDO::getStatus, 1)
                .orderByAsc(ClubPointFreezeDO::getUserId));
    }

    default List<ClubPointFreezeDO> selectFrozenListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ClubPointFreezeDO>()
                .eq(ClubPointFreezeDO::getUserId, userId)
                .eq(ClubPointFreezeDO::getStatus, 1));
    }

    default ClubPointFreezeDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointFreezeDO::getId, id);
    }

    default ClubPointFreezeDO selectByFreezeNo(String freezeNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointFreezeDO>()
                .eq(ClubPointFreezeDO::getFreezeNo, freezeNo));
    }

    default ClubPointFreezeDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointFreezeDO>()
                .eq(ClubPointFreezeDO::getIdempotencyKey, idempotencyKey));
    }

    default ClubPointFreezeDO selectBySource(Integer sourceType, Long sourceId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointFreezeDO>()
                .eq(ClubPointFreezeDO::getSourceType, sourceType)
                .eq(ClubPointFreezeDO::getSourceId, sourceId));
    }

}

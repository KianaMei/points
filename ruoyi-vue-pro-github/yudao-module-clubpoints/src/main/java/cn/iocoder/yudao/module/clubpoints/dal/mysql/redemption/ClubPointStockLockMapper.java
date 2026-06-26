package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointStockLockMapper extends BaseMapperX<ClubPointStockLockDO> {

    default ClubPointStockLockDO selectByApplicationId(Long applicationId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointStockLockDO>()
                .eq(ClubPointStockLockDO::getApplicationId, applicationId));
    }

    default ClubPointStockLockDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointStockLockDO>()
                .eq(ClubPointStockLockDO::getIdempotencyKey, idempotencyKey));
    }

}

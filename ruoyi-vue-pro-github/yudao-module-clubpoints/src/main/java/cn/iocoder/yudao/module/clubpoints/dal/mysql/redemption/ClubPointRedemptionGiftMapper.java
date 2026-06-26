package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointRedemptionGiftMapper extends BaseMapperX<ClubPointRedemptionGiftDO> {

    default List<ClubPointRedemptionGiftDO> selectListByBatchId(Long batchId) {
        return selectList(new LambdaQueryWrapperX<ClubPointRedemptionGiftDO>()
                .eq(ClubPointRedemptionGiftDO::getBatchId, batchId)
                .orderByAsc(ClubPointRedemptionGiftDO::getSort)
                .orderByAsc(ClubPointRedemptionGiftDO::getId));
    }

}

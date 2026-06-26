package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    default ClubPointRedemptionGiftDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointRedemptionGiftDO::getId, id);
    }

    default int increaseLockedStock(Long id, Integer onShelfStatus, Integer quantity) {
        return update(null, new LambdaUpdateWrapper<ClubPointRedemptionGiftDO>()
                .eq(ClubPointRedemptionGiftDO::getId, id)
                .eq(ClubPointRedemptionGiftDO::getStatus, onShelfStatus)
                .apply("stock_total - stock_locked - stock_used >= {0}", quantity)
                .setSql("stock_locked = stock_locked + " + quantity));
    }

    default int decreaseLockedStock(Long id, Integer quantity) {
        return update(null, new LambdaUpdateWrapper<ClubPointRedemptionGiftDO>()
                .eq(ClubPointRedemptionGiftDO::getId, id)
                .ge(ClubPointRedemptionGiftDO::getStockLocked, quantity)
                .setSql("stock_locked = stock_locked - " + quantity));
    }

    default int convertLockedStockToUsed(Long id, Integer quantity) {
        return update(null, new LambdaUpdateWrapper<ClubPointRedemptionGiftDO>()
                .eq(ClubPointRedemptionGiftDO::getId, id)
                .ge(ClubPointRedemptionGiftDO::getStockLocked, quantity)
                .setSql("stock_locked = stock_locked - " + quantity)
                .setSql("stock_used = stock_used + " + quantity));
    }

}

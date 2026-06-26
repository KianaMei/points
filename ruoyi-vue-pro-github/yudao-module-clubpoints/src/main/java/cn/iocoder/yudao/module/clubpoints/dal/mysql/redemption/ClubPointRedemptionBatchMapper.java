package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ClubPointRedemptionBatchMapper extends BaseMapperX<ClubPointRedemptionBatchDO> {

    default PageResult<ClubPointRedemptionBatchDO> selectPage(PageParam pageParam, Integer year, Integer status,
                                                             String keyword, Collection<Integer> statuses) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointRedemptionBatchDO>()
                .eqIfPresent(ClubPointRedemptionBatchDO::getYear, year)
                .eqIfPresent(ClubPointRedemptionBatchDO::getStatus, status)
                .inIfPresent(ClubPointRedemptionBatchDO::getStatus, statuses)
                .likeIfPresent(ClubPointRedemptionBatchDO::getName, keyword)
                .orderByDesc(ClubPointRedemptionBatchDO::getOpenTime)
                .orderByDesc(ClubPointRedemptionBatchDO::getId));
    }

    default ClubPointRedemptionBatchDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointRedemptionBatchDO::getId, id);
    }

}

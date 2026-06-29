package cn.iocoder.yudao.module.clubpoints.dal.mysql.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Mapper
public interface ClubPointActivitySettlementRunMapper extends BaseMapperX<ClubPointActivitySettlementRunDO> {

    default ClubPointActivitySettlementRunDO selectByRunKey(String runKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointActivitySettlementRunDO>()
                .eq(ClubPointActivitySettlementRunDO::getRunKey, runKey));
    }

    default PageResult<ClubPointActivitySettlementRunDO> selectPage(PageParam pageParam, Long activityId,
                                                                    Collection<Long> activityIds, Integer status,
                                                                    LocalDateTime startTime, LocalDateTime endTime) {
        if (activityIds != null && activityIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointActivitySettlementRunDO>()
                .eqIfPresent(ClubPointActivitySettlementRunDO::getActivityId, activityId)
                .inIfPresent(ClubPointActivitySettlementRunDO::getActivityId, activityIds)
                .eqIfPresent(ClubPointActivitySettlementRunDO::getStatus, status)
                .betweenIfPresent(ClubPointActivitySettlementRunDO::getSettlementTime, startTime, endTime)
                .orderByDesc(ClubPointActivitySettlementRunDO::getSettlementTime)
                .orderByDesc(ClubPointActivitySettlementRunDO::getId));
    }

}

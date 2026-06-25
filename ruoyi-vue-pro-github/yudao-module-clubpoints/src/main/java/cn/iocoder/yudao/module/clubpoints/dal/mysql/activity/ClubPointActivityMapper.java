package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;

@Mapper
public interface ClubPointActivityMapper extends BaseMapperX<ClubPointActivityDO> {

    default PageResult<ClubPointActivityDO> selectPage(PageParam pageParam, Collection<Long> clubIds,
                                                       Long clubId, String keyword, Integer status,
                                                       Collection<Integer> statuses,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointActivityDO>()
                .inIfPresent(ClubPointActivityDO::getClubId, clubIds)
                .eqIfPresent(ClubPointActivityDO::getClubId, clubId)
                .likeIfPresent(ClubPointActivityDO::getTitle, keyword)
                .eqIfPresent(ClubPointActivityDO::getStatus, status)
                .inIfPresent(ClubPointActivityDO::getStatus, statuses)
                .geIfPresent(ClubPointActivityDO::getStartTime, startTime)
                .leIfPresent(ClubPointActivityDO::getEndTime, endTime)
                .orderByDesc(ClubPointActivityDO::getStartTime)
                .orderByDesc(ClubPointActivityDO::getId));
    }

}

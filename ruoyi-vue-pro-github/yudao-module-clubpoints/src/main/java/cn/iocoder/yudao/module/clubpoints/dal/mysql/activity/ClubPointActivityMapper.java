package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ClubPointActivityMapper extends BaseMapperX<ClubPointActivityDO> {

    default ClubPointActivityDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointActivityDO::getId, id);
    }

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

    default PageResult<ClubPointActivityDO> selectSettlementPendingPage(PageParam pageParam, Long clubId,
                                                                        String clubName, String activityTitle,
                                                                        LocalDateTime startTime,
                                                                        LocalDateTime endTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointActivityDO>()
                .eqIfPresent(ClubPointActivityDO::getClubId, clubId)
                .likeIfPresent(ClubPointActivityDO::getClubNameSnapshot, clubName)
                .likeIfPresent(ClubPointActivityDO::getTitle, activityTitle)
                .eq(ClubPointActivityDO::getStatus, ClubPointActivityStatusEnum.ENDED.getStatus())
                .geIfPresent(ClubPointActivityDO::getStartTime, startTime)
                .leIfPresent(ClubPointActivityDO::getEndTime, endTime)
                .orderByDesc(ClubPointActivityDO::getStartTime)
                .orderByDesc(ClubPointActivityDO::getId));
    }

    default List<ClubPointActivityDO> selectListBySettlementBusinessFilter(String clubName, String activityTitle,
                                                                           LocalDateTime startTime,
                                                                           LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<ClubPointActivityDO>()
                .likeIfPresent(ClubPointActivityDO::getClubNameSnapshot, clubName)
                .likeIfPresent(ClubPointActivityDO::getTitle, activityTitle)
                .geIfPresent(ClubPointActivityDO::getStartTime, startTime)
                .leIfPresent(ClubPointActivityDO::getEndTime, endTime)
                .orderByDesc(ClubPointActivityDO::getStartTime)
                .orderByDesc(ClubPointActivityDO::getId));
    }

    default List<ClubPointActivityDO> selectListByClubIds(Collection<Long> clubIds) {
        return selectList(new LambdaQueryWrapperX<ClubPointActivityDO>()
                .in(ClubPointActivityDO::getClubId, clubIds)
                .orderByDesc(ClubPointActivityDO::getStartTime)
                .orderByDesc(ClubPointActivityDO::getId));
    }

    default List<ClubPointActivityDO> selectAutoSettlementCandidates() {
        return selectList(new LambdaQueryWrapperX<ClubPointActivityDO>()
                .eq(ClubPointActivityDO::getStatus, ClubPointActivityStatusEnum.ENDED.getStatus())
                .orderByAsc(ClubPointActivityDO::getEndTime)
                .orderByAsc(ClubPointActivityDO::getId));
    }

}

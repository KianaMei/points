package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointActivityRegistrationMapper extends BaseMapperX<ClubPointActivityRegistrationDO> {

    default ClubPointActivityRegistrationDO selectByActivityIdAndUserIdAndStatus(Long activityId,
                                                                                 Long userId,
                                                                                 Integer status) {
        return selectOne(new LambdaQueryWrapperX<ClubPointActivityRegistrationDO>()
                .eq(ClubPointActivityRegistrationDO::getActivityId, activityId)
                .eq(ClubPointActivityRegistrationDO::getUserId, userId)
                .eq(ClubPointActivityRegistrationDO::getStatus, status));
    }

    default PageResult<ClubPointActivityRegistrationDO> selectPage(PageParam pageParam, Long clubId,
                                                                   Long activityId, Integer status,
                                                                   Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointActivityRegistrationDO>()
                .eqIfPresent(ClubPointActivityRegistrationDO::getClubId, clubId)
                .eqIfPresent(ClubPointActivityRegistrationDO::getActivityId, activityId)
                .eqIfPresent(ClubPointActivityRegistrationDO::getStatus, status)
                .eqIfPresent(ClubPointActivityRegistrationDO::getUserId, userId)
                .orderByDesc(ClubPointActivityRegistrationDO::getRegisterTime)
                .orderByDesc(ClubPointActivityRegistrationDO::getId));
    }

}

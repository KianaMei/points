package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ClubPointAttendanceRecordMapper extends BaseMapperX<ClubPointAttendanceRecordDO> {

    default ClubPointAttendanceRecordDO selectByRegistrationIdAndTargetType(Long registrationId, Integer targetType) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAttendanceRecordDO>()
                .eq(ClubPointAttendanceRecordDO::getRegistrationId, registrationId)
                .eq(ClubPointAttendanceRecordDO::getTargetType, targetType));
    }

    default PageResult<ClubPointAttendanceRecordDO> selectPageByActivityIds(PageParam pageParam,
                                                                            Collection<Long> activityIds,
                                                                            Long activityId,
                                                                            Long registrationId,
                                                                            Long userId,
                                                                            Integer targetType) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointAttendanceRecordDO>()
                .in(ClubPointAttendanceRecordDO::getActivityId, activityIds)
                .eqIfPresent(ClubPointAttendanceRecordDO::getActivityId, activityId)
                .eqIfPresent(ClubPointAttendanceRecordDO::getRegistrationId, registrationId)
                .eqIfPresent(ClubPointAttendanceRecordDO::getUserId, userId)
                .eqIfPresent(ClubPointAttendanceRecordDO::getTargetType, targetType)
                .orderByDesc(ClubPointAttendanceRecordDO::getRecordTime)
                .orderByDesc(ClubPointAttendanceRecordDO::getId));
    }

}

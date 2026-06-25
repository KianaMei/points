package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointAttendanceRecordMapper extends BaseMapperX<ClubPointAttendanceRecordDO> {

    default ClubPointAttendanceRecordDO selectByRegistrationIdAndTargetType(Long registrationId, Integer targetType) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAttendanceRecordDO>()
                .eq(ClubPointAttendanceRecordDO::getRegistrationId, registrationId)
                .eq(ClubPointAttendanceRecordDO::getTargetType, targetType));
    }

}

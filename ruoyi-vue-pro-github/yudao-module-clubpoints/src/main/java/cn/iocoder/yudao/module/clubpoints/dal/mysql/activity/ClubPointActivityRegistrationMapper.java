package cn.iocoder.yudao.module.clubpoints.dal.mysql.activity;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ClubPointActivityRegistrationMapper extends BaseMapperX<ClubPointActivityRegistrationDO> {

    default List<ClubPointActivityRegistrationDO> selectListByActivityId(Long activityId) {
        return selectList(new LambdaQueryWrapperX<ClubPointActivityRegistrationDO>()
                .eq(ClubPointActivityRegistrationDO::getActivityId, activityId)
                .orderByAsc(ClubPointActivityRegistrationDO::getId));
    }

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

    @Select("SELECT r.* FROM club_points_activity_registration r"
            + " JOIN club_points_activity a ON a.id = r.activity_id"
            + " LEFT JOIN club_points_attendance_record c"
            + " ON c.registration_id = r.id AND c.target_type = #{checkinTargetType}"
            + " WHERE r.user_id = #{userId}"
            + " AND r.status = #{registrationStatus}"
            + " AND (r.no_absence_deduct IS NULL OR r.no_absence_deduct = FALSE)"
            + " AND (r.special_absence_flag IS NULL OR r.special_absence_flag = FALSE)"
            + " AND a.status IN (#{endedStatus}, #{settledStatus})"
            + " AND a.end_time >= #{monthStart}"
            + " AND a.end_time < #{monthEnd}"
            + " AND c.id IS NULL"
            + " ORDER BY a.end_time ASC, r.id ASC")
    List<ClubPointActivityRegistrationDO> selectMonthlyUnexcusedAbsenceList(@Param("userId") Long userId,
                                                                            @Param("monthStart") LocalDateTime monthStart,
                                                                            @Param("monthEnd") LocalDateTime monthEnd,
                                                                            @Param("registrationStatus") Integer registrationStatus,
                                                                            @Param("endedStatus") Integer endedStatus,
                                                                            @Param("settledStatus") Integer settledStatus,
                                                                            @Param("checkinTargetType") Integer checkinTargetType);

    @Select("SELECT COUNT(1) FROM club_points_activity_registration r"
            + " JOIN club_points_activity a ON a.id = r.activity_id"
            + " WHERE r.user_id = #{userId}"
            + " AND r.status = #{registrationStatus}"
            + " AND a.end_time > #{now}")
    Long selectRegisteredActiveCountByUserId(@Param("userId") Long userId,
                                             @Param("registrationStatus") Integer registrationStatus,
                                             @Param("now") LocalDateTime now);

    @Select({"<script>",
            "SELECT COUNT(1) FROM club_points_activity_registration r",
            "JOIN club_points_activity a ON a.id = r.activity_id",
            "LEFT JOIN club_points_attendance_record c",
            "ON c.registration_id = r.id AND c.target_type = #{checkinTargetType}",
            "WHERE r.club_id IN",
            "<foreach collection='clubIds' item='clubId' open='(' separator=',' close=')'>",
            "#{clubId}",
            "</foreach>",
            "AND r.status = #{registrationStatus}",
            "AND (r.no_absence_deduct IS NULL OR r.no_absence_deduct = FALSE)",
            "AND (r.special_absence_flag IS NULL OR r.special_absence_flag = FALSE)",
            "AND a.status IN (#{endedStatus}, #{settledStatus})",
            "AND c.id IS NULL",
            "</script>"})
    Long selectAttendanceExceptionCountByClubIds(@Param("clubIds") Collection<Long> clubIds,
                                                 @Param("registrationStatus") Integer registrationStatus,
                                                 @Param("endedStatus") Integer endedStatus,
                                                 @Param("settledStatus") Integer settledStatus,
                                                 @Param("checkinTargetType") Integer checkinTargetType);

}

package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface ClubMemberMapper extends BaseMapperX<ClubMemberDO> {

    default ClubMemberDO selectByUserIdAndClubIdAndStatus(Long userId, Long clubId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<ClubMemberDO>()
                .eq(ClubMemberDO::getUserId, userId)
                .eq(ClubMemberDO::getClubId, clubId)
                .eq(ClubMemberDO::getStatus, status));
    }

    default PageResult<ClubMemberDO> selectPageByClubIdAndStatus(PageParam pageParam, Long clubId,
                                                                 Integer status, Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubMemberDO>()
                .eq(ClubMemberDO::getClubId, clubId)
                .eq(ClubMemberDO::getStatus, status)
                .eqIfPresent(ClubMemberDO::getUserId, userId)
                .orderByDesc(ClubMemberDO::getId));
    }

    @Update("UPDATE club_points_activity_registration"
            + " SET status = #{status}, cancel_time = #{cancelTime}, cancel_reason_type = #{cancelReasonType},"
            + " cancel_reason = #{cancelReason}, cancel_operator_user_id = #{operatorUserId},"
            + " no_absence_deduct = TRUE, active_unique_key = NULL, update_time = CURRENT_TIMESTAMP"
            + " WHERE club_id = #{clubId} AND user_id = #{userId} AND status = 1")
    int cancelActiveRegistrationsByMemberChange(@Param("clubId") Long clubId,
                                                @Param("userId") Long userId,
                                                @Param("status") Integer status,
                                                @Param("cancelReasonType") Integer cancelReasonType,
                                                @Param("cancelReason") String cancelReason,
                                                @Param("operatorUserId") Long operatorUserId,
                                                @Param("cancelTime") LocalDateTime cancelTime);

}

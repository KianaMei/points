package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClubLeaderMapper extends BaseMapperX<ClubLeaderDO> {

    default ClubLeaderDO selectByUserIdAndClubIdAndStatus(Long userId, Long clubId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<ClubLeaderDO>()
                .eq(ClubLeaderDO::getUserId, userId)
                .eq(ClubLeaderDO::getClubId, clubId)
                .eq(ClubLeaderDO::getStatus, status));
    }

    default List<ClubLeaderDO> selectActiveListByUserId(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubLeaderDO>()
                .eq(ClubLeaderDO::getUserId, userId)
                .eq(ClubLeaderDO::getStatus, status)
                .orderByDesc(ClubLeaderDO::getId));
    }

    default PageResult<ClubLeaderDO> selectPageByClubId(PageParam pageParam, Long clubId,
                                                        Integer status, Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubLeaderDO>()
                .eqIfPresent(ClubLeaderDO::getClubId, clubId)
                .eqIfPresent(ClubLeaderDO::getStatus, status)
                .eqIfPresent(ClubLeaderDO::getUserId, userId)
                .orderByDesc(ClubLeaderDO::getId));
    }

    default List<ClubLeaderDO> selectListByClubIdAndStatus(Long clubId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubLeaderDO>()
                .eq(ClubLeaderDO::getClubId, clubId)
                .eq(ClubLeaderDO::getStatus, status)
                .orderByDesc(ClubLeaderDO::getId));
    }

    @Select("SELECT COUNT(1) FROM club_points_club_leader WHERE club_id = #{clubId} AND status = #{status}")
    Long selectCountByClubIdAndStatus(@Param("clubId") Long clubId,
                                      @Param("status") Integer status);

}

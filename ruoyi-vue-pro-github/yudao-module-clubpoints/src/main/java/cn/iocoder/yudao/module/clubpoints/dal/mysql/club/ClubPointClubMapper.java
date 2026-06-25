package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointClubStatusEnum;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ClubPointClubMapper extends BaseMapperX<ClubPointClubDO> {

    default ClubPointClubDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<ClubPointClubDO>()
                .eq(ClubPointClubDO::getCode, code));
    }

    default ClubPointClubDO selectByName(String name) {
        return selectOne(new LambdaQueryWrapperX<ClubPointClubDO>()
                .eq(ClubPointClubDO::getName, name));
    }

    default PageResult<ClubPointClubDO> selectPage(PageParam pageParam, String keyword, Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointClubDO>()
                .likeIfPresent(ClubPointClubDO::getName, keyword)
                .eqIfPresent(ClubPointClubDO::getStatus, status)
                .orderByAsc(ClubPointClubDO::getSort)
                .orderByDesc(ClubPointClubDO::getId));
    }

    default PageResult<ClubPointClubDO> selectJoinablePage(PageParam pageParam, String keyword,
                                                           Collection<Long> joinedClubIds) {
        LambdaQueryWrapperX<ClubPointClubDO> query = new LambdaQueryWrapperX<ClubPointClubDO>()
                .likeIfPresent(ClubPointClubDO::getName, keyword)
                .eq(ClubPointClubDO::getStatus, ClubPointClubStatusEnum.ENABLED.getStatus());
        if (joinedClubIds != null && !joinedClubIds.isEmpty()) {
            query.notIn(ClubPointClubDO::getId, joinedClubIds);
        }
        query.orderByAsc(ClubPointClubDO::getSort).orderByDesc(ClubPointClubDO::getId);
        return selectPage(pageParam, query);
    }

    default List<ClubPointClubDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ClubPointClubDO>()
                .in(ClubPointClubDO::getId, ids)
                .orderByAsc(ClubPointClubDO::getSort)
                .orderByDesc(ClubPointClubDO::getId));
    }

    @Select("SELECT COALESCE(SUM(cnt), 0) FROM ("
            + " SELECT COUNT(1) AS cnt FROM club_points_club_member WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_club_leader WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_activity WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_activity_registration WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_transaction WHERE issuing_club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_contribution_material WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_contribution_item WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_annual_ranking_record WHERE club_id = #{clubId}"
            + " UNION ALL SELECT COUNT(1) AS cnt FROM club_points_incentive_record WHERE club_id = #{clubId}"
            + ") t")
    Long selectReferenceCountByClubId(@Param("clubId") Long clubId);

    @Delete("DELETE FROM club_points_club WHERE id = #{id}")
    int deletePhysicallyById(@Param("id") Long id);

}

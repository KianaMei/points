package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

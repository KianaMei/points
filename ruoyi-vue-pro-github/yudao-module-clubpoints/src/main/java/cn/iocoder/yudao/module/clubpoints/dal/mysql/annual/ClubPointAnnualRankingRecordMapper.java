package cn.iocoder.yudao.module.clubpoints.dal.mysql.annual;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClubPointAnnualRankingRecordMapper extends BaseMapperX<ClubPointAnnualRankingRecordDO> {

    default List<ClubPointAnnualRankingRecordDO> selectListByYear(Integer year) {
        return selectList(new LambdaQueryWrapperX<ClubPointAnnualRankingRecordDO>()
                .eq(ClubPointAnnualRankingRecordDO::getYear, year)
                .orderByAsc(ClubPointAnnualRankingRecordDO::getRankNo)
                .orderByAsc(ClubPointAnnualRankingRecordDO::getId));
    }

    default ClubPointAnnualRankingRecordDO selectByYearAndClubCode(Integer year, String clubCode) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAnnualRankingRecordDO>()
                .eq(ClubPointAnnualRankingRecordDO::getYear, year)
                .eq(ClubPointAnnualRankingRecordDO::getClubCodeSnapshot, clubCode));
    }

    @Delete("DELETE FROM club_points_annual_ranking_record WHERE `year` = #{year}")
    int deletePhysicallyByYear(@Param("year") Integer year);

}

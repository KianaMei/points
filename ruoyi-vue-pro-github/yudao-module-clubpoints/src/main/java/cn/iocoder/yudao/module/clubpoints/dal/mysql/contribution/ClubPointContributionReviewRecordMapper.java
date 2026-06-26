package cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionReviewRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointContributionReviewRecordMapper extends BaseMapperX<ClubPointContributionReviewRecordDO> {

    default List<ClubPointContributionReviewRecordDO> selectListByMaterialId(Long materialId) {
        return selectList(new LambdaQueryWrapperX<ClubPointContributionReviewRecordDO>()
                .eq(ClubPointContributionReviewRecordDO::getMaterialId, materialId)
                .orderByAsc(ClubPointContributionReviewRecordDO::getReviewTime)
                .orderByAsc(ClubPointContributionReviewRecordDO::getId));
    }

}

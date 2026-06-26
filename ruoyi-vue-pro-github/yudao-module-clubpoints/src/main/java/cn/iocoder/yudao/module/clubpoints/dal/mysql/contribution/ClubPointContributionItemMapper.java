package cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointContributionItemMapper extends BaseMapperX<ClubPointContributionItemDO> {

    default List<ClubPointContributionItemDO> selectListByMaterialId(Long materialId) {
        return selectList(new LambdaQueryWrapperX<ClubPointContributionItemDO>()
                .eq(ClubPointContributionItemDO::getMaterialId, materialId)
                .orderByAsc(ClubPointContributionItemDO::getId));
    }

    default ClubPointContributionItemDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointContributionItemDO>()
                .eq(ClubPointContributionItemDO::getIdempotencyKey, idempotencyKey));
    }

    default ClubPointContributionItemDO selectByEffectiveUniqueKey(String effectiveUniqueKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointContributionItemDO>()
                .eq(ClubPointContributionItemDO::getEffectiveUniqueKey, effectiveUniqueKey));
    }

    default int deleteByMaterialId(Long materialId) {
        return delete(new LambdaQueryWrapperX<ClubPointContributionItemDO>()
                .eq(ClubPointContributionItemDO::getMaterialId, materialId));
    }

}

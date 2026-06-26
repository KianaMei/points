package cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointContributionMaterialMapper extends BaseMapperX<ClubPointContributionMaterialDO> {

    default ClubPointContributionMaterialDO selectByRequestNo(String requestNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointContributionMaterialDO>()
                .eq(ClubPointContributionMaterialDO::getRequestNo, requestNo));
    }

    default ClubPointContributionMaterialDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointContributionMaterialDO::getId, id);
    }

    default List<ClubPointContributionMaterialDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointContributionMaterialDO>()
                .eq(ClubPointContributionMaterialDO::getStatus, status)
                .orderByAsc(ClubPointContributionMaterialDO::getSubmitTime)
                .orderByAsc(ClubPointContributionMaterialDO::getId));
    }

    default PageResult<ClubPointContributionMaterialDO> selectPage(PageParam pageParam, Long clubId, Integer type,
                                                                  Integer status, Boolean directCreated) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointContributionMaterialDO>()
                .eqIfPresent(ClubPointContributionMaterialDO::getClubId, clubId)
                .eqIfPresent(ClubPointContributionMaterialDO::getType, type)
                .eqIfPresent(ClubPointContributionMaterialDO::getStatus, status)
                .eqIfPresent(ClubPointContributionMaterialDO::getDirectCreated, directCreated)
                .orderByDesc(ClubPointContributionMaterialDO::getSubmitTime)
                .orderByDesc(ClubPointContributionMaterialDO::getId));
    }

}

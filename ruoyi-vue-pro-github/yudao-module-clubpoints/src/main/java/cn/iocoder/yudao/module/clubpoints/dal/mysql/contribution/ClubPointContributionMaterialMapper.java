package cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointContributionMaterialMapper extends BaseMapperX<ClubPointContributionMaterialDO> {

    default ClubPointContributionMaterialDO selectByRequestNo(String requestNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointContributionMaterialDO>()
                .eq(ClubPointContributionMaterialDO::getRequestNo, requestNo));
    }

}

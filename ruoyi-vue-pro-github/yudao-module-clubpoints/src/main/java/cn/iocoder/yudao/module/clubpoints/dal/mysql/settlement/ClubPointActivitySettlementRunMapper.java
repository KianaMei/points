package cn.iocoder.yudao.module.clubpoints.dal.mysql.settlement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointActivitySettlementRunMapper extends BaseMapperX<ClubPointActivitySettlementRunDO> {

    default ClubPointActivitySettlementRunDO selectByRunKey(String runKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointActivitySettlementRunDO>()
                .eq(ClubPointActivitySettlementRunDO::getRunKey, runKey));
    }

}

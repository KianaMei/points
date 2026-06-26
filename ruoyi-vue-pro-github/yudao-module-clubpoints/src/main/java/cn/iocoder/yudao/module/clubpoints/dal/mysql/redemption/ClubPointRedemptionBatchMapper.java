package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointRedemptionBatchMapper extends BaseMapperX<ClubPointRedemptionBatchDO> {

    default ClubPointRedemptionBatchDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointRedemptionBatchDO::getId, id);
    }

}

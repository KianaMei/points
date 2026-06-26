package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointRedemptionApplicationMapper extends BaseMapperX<ClubPointRedemptionApplicationDO> {

    default ClubPointRedemptionApplicationDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointRedemptionApplicationDO::getId, id);
    }

    default List<ClubPointRedemptionApplicationDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getStatus, status)
                .orderByAsc(ClubPointRedemptionApplicationDO::getApplyTime)
                .orderByAsc(ClubPointRedemptionApplicationDO::getId));
    }

    default ClubPointRedemptionApplicationDO selectByApplicationNo(String applicationNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getApplicationNo, applicationNo));
    }

    default ClubPointRedemptionApplicationDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                .eq(ClubPointRedemptionApplicationDO::getIdempotencyKey, idempotencyKey));
    }

}

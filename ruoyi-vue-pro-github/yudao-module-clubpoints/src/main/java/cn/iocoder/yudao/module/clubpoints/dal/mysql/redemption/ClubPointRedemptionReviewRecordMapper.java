package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionReviewRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointRedemptionReviewRecordMapper extends BaseMapperX<ClubPointRedemptionReviewRecordDO> {

    default List<ClubPointRedemptionReviewRecordDO> selectListByApplicationId(Long applicationId) {
        return selectList(new LambdaQueryWrapperX<ClubPointRedemptionReviewRecordDO>()
                .eq(ClubPointRedemptionReviewRecordDO::getApplicationId, applicationId)
                .orderByAsc(ClubPointRedemptionReviewRecordDO::getReviewTime)
                .orderByAsc(ClubPointRedemptionReviewRecordDO::getId));
    }

}

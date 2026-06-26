package cn.iocoder.yudao.module.clubpoints.dal.mysql.annual;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointAnnualClearingRecordMapper extends BaseMapperX<ClubPointAnnualClearingRecordDO> {

    default ClubPointAnnualClearingRecordDO selectByUserIdAndYear(Long userId, Integer year) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAnnualClearingRecordDO>()
                .eq(ClubPointAnnualClearingRecordDO::getUserId, userId)
                .eq(ClubPointAnnualClearingRecordDO::getYear, year));
    }

    default ClubPointAnnualClearingRecordDO selectByUserIdAndYearForUpdate(Long userId, Integer year) {
        return selectOneForUpdate(new LambdaQueryWrapperX<ClubPointAnnualClearingRecordDO>()
                .eq(ClubPointAnnualClearingRecordDO::getUserId, userId)
                .eq(ClubPointAnnualClearingRecordDO::getYear, year));
    }

    default ClubPointAnnualClearingRecordDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAnnualClearingRecordDO>()
                .eq(ClubPointAnnualClearingRecordDO::getIdempotencyKey, idempotencyKey));
    }

    default List<ClubPointAnnualClearingRecordDO> selectListByYearAndStatus(Integer year, Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointAnnualClearingRecordDO>()
                .eq(ClubPointAnnualClearingRecordDO::getYear, year)
                .eq(ClubPointAnnualClearingRecordDO::getStatus, status));
    }

    default PageResult<ClubPointAnnualClearingRecordDO> selectPageByQuery(PageParam pageParam, Integer year,
                                                                          Long userId, Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointAnnualClearingRecordDO>()
                .eqIfPresent(ClubPointAnnualClearingRecordDO::getYear, year)
                .eqIfPresent(ClubPointAnnualClearingRecordDO::getUserId, userId)
                .eqIfPresent(ClubPointAnnualClearingRecordDO::getStatus, status)
                .orderByDesc(ClubPointAnnualClearingRecordDO::getClearTime)
                .orderByDesc(ClubPointAnnualClearingRecordDO::getId));
    }

}

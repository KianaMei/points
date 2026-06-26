package cn.iocoder.yudao.module.clubpoints.dal.mysql.annual;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointIncentiveRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointIncentiveRecordMapper extends BaseMapperX<ClubPointIncentiveRecordDO> {

    default ClubPointIncentiveRecordDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointIncentiveRecordDO::getId, id);
    }

    default ClubPointIncentiveRecordDO selectBySourceTypeAndSourceId(Integer sourceType, Long sourceId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointIncentiveRecordDO>()
                .eq(ClubPointIncentiveRecordDO::getSourceType, sourceType)
                .eq(ClubPointIncentiveRecordDO::getSourceId, sourceId));
    }

    default List<ClubPointIncentiveRecordDO> selectListByYearTypeStatus(Integer year, Integer type, Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointIncentiveRecordDO>()
                .eq(ClubPointIncentiveRecordDO::getYear, year)
                .eq(ClubPointIncentiveRecordDO::getType, type)
                .eq(ClubPointIncentiveRecordDO::getStatus, status)
                .orderByAsc(ClubPointIncentiveRecordDO::getId));
    }

}

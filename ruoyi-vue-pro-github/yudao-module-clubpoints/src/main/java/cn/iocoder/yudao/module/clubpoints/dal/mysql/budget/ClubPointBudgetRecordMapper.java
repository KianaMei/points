package cn.iocoder.yudao.module.clubpoints.dal.mysql.budget;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.budget.ClubPointBudgetRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ClubPointBudgetRecordMapper extends BaseMapperX<ClubPointBudgetRecordDO> {

    default ClubPointBudgetRecordDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointBudgetRecordDO::getId, id);
    }

    default ClubPointBudgetRecordDO selectBySourceTypeAndSourceId(Integer sourceType, Long sourceId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointBudgetRecordDO>()
                .eq(ClubPointBudgetRecordDO::getSourceType, sourceType)
                .eq(ClubPointBudgetRecordDO::getSourceId, sourceId));
    }

    default List<ClubPointBudgetRecordDO> selectListByQuery(Integer category, LocalDate startDate,
                                                            LocalDate endDateExclusive,
                                                            Integer sourceType, Long sourceId) {
        return selectList(new LambdaQueryWrapperX<ClubPointBudgetRecordDO>()
                .eqIfPresent(ClubPointBudgetRecordDO::getCategory, category)
                .geIfPresent(ClubPointBudgetRecordDO::getOccurDate, startDate)
                .ltIfPresent(ClubPointBudgetRecordDO::getOccurDate, endDateExclusive)
                .eqIfPresent(ClubPointBudgetRecordDO::getSourceType, sourceType)
                .eqIfPresent(ClubPointBudgetRecordDO::getSourceId, sourceId)
                .orderByAsc(ClubPointBudgetRecordDO::getOccurDate)
                .orderByAsc(ClubPointBudgetRecordDO::getId));
    }

    default PageResult<ClubPointBudgetRecordDO> selectPageForReport(PageParam pageParam, Integer category,
                                                                    LocalDate startDate, LocalDate endDateExclusive,
                                                                    Integer sourceType, Long sourceId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointBudgetRecordDO>()
                .eqIfPresent(ClubPointBudgetRecordDO::getCategory, category)
                .geIfPresent(ClubPointBudgetRecordDO::getOccurDate, startDate)
                .ltIfPresent(ClubPointBudgetRecordDO::getOccurDate, endDateExclusive)
                .eqIfPresent(ClubPointBudgetRecordDO::getSourceType, sourceType)
                .eqIfPresent(ClubPointBudgetRecordDO::getSourceId, sourceId)
                .orderByAsc(ClubPointBudgetRecordDO::getOccurDate)
                .orderByAsc(ClubPointBudgetRecordDO::getId));
    }

}

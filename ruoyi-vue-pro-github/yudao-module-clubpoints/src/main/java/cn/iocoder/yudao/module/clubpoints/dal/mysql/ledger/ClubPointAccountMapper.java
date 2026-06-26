package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointAccountMapper extends BaseMapperX<ClubPointAccountDO> {

    default List<ClubPointAccountDO> selectListForRebuild() {
        return selectList(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .orderByAsc(ClubPointAccountDO::getUserId));
    }

    default List<ClubPointAccountDO> selectListForEligibilitySnapshot() {
        return selectList(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .orderByDesc(ClubPointAccountDO::getAvailablePoints)
                .orderByAsc(ClubPointAccountDO::getUserId));
    }

    default List<ClubPointAccountDO> selectListForAnnualClearing() {
        return selectList(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .orderByAsc(ClubPointAccountDO::getUserId));
    }

    default ClubPointAccountDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .eq(ClubPointAccountDO::getUserId, userId));
    }

    default ClubPointAccountDO selectByUserIdForUpdate(Long userId) {
        return selectOneForUpdate(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .eq(ClubPointAccountDO::getUserId, userId));
    }

    default PageResult<ClubPointAccountDO> selectPage(PageParam pageParam, Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointAccountDO>()
                .eqIfPresent(ClubPointAccountDO::getUserId, userId)
                .orderByDesc(ClubPointAccountDO::getId));
    }

}

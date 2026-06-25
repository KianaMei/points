package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointUserYearStatusDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointUserYearStatusMapper extends BaseMapperX<ClubPointUserYearStatusDO> {

    default ClubPointUserYearStatusDO selectByUserIdAndYear(Long userId, Integer year) {
        return selectOne(new LambdaQueryWrapperX<ClubPointUserYearStatusDO>()
                .eq(ClubPointUserYearStatusDO::getUserId, userId)
                .eq(ClubPointUserYearStatusDO::getYear, year));
    }

}

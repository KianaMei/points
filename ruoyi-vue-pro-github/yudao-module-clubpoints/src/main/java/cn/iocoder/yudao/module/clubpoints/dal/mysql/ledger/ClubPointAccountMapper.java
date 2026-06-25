package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointAccountMapper extends BaseMapperX<ClubPointAccountDO> {

    default ClubPointAccountDO selectByUserId(Long userId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .eq(ClubPointAccountDO::getUserId, userId));
    }

    default ClubPointAccountDO selectByUserIdForUpdate(Long userId) {
        return selectOneForUpdate(new LambdaQueryWrapperX<ClubPointAccountDO>()
                .eq(ClubPointAccountDO::getUserId, userId));
    }

}

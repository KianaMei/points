package cn.iocoder.yudao.module.clubpoints.dal.mysql.job;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubJobRunMapper extends BaseMapperX<ClubJobRunDO> {

    default ClubJobRunDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubJobRunDO>()
                .eq(ClubJobRunDO::getIdempotencyKey, idempotencyKey));
    }

}

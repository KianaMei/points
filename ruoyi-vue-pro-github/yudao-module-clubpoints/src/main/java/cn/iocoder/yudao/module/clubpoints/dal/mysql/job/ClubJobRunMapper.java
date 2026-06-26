package cn.iocoder.yudao.module.clubpoints.dal.mysql.job;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunPageReqBO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubJobRunMapper extends BaseMapperX<ClubJobRunDO> {

    default ClubJobRunDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubJobRunDO>()
                .eq(ClubJobRunDO::getIdempotencyKey, idempotencyKey));
    }

    default PageResult<ClubJobRunDO> selectPage(ClubJobRunPageReqBO reqBO) {
        return selectPage(reqBO, new LambdaQueryWrapperX<ClubJobRunDO>()
                .eqIfPresent(ClubJobRunDO::getTaskType, reqBO.getTaskType())
                .eqIfPresent(ClubJobRunDO::getBizType, reqBO.getBizType())
                .eqIfPresent(ClubJobRunDO::getBizId, reqBO.getBizId())
                .eqIfPresent(ClubJobRunDO::getStatus, reqBO.getStatus())
                .eqIfPresent(ClubJobRunDO::getTriggerSource, reqBO.getTriggerSource())
                .likeIfPresent(ClubJobRunDO::getRunKey, reqBO.getRunKey())
                .betweenIfPresent(ClubJobRunDO::getStartTime, reqBO.getStartTime(), reqBO.getEndTime())
                .orderByDesc(ClubJobRunDO::getId));
    }

}

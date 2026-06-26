package cn.iocoder.yudao.module.clubpoints.dal.mysql.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditPageReqBO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubAuditLogMapper extends BaseMapperX<ClubAuditLogDO> {

    default PageResult<ClubAuditLogDO> selectPage(ClubAuditPageReqBO reqBO) {
        return selectPage(reqBO, new LambdaQueryWrapperX<ClubAuditLogDO>()
                .eqIfPresent(ClubAuditLogDO::getActionType, reqBO.getActionType())
                .eqIfPresent(ClubAuditLogDO::getBizType, reqBO.getBizType())
                .eqIfPresent(ClubAuditLogDO::getBizId, reqBO.getBizId())
                .eqIfPresent(ClubAuditLogDO::getOperatorUserId, reqBO.getOperatorUserId())
                .eqIfPresent(ClubAuditLogDO::getSuccess, reqBO.getSuccess())
                .likeIfPresent(ClubAuditLogDO::getOperatorNameSnapshot, reqBO.getOperatorNameSnapshot())
                .likeIfPresent(ClubAuditLogDO::getReason, reqBO.getReason())
                .betweenIfPresent(ClubAuditLogDO::getOperationTime, reqBO.getOperationTimeStart(),
                        reqBO.getOperationTimeEnd())
                .orderByDesc(ClubAuditLogDO::getOperationTime)
                .orderByDesc(ClubAuditLogDO::getId));
    }

}

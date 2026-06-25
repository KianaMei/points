package cn.iocoder.yudao.module.clubpoints.dal.mysql.audit;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubAuditLogMapper extends BaseMapperX<ClubAuditLogDO> {
}

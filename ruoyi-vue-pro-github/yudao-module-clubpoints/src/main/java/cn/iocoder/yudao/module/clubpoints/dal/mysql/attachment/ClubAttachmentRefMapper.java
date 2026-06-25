package cn.iocoder.yudao.module.clubpoints.dal.mysql.attachment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.attachment.ClubAttachmentRefDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubAttachmentRefMapper extends BaseMapperX<ClubAttachmentRefDO> {

    default List<ClubAttachmentRefDO> selectListByBiz(String bizType, Long bizId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubAttachmentRefDO>()
                .eq(ClubAttachmentRefDO::getBizType, bizType)
                .eq(ClubAttachmentRefDO::getBizId, bizId)
                .eq(ClubAttachmentRefDO::getStatus, status));
    }

}

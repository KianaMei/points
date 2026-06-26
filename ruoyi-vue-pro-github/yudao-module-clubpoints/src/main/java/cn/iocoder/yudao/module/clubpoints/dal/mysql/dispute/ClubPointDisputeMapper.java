package cn.iocoder.yudao.module.clubpoints.dal.mysql.dispute;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointDisputeMapper extends BaseMapperX<ClubPointDisputeDO> {

    default ClubPointDisputeDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointDisputeDO::getId, id);
    }

    default PageResult<ClubPointDisputeDO> selectMyPage(PageParam pageParam, Long userId, Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointDisputeDO>()
                .eq(ClubPointDisputeDO::getUserId, userId)
                .eqIfPresent(ClubPointDisputeDO::getStatus, status)
                .orderByDesc(ClubPointDisputeDO::getSubmitTime)
                .orderByDesc(ClubPointDisputeDO::getId));
    }

    default PageResult<ClubPointDisputeDO> selectAdminPage(PageParam pageParam, Long userId, Integer status,
                                                          Integer targetType, Long targetId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointDisputeDO>()
                .eqIfPresent(ClubPointDisputeDO::getUserId, userId)
                .eqIfPresent(ClubPointDisputeDO::getStatus, status)
                .eqIfPresent(ClubPointDisputeDO::getTargetType, targetType)
                .eqIfPresent(ClubPointDisputeDO::getTargetId, targetId)
                .orderByDesc(ClubPointDisputeDO::getSubmitTime)
                .orderByDesc(ClubPointDisputeDO::getId));
    }

    default List<ClubPointDisputeDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointDisputeDO>()
                .eq(ClubPointDisputeDO::getStatus, status)
                .orderByAsc(ClubPointDisputeDO::getSubmitTime)
                .orderByAsc(ClubPointDisputeDO::getId));
    }

}

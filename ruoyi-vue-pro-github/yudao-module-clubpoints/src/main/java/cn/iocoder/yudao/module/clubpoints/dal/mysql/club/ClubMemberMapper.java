package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubMemberMapper extends BaseMapperX<ClubMemberDO> {

    default ClubMemberDO selectByUserIdAndClubIdAndStatus(Long userId, Long clubId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<ClubMemberDO>()
                .eq(ClubMemberDO::getUserId, userId)
                .eq(ClubMemberDO::getClubId, clubId)
                .eq(ClubMemberDO::getStatus, status));
    }

    default PageResult<ClubMemberDO> selectPageByClubIdAndStatus(PageParam pageParam, Long clubId,
                                                                 Integer status, Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubMemberDO>()
                .eq(ClubMemberDO::getClubId, clubId)
                .eq(ClubMemberDO::getStatus, status)
                .eqIfPresent(ClubMemberDO::getUserId, userId)
                .orderByDesc(ClubMemberDO::getId));
    }

}

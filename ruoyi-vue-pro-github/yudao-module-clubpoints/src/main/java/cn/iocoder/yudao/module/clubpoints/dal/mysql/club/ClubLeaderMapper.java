package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubLeaderMapper extends BaseMapperX<ClubLeaderDO> {

    default ClubLeaderDO selectByUserIdAndClubIdAndStatus(Long userId, Long clubId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<ClubLeaderDO>()
                .eq(ClubLeaderDO::getUserId, userId)
                .eq(ClubLeaderDO::getClubId, clubId)
                .eq(ClubLeaderDO::getStatus, status));
    }

}

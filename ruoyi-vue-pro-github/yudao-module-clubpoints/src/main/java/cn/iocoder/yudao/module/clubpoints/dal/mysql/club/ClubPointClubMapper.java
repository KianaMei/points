package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointClubMapper extends BaseMapperX<ClubPointClubDO> {

    default ClubPointClubDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<ClubPointClubDO>()
                .eq(ClubPointClubDO::getCode, code));
    }

}

package cn.iocoder.yudao.module.clubpoints.dal.mysql.rule;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointRuleVersionMapper extends BaseMapperX<ClubPointRuleVersionDO> {

    default ClubPointRuleVersionDO selectByVersionNo(String versionNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRuleVersionDO>()
                .eq(ClubPointRuleVersionDO::getVersionNo, versionNo));
    }

}

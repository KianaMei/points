package cn.iocoder.yudao.module.clubpoints.dal.mysql.rule;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ClubPointRuleVersionMapper extends BaseMapperX<ClubPointRuleVersionDO> {

    default ClubPointRuleVersionDO selectByVersionNo(String versionNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRuleVersionDO>()
                .eq(ClubPointRuleVersionDO::getVersionNo, versionNo));
    }

    default PageResult<ClubPointRuleVersionDO> selectPage(PageParam pageParam, String versionNo,
                                                          String name, Integer status) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointRuleVersionDO>()
                .likeIfPresent(ClubPointRuleVersionDO::getVersionNo, versionNo)
                .likeIfPresent(ClubPointRuleVersionDO::getName, name)
                .eqIfPresent(ClubPointRuleVersionDO::getStatus, status)
                .orderByDesc(ClubPointRuleVersionDO::getId));
    }

    default List<ClubPointRuleVersionDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ClubPointRuleVersionDO>()
                .eq(ClubPointRuleVersionDO::getStatus, status));
    }

    default ClubPointRuleVersionDO selectCurrentPublished(Integer status, LocalDateTime now) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRuleVersionDO>()
                .eq(ClubPointRuleVersionDO::getStatus, status)
                .le(ClubPointRuleVersionDO::getEffectiveTime, now)
                .orderByDesc(ClubPointRuleVersionDO::getEffectiveTime)
                .last("LIMIT 1"));
    }

}

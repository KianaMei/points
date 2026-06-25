package cn.iocoder.yudao.module.clubpoints.dal.mysql.rule;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointRuleItemMapper extends BaseMapperX<ClubPointRuleItemDO> {

    default ClubPointRuleItemDO selectByRuleVersionIdAndItemCode(Long ruleVersionId, String itemCode) {
        return selectOne(new LambdaQueryWrapperX<ClubPointRuleItemDO>()
                .eq(ClubPointRuleItemDO::getRuleVersionId, ruleVersionId)
                .eq(ClubPointRuleItemDO::getItemCode, itemCode));
    }

    default List<ClubPointRuleItemDO> selectListByRuleVersionId(Long ruleVersionId) {
        return selectList(new LambdaQueryWrapperX<ClubPointRuleItemDO>()
                .eq(ClubPointRuleItemDO::getRuleVersionId, ruleVersionId));
    }

}

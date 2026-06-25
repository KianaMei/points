package cn.iocoder.yudao.module.clubpoints.dal.mysql.rule;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRulePublishRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClubPointRulePublishRecordMapper extends BaseMapperX<ClubPointRulePublishRecordDO> {

    default List<ClubPointRulePublishRecordDO> selectListByRuleVersionId(Long ruleVersionId) {
        return selectList(new LambdaQueryWrapperX<ClubPointRulePublishRecordDO>()
                .eq(ClubPointRulePublishRecordDO::getRuleVersionId, ruleVersionId));
    }

}

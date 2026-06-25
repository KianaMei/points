package cn.iocoder.yudao.module.clubpoints.service.rule;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleVersionSaveReqBO;

/**
 * 积分规则版本服务
 */
public interface ClubPointRuleService {

    Long createDraftVersion(ClubPointRuleVersionSaveReqBO reqBO);

    Long copyVersion(Long sourceVersionId, ClubPointRuleVersionSaveReqBO reqBO);

    Long createRuleItem(ClubPointRuleItemSaveReqBO reqBO);

    void updateDraftRuleItem(ClubPointRuleItemSaveReqBO reqBO);

    void publishVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO);

    void disableVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO);

    ClubPointRuleVersionDO getCurrentRuleVersion();

    ClubPointRuleItemDO getRuleItemByCode(String code);

}

package cn.iocoder.yudao.module.clubpoints.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleItemSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleVersionSaveReqBO;

import java.util.List;

/**
 * 积分规则版本服务
 */
public interface ClubPointRuleService {

    Long createDraftVersion(ClubPointRuleVersionSaveReqBO reqBO);

    void updateDraftVersion(ClubPointRuleVersionSaveReqBO reqBO);

    Long copyVersion(Long sourceVersionId, ClubPointRuleVersionSaveReqBO reqBO);

    Long createRuleItem(ClubPointRuleItemSaveReqBO reqBO);

    void updateDraftRuleItem(ClubPointRuleItemSaveReqBO reqBO);

    void publishVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO);

    void withdrawVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO);

    void disableVersion(Long ruleVersionId, ClubPointRuleOperationReqBO reqBO);

    PageResult<ClubPointRuleVersionDO> getRuleVersionPage(PageParam pageParam, String versionNo,
                                                          String name, Integer status);

    ClubPointRuleVersionDO getRuleVersion(Long ruleVersionId);

    ClubPointRuleVersionDO getCurrentRuleVersion();

    List<ClubPointRuleItemDO> getRuleItemList(Long ruleVersionId);

    ClubPointRuleItemDO getRuleItemByCode(String code);

}

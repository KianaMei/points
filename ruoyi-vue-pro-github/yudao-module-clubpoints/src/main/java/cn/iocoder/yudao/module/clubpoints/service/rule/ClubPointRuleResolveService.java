package cn.iocoder.yudao.module.clubpoints.service.rule;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.service.rule.bo.ClubPointRuleSnapshotBO;

import java.time.LocalDateTime;

/**
 * 积分规则业务读取服务
 */
public interface ClubPointRuleResolveService {

    ClubPointRuleVersionDO getEffectiveVersion(LocalDateTime occurredAt);

    ClubPointRuleItemDO getItem(Long ruleVersionId, String itemCode);

    Integer getFixedPoints(Long ruleVersionId, String itemCode);

    void validatePointsInRange(Long ruleVersionId, String itemCode, Integer points);

    void validatePointInRange(String itemCode, Integer points);

    ClubPointRuleSnapshotBO snapshotRuleItem(Long ruleVersionId, String itemCode);

    ClubPointRuleSnapshotBO snapshotRuleItem(Long ruleVersionId, String itemCode, Integer points);

    ClubPointRuleSnapshotBO buildRuleSnapshot(String itemCode, Integer points);

}

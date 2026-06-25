package cn.iocoder.yudao.module.clubpoints.service.rule.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 积分规则快照
 */
@Data
@Accessors(chain = true)
public class ClubPointRuleSnapshotBO {

    private Long ruleVersionId;
    private String ruleVersionNo;
    private Long ruleItemId;
    private String ruleItemCode;
    private String ruleItemName;
    private Integer minPoints;
    private Integer maxPoints;
    private Integer defaultPoints;
    private Integer pointsSnapshot;
    private String ruleSnapshotJson;

}

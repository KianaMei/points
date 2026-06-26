package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 非签到积分材料明细保存参数
 */
@Data
@Accessors(chain = true)
public class ClubPointContributionItemSaveReqBO {

    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private Integer points;
    private String reason;
    private String materialSummary;
    private Integer dutyMonth;
    private Long recommendedUserId;
    private Integer awardLevel;
    private String approvalResultSnapshot;

}

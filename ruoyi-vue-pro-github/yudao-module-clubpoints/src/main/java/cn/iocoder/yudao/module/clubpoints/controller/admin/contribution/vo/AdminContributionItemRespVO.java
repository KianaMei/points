package cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 非签到积分材料明细 Response VO")
@Data
@Accessors(chain = true)
public class AdminContributionItemRespVO {

    private Long id;
    private Long materialId;
    private Long clubId;
    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private Integer pointCategory;
    private Long ruleItemId;
    private String ruleItemCode;
    private Integer direction;
    private Integer points;
    private String reason;
    private String materialSummary;
    private Integer dutyMonth;
    private Long recommendedUserId;
    private Integer awardLevel;
    private String approvalResultSnapshot;
    private Long transactionId;

}

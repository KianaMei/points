package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 兑换批次保存请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionBatchSaveReqBO {

    private Long id;
    private Integer year;
    private String name;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String description;
    private Integer minAvailablePoints;
    private Integer qualifiedCount;
    private Boolean includeTieAtCutoff;
    private String qualificationRuleJson;
    private Long ruleVersionId;
    private String ruleSnapshotJson;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}

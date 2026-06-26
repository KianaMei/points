package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminRedemptionBatchRespVO {

    private Long id;
    private Integer year;
    private String name;
    private Integer status;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String description;
    private Integer minAvailablePoints;
    private Integer qualifiedCount;
    private Boolean includeTieAtCutoff;
    private String qualificationRule;
    private Boolean snapshotGenerated;
    private LocalDateTime snapshotGeneratedTime;
    private Long ruleVersionId;
    private String ruleSnapshotJson;

}

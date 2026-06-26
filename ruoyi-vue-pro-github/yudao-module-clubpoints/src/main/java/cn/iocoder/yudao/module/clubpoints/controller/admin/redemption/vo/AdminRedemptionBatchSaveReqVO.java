package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminRedemptionBatchSaveReqVO {

    private Long id;
    @NotNull
    private Integer year;
    @NotBlank
    private String name;
    @NotNull
    private LocalDateTime openTime;
    @NotNull
    private LocalDateTime closeTime;
    private String description;
    @NotBlank
    private String qualificationRule;
    @NotNull
    private Integer minAvailablePoints;
    @NotNull
    private Integer qualifiedCount;
    @NotNull
    private Boolean includeTieAtCutoff;
    @NotNull
    private Long ruleVersionId;
    private String ruleSnapshotJson;
    private String reason;

}

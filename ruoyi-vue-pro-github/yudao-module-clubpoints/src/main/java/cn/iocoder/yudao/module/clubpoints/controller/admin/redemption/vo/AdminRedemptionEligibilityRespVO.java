package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminRedemptionEligibilityRespVO {

    private Long id;
    private Long batchId;
    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private Integer netPointsSnapshot;
    private Integer frozenPointsSnapshot;
    private Integer availablePointsSnapshot;
    private Integer annualEarnedPointsSnapshot;
    private Integer rankNo;
    private Boolean qualified;
    private String qualificationReason;
    private Boolean tieAtCutoff;
    private LocalDateTime generatedTime;

}

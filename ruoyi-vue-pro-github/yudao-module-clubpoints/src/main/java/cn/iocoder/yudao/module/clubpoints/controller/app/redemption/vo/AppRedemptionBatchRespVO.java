package cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AppRedemptionBatchRespVO {

    private Long id;
    private Integer year;
    private String name;
    private Integer status;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String description;
    private String qualificationRule;
    private Boolean snapshotGenerated;

}

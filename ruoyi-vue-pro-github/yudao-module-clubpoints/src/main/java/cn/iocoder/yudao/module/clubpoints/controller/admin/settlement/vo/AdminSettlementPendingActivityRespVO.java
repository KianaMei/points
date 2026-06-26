package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminSettlementPendingActivityRespVO {

    private Long id;
    private Long clubId;
    private String clubCodeSnapshot;
    private String clubNameSnapshot;
    private String title;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}

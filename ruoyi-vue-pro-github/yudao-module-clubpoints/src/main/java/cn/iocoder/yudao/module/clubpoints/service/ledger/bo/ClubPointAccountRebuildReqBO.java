package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ClubPointAccountRebuildReqBO {

    private Long userId;
    private Integer businessYear;
    private String runKey;
    private LocalDateTime plannedTime;
    private Integer triggerSource;
    private Long handlerUserId;
    private String manualHandleReason;

}

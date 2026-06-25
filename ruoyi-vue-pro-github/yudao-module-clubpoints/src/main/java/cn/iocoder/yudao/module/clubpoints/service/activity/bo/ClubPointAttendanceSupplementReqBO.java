package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 签到签退补录参数
 */
@Data
@Accessors(chain = true)
public class ClubPointAttendanceSupplementReqBO {

    private Long registrationId;
    private Integer targetType;
    private LocalDateTime recordTime;
    private String reason;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private Boolean operatorGlobalScope;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;

}

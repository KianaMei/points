package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 签到签退修正参数
 */
@Data
@Accessors(chain = true)
public class ClubPointAttendanceCorrectReqBO {

    private Long attendanceRecordId;
    private LocalDateTime newRecordTime;
    private String reason;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private Boolean operatorGlobalScope;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;

}

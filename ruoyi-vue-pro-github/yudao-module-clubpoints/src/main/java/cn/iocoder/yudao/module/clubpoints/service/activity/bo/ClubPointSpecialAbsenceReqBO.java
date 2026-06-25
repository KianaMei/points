package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 特殊缺席标记参数
 */
@Data
@Accessors(chain = true)
public class ClubPointSpecialAbsenceReqBO {

    private Long registrationId;
    private String reason;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private Boolean operatorGlobalScope;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;

}

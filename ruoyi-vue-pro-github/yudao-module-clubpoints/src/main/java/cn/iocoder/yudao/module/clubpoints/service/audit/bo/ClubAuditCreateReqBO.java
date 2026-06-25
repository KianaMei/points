package cn.iocoder.yudao.module.clubpoints.service.audit.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 强审计创建参数
 */
@Data
@Accessors(chain = true)
public class ClubAuditCreateReqBO {

    private String actionType;
    private String bizType;
    private Long bizId;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private LocalDateTime operationTime;
    private String clientIp;
    private String userAgent;
    private String reason;
    private String beforeJson;
    private String afterJson;
    private String targetSnapshotJson;
    private Boolean success;
    private String errorMessage;

}

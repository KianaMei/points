package cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 强审计日志 Response VO")
@Data
@Accessors(chain = true)
public class AdminAuditRespVO {

    private Long id;
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
    private LocalDateTime createTime;

}

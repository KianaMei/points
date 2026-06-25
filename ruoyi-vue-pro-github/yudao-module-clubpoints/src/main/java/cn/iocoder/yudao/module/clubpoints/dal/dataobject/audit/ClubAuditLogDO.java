package cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 俱乐部积分强审计日志 DO
 */
@TableName("club_points_audit_log")
@KeySequence("club_points_audit_log_seq")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubAuditLogDO extends BaseDO {

    @TableId
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

}

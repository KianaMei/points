package cn.iocoder.yudao.module.clubpoints.service.audit.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClubAuditPageReqBO extends PageParam {

    private String actionType;
    private String bizType;
    private Long bizId;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private Boolean success;
    private String reason;
    private LocalDateTime operationTimeStart;
    private LocalDateTime operationTimeEnd;

}

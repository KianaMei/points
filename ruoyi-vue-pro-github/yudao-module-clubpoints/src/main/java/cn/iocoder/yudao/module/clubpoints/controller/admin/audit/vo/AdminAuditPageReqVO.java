package cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 强审计日志分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminAuditPageReqVO extends PageParam {

    @Schema(description = "动作类型")
    private String actionType;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务编号")
    private Long bizId;

    @Schema(description = "操作人用户编号")
    private Long operatorUserId;

    @Schema(description = "操作人快照")
    private String operatorNameSnapshot;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "原因")
    private String reason;

    @Schema(description = "操作开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime operationTimeStart;

    @Schema(description = "操作结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime operationTimeEnd;

}

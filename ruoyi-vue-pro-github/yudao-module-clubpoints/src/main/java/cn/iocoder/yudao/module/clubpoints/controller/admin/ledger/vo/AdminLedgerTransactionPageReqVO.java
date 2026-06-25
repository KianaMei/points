package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分流水分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminLedgerTransactionPageReqVO extends PageParam {

    private Long userId;
    private Long clubId;
    private Integer direction;
    private Integer pointCategory;
    private Integer sourceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}

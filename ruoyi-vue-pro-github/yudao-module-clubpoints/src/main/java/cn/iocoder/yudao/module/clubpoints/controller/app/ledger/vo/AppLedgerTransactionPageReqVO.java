package cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "员工端 - 我的积分流水分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppLedgerTransactionPageReqVO extends PageParam {

    private Integer direction;
    private Integer pointCategory;
    private Integer sourceType;
    private Long clubId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}

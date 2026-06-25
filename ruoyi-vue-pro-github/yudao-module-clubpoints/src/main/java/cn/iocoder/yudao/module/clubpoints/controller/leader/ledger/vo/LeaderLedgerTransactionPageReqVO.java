package cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "负责人端 - 负责俱乐部积分流水分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class LeaderLedgerTransactionPageReqVO extends PageParam {

    @NotNull(message = "俱乐部 ID 不能为空")
    private Long clubId;

    private Long userId;
    private Integer direction;
    private Integer pointCategory;
    private Integer sourceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}

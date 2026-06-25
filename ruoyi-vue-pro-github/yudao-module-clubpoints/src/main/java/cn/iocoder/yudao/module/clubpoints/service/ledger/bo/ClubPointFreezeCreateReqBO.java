package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分冻结创建请求
 */
@Data
@Accessors(chain = true)
public class ClubPointFreezeCreateReqBO {

    private String freezeNo;
    private Long userId;
    private Integer points;
    private Integer sourceType;
    private Long sourceId;
    private LocalDateTime frozenAt;
    private String idempotencyKey;

}

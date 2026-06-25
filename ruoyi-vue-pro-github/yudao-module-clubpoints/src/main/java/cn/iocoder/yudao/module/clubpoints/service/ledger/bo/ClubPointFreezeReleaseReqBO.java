package cn.iocoder.yudao.module.clubpoints.service.ledger.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分冻结释放请求
 */
@Data
@Accessors(chain = true)
public class ClubPointFreezeReleaseReqBO {

    private Long freezeId;
    private LocalDateTime releasedAt;
    private String releaseReason;

}

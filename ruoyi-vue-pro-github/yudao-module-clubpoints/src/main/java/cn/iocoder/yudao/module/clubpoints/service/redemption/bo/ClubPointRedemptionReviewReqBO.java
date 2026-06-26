package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 兑换审核请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionReviewReqBO {

    private Long id;
    private Integer result;
    private String reason;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    /**
     * 内部审核时间；Controller 不接收该字段，测试和任务可显式固定跨年时间。
     */
    private LocalDateTime reviewTime;

}

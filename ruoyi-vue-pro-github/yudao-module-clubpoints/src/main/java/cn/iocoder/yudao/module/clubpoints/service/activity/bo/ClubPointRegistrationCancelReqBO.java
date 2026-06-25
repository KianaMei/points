package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动报名取消参数
 */
@Data
@Accessors(chain = true)
public class ClubPointRegistrationCancelReqBO {

    private Long registrationId;
    private Long userId;
    private String reason;
    private LocalDateTime operationTime;

}

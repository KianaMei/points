package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 活动报名创建参数
 */
@Data
@Accessors(chain = true)
public class ClubPointRegistrationCreateReqBO {

    private Long activityId;
    private Long userId;
    private LocalDateTime operationTime;

}

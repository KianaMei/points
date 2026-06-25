package cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/**
 * 管理员补录签到签退请求
 */
@Data
@Accessors(chain = true)
public class AdminAttendanceSupplementReqVO {

    @NotNull
    private Long registrationId;
    @NotNull
    private Integer targetType;
    @NotNull
    private LocalDateTime recordTime;
    private String reason;

}

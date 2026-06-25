package cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

/**
 * 员工签到签退请求
 */
@Data
@Accessors(chain = true)
public class AppAttendanceCheckReqVO {

    @NotNull
    private Long registrationId;
    private LocalDateTime clientTime;
    private String locationText;
    private String remark;

}

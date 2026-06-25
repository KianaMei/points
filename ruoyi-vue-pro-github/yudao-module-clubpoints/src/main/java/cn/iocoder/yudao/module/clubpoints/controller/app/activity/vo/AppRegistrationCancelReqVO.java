package cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 员工取消报名请求
 */
@Data
@Accessors(chain = true)
public class AppRegistrationCancelReqVO {

    @NotNull
    private Long registrationId;
    private String reason;

}

package cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 员工报名请求
 */
@Data
@Accessors(chain = true)
public class AppRegistrationCreateReqVO {

    @NotNull
    private Long activityId;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 管理员活动原因请求
 */
@Data
@Accessors(chain = true)
public class AdminActivityReasonReqVO {

    @NotNull
    private Long id;
    private String reason;

}

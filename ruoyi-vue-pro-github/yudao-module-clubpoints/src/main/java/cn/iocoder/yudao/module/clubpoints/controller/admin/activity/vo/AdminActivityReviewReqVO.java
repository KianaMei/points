package cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 管理员活动审核请求
 */
@Data
@Accessors(chain = true)
public class AdminActivityReviewReqVO {

    @NotNull
    private Long id;
    @NotNull
    private Boolean approved;
    private String reason;

}

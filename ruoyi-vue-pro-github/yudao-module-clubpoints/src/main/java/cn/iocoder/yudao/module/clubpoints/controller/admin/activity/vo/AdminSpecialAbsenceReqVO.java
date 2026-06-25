package cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 管理员特殊缺席请求
 */
@Data
@Accessors(chain = true)
public class AdminSpecialAbsenceReqVO {

    @NotNull
    private Long registrationId;
    private String reason;

}

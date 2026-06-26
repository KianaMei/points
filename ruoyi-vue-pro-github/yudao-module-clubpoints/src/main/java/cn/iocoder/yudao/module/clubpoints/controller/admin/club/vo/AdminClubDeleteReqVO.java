package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubStrongConfirmReqBO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 管理后台俱乐部物理删除请求
 */
@Data
@Accessors(chain = true)
public class AdminClubDeleteReqVO {

    @NotNull
    private Long id;
    private String reason;
    @Valid
    @NotNull
    private ClubStrongConfirmReqBO strongConfirm;

}

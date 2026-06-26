package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 管理后台俱乐部操作请求
 */
@Data
@Accessors(chain = true)
public class AdminClubOperationReqVO {

    @NotNull
    private Long id;
    private String reason;

}

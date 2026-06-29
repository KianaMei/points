package cn.iocoder.yudao.module.clubpoints.controller.app.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 员工端俱乐部操作请求
 */
@Data
@Accessors(chain = true)
public class AppClubOperationReqVO {

    @NotNull
    private Long id;

    private String reason;

}

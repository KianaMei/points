package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 管理后台俱乐部成员/负责人保存请求
 */
@Data
@Accessors(chain = true)
public class AdminClubMemberSaveReqVO {

    @NotNull
    private Long clubId;
    @NotNull
    private Long userId;
    private String reason;

}

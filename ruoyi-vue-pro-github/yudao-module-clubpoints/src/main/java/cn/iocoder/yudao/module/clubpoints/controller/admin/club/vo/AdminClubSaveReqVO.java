package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 管理后台俱乐部保存请求
 */
@Data
@Accessors(chain = true)
public class AdminClubSaveReqVO {

    private Long id;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String description;
    private String contactText;
    private Long coverFileId;
    private Integer sort;
    private String remark;
    private String reason;

}

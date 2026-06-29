package cn.iocoder.yudao.module.clubpoints.controller.leader.club.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 负责人端俱乐部资料保存请求
 */
@Data
@Accessors(chain = true)
public class LeaderClubSaveReqVO {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    private String description;
    private String contactText;
    private Long coverFileId;
    private String reason;

}

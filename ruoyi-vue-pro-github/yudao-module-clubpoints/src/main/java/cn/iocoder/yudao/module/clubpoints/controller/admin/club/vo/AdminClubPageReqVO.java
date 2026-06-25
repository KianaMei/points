package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 俱乐部分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminClubPageReqVO extends PageParam {

    @Schema(description = "俱乐部名称关键词", example = "篮球")
    private String keyword;

    @Schema(description = "俱乐部状态", example = "1")
    private Integer status;

}

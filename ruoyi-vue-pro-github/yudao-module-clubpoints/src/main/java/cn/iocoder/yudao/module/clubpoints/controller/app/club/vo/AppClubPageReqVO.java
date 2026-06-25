package cn.iocoder.yudao.module.clubpoints.controller.app.club.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "员工端 - 可加入俱乐部分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppClubPageReqVO extends PageParam {

    @Schema(description = "俱乐部名称关键词", example = "篮球")
    private String keyword;

}

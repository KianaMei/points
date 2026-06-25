package cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 俱乐部负责人分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminClubLeaderPageReqVO extends PageParam {

    @Schema(description = "俱乐部 ID", example = "1")
    private Long clubId;

    @Schema(description = "负责人用户 ID", example = "900")
    private Long userId;

    @Schema(description = "负责人状态", example = "1")
    private Integer status;

}

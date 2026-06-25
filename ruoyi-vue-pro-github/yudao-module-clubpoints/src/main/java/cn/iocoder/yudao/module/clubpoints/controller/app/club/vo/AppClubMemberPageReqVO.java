package cn.iocoder.yudao.module.clubpoints.controller.app.club.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "员工端 - 俱乐部成员分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppClubMemberPageReqVO extends PageParam {

    @Schema(description = "俱乐部 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long clubId;

    @Schema(description = "员工用户 ID", example = "100")
    private Long userId;

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 规则版本分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RuleVersionPageReqVO extends PageParam {

    @Schema(description = "版本号", example = "V2026.01")
    private String versionNo;

    @Schema(description = "规则名称", example = "2026")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

}

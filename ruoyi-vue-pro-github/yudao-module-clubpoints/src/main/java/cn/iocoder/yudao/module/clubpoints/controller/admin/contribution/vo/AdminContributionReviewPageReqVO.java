package cn.iocoder.yudao.module.clubpoints.controller.admin.contribution.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Schema(description = "管理后台 - 待审核非签到积分材料分页 Request VO")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminContributionReviewPageReqVO extends PageParam {

    private Long clubId;
    private Integer type;

}

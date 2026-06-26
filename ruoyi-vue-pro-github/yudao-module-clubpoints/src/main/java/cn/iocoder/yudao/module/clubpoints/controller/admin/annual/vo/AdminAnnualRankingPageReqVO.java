package cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminAnnualRankingPageReqVO extends PageParam {

    @NotNull
    private Integer year;

}

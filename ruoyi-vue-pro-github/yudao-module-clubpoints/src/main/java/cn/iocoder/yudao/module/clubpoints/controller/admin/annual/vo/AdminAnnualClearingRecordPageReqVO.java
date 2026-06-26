package cn.iocoder.yudao.module.clubpoints.controller.admin.annual.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminAnnualClearingRecordPageReqVO extends PageParam {

    private Integer year;
    private Long userId;
    private Integer status;

}

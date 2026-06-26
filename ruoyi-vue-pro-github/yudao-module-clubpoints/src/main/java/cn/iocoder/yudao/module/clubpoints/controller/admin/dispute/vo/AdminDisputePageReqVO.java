package cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AdminDisputePageReqVO extends PageParam {

    private Long userId;
    private Integer status;
    private Integer targetType;
    private Long targetId;

}

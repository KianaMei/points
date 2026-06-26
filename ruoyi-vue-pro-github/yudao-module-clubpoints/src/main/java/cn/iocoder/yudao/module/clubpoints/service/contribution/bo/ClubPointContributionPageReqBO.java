package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 非签到积分材料分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointContributionPageReqBO extends PageParam {

    private Long clubId;
    private Integer type;
    private Integer status;

}

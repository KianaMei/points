package cn.iocoder.yudao.module.clubpoints.service.activity.bo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 活动报名分页查询参数
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ClubPointRegistrationPageReqBO extends PageParam {

    private Long clubId;
    private Long activityId;
    private Integer status;
    private Long userId;

}

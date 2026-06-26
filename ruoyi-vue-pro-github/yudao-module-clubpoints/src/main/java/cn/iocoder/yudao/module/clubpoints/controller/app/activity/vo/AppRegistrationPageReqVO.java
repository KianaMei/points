package cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 员工报名分页请求
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AppRegistrationPageReqVO extends PageParam {

    private Long clubId;
    private Long activityId;
    private Integer status;

}

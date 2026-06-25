package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityPageReqBO;

/**
 * 活动查询服务
 */
public interface ClubPointActivityQueryService {

    PageResult<ClubPointActivityInfoBO> getAppActivityPage(Long loginUserId, ClubPointActivityPageReqBO reqBO);

    ClubPointActivityInfoBO getAppActivity(Long loginUserId, Long activityId);

    PageResult<ClubPointActivityInfoBO> getLeaderActivityPage(Long loginUserId, ClubPointActivityPageReqBO reqBO);

    ClubPointActivityInfoBO getLeaderActivity(Long loginUserId, Long activityId);

    PageResult<ClubPointActivityInfoBO> getAdminActivityPage(ClubPointActivityPageReqBO reqBO);

    ClubPointActivityInfoBO getAdminActivity(Long activityId);

}

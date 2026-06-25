package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;

/**
 * 活动签到签退服务
 */
public interface ClubPointAttendanceService {

    Long checkIn(ClubPointAttendanceSelfReqBO reqBO);

    Long checkOut(ClubPointAttendanceSelfReqBO reqBO);

}

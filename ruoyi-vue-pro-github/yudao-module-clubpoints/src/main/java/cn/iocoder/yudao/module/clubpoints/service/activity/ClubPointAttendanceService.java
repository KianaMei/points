package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceCorrectReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSupplementReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointSpecialAbsenceReqBO;

/**
 * 活动签到签退服务
 */
public interface ClubPointAttendanceService {

    Long checkIn(ClubPointAttendanceSelfReqBO reqBO);

    Long checkOut(ClubPointAttendanceSelfReqBO reqBO);

    Long supplementAttendance(ClubPointAttendanceSupplementReqBO reqBO);

    Long correctAttendance(ClubPointAttendanceCorrectReqBO reqBO);

    void markSpecialAbsence(ClubPointSpecialAbsenceReqBO reqBO);

}

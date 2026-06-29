package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceCorrectReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendancePageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSupplementReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointSpecialAbsenceReqBO;

/**
 * 活动签到签退服务
 */
public interface ClubPointAttendanceService {

    Long checkIn(ClubPointAttendanceSelfReqBO reqBO);

    Long checkOut(ClubPointAttendanceSelfReqBO reqBO);

    PageResult<ClubPointAttendanceRecordDO> getLeaderAttendancePage(Long loginUserId, ClubPointAttendancePageReqBO reqBO);

    Long supplementAttendance(ClubPointAttendanceSupplementReqBO reqBO);

    Long correctAttendance(ClubPointAttendanceCorrectReqBO reqBO);

    void markSpecialAbsence(ClubPointSpecialAbsenceReqBO reqBO);

}

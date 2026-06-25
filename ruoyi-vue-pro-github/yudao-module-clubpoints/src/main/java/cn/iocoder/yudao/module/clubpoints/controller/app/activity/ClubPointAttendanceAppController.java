package cn.iocoder.yudao.module.clubpoints.controller.app.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppAttendanceCheckReqVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSelfReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 签到签退")
@RestController
@RequestMapping("/clubpoints/app/attendance")
@Validated
public class ClubPointAttendanceAppController {

    @Resource
    private ClubPointAttendanceService attendanceService;

    @PostMapping("/check-in")
    @Operation(summary = "签到")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:check-in')")
    public CommonResult<Long> checkIn(@RequestBody @Valid AppAttendanceCheckReqVO reqVO) {
        return success(attendanceService.checkIn(toSelfReqBO(reqVO)));
    }

    @PostMapping("/check-out")
    @Operation(summary = "签退")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:check-out')")
    public CommonResult<Long> checkOut(@RequestBody @Valid AppAttendanceCheckReqVO reqVO) {
        return success(attendanceService.checkOut(toSelfReqBO(reqVO)));
    }

    private static ClubPointAttendanceSelfReqBO toSelfReqBO(AppAttendanceCheckReqVO reqVO) {
        return new ClubPointAttendanceSelfReqBO()
                .setRegistrationId(reqVO.getRegistrationId())
                .setUserId(getLoginUserId())
                .setRecordTime(null)
                .setClientIp(ServletUtils.getClientIP())
                .setRemark(reqVO.getRemark());
    }

}

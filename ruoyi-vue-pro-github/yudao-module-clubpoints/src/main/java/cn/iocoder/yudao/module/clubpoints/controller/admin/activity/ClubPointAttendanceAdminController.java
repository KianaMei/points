package cn.iocoder.yudao.module.clubpoints.controller.admin.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminAttendanceCorrectReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminAttendanceSupplementReqVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceCorrectReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSupplementReqBO;
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
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 签到修正")
@RestController
@RequestMapping("/clubpoints/attendance")
@Validated
public class ClubPointAttendanceAdminController {

    @Resource
    private ClubPointAttendanceService attendanceService;

    @PostMapping("/supplement")
    @Operation(summary = "补录签到签退")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:correct')")
    public CommonResult<Long> supplementAttendance(@RequestBody @Valid AdminAttendanceSupplementReqVO reqVO) {
        return success(attendanceService.supplementAttendance(new ClubPointAttendanceSupplementReqBO()
                .setRegistrationId(reqVO.getRegistrationId())
                .setTargetType(reqVO.getTargetType())
                .setRecordTime(reqVO.getRecordTime())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperatorGlobalScope(true)
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    @PostMapping("/correct")
    @Operation(summary = "修正签到签退")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:correct')")
    public CommonResult<Long> correctAttendance(@RequestBody @Valid AdminAttendanceCorrectReqVO reqVO) {
        return success(attendanceService.correctAttendance(new ClubPointAttendanceCorrectReqBO()
                .setAttendanceRecordId(reqVO.getAttendanceRecordId())
                .setNewRecordTime(reqVO.getNewRecordTime())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperatorGlobalScope(true)
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

}

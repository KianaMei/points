package cn.iocoder.yudao.module.clubpoints.controller.admin.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo.AdminSpecialAbsenceReqVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointSpecialAbsenceReqBO;
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

@Tag(name = "管理后台 - 活动报名")
@RestController
@RequestMapping("/clubpoints/registration")
@Validated
public class ClubPointRegistrationAdminController {

    @Resource
    private ClubPointAttendanceService attendanceService;

    @PostMapping("/mark-special-absence")
    @Operation(summary = "标记特殊缺席")
    @PreAuthorize("@ss.hasPermission('clubpoints:registration:special-absence')")
    public CommonResult<Boolean> markSpecialAbsence(@RequestBody @Valid AdminSpecialAbsenceReqVO reqVO) {
        attendanceService.markSpecialAbsence(new ClubPointSpecialAbsenceReqBO()
                .setRegistrationId(reqVO.getRegistrationId())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setOperatorGlobalScope(true)
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent()));
        return success(true);
    }

}

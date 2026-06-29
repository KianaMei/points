package cn.iocoder.yudao.module.clubpoints.controller.leader.registration;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.leader.registration.vo.LeaderRegistrationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.registration.vo.LeaderRegistrationRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.registration.vo.LeaderSpecialAbsenceReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceService;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointRegistrationService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointSpecialAbsenceReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

@Tag(name = "负责人端 - 活动报名")
@RestController
@RequestMapping("/clubpoints/leader/registration")
@Validated
public class ClubPointRegistrationLeaderController {

    @Resource
    private ClubPointRegistrationService registrationService;
    @Resource
    private ClubPointAttendanceService attendanceService;

    @GetMapping("/page")
    @Operation(summary = "负责俱乐部报名分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:registration:query')")
    public CommonResult<PageResult<LeaderRegistrationRespVO>> getRegistrationPage(
            @Valid LeaderRegistrationPageReqVO pageReqVO) {
        PageResult<ClubPointActivityRegistrationDO> pageResult = registrationService.getLeaderRegistrationPage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), LeaderRegistrationRespVO.class),
                pageResult.getTotal()));
    }

    @PostMapping("/mark-special-absence")
    @Operation(summary = "标记特殊缺席")
    @PreAuthorize("@ss.hasPermission('clubpoints:registration:special-absence')")
    public CommonResult<Boolean> markSpecialAbsence(@RequestBody @Valid LeaderSpecialAbsenceReqVO reqVO) {
        attendanceService.markSpecialAbsence(new ClubPointSpecialAbsenceReqBO()
                .setRegistrationId(reqVO.getId())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setOperatorGlobalScope(false)
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent()));
        return success(true);
    }

    private static ClubPointRegistrationPageReqBO toPageReqBO(LeaderRegistrationPageReqVO reqVO) {
        ClubPointRegistrationPageReqBO reqBO = new ClubPointRegistrationPageReqBO()
                .setClubId(reqVO.getClubId())
                .setActivityId(reqVO.getActivityId())
                .setStatus(reqVO.getStatus())
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

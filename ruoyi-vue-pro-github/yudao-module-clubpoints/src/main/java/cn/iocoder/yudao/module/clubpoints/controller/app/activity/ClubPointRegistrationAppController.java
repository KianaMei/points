package cn.iocoder.yudao.module.clubpoints.controller.app.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationCancelReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationCreateReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppRegistrationRespVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointRegistrationService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
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

@Tag(name = "员工端 - 活动报名")
@RestController
@RequestMapping("/clubpoints/app/registration")
@Validated
public class ClubPointRegistrationAppController {

    @Resource
    private ClubPointRegistrationService registrationService;

    @GetMapping("/my-page")
    @Operation(summary = "我的报名分页")
    public CommonResult<PageResult<AppRegistrationRespVO>> getMyRegistrationPage(@Valid AppRegistrationPageReqVO reqVO) {
        PageResult<AppRegistrationRespVO> pageResult = BeanUtils.toBean(registrationService.getAdminRegistrationPage(
                toPageReqBO(reqVO).setUserId(getLoginUserId())), AppRegistrationRespVO.class);
        return success(pageResult);
    }

    @PostMapping("/create")
    @Operation(summary = "报名活动")
    @PreAuthorize("@ss.hasPermission('clubpoints:registration:create')")
    public CommonResult<Long> createRegistration(@RequestBody @Valid AppRegistrationCreateReqVO reqVO) {
        return success(registrationService.createRegistration(new ClubPointRegistrationCreateReqBO()
                .setActivityId(reqVO.getActivityId())
                .setUserId(getLoginUserId())
                .setOperationTime(LocalDateTime.now())));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消报名")
    @PreAuthorize("@ss.hasPermission('clubpoints:registration:cancel')")
    public CommonResult<Boolean> cancelRegistration(@RequestBody @Valid AppRegistrationCancelReqVO reqVO) {
        registrationService.cancelRegistration(new ClubPointRegistrationCancelReqBO()
                .setRegistrationId(reqVO.getRegistrationId())
                .setUserId(getLoginUserId())
                .setReason(reqVO.getReason())
                .setOperationTime(LocalDateTime.now()));
        return success(true);
    }

    private static ClubPointRegistrationPageReqBO toPageReqBO(AppRegistrationPageReqVO reqVO) {
        ClubPointRegistrationPageReqBO reqBO = new ClubPointRegistrationPageReqBO()
                .setClubId(reqVO.getClubId())
                .setActivityId(reqVO.getActivityId())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

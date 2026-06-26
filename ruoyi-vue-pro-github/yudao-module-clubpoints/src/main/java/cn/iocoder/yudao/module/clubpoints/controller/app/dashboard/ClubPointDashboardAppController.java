package cn.iocoder.yudao.module.clubpoints.controller.app.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.dashboard.vo.AppDashboardSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.ClubPointDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 俱乐部积分工作台")
@RestController
@RequestMapping("/clubpoints/app/dashboard")
@Validated
public class ClubPointDashboardAppController {

    @Resource
    private ClubPointDashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "我的工作台汇总")
    public CommonResult<AppDashboardSummaryRespVO> getSummary() {
        return success(BeanUtils.toBean(dashboardService.getAppSummary(getLoginUserId()),
                AppDashboardSummaryRespVO.class));
    }

}

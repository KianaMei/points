package cn.iocoder.yudao.module.clubpoints.controller.leader.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.leader.dashboard.vo.LeaderDashboardSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.ClubPointDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "负责人端 - 俱乐部积分工作台")
@RestController
@RequestMapping("/clubpoints/leader/dashboard")
@Validated
public class ClubPointDashboardLeaderController {

    @Resource
    private ClubPointDashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "负责人工作台汇总")
    @PreAuthorize("@ss.hasPermission('clubpoints:leader')")
    public CommonResult<LeaderDashboardSummaryRespVO> getSummary() {
        return success(BeanUtils.toBean(dashboardService.getLeaderSummary(getLoginUserId()),
                LeaderDashboardSummaryRespVO.class));
    }

}

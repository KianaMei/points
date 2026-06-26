package cn.iocoder.yudao.module.clubpoints.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.dashboard.vo.AdminDashboardSummaryRespVO;
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

@Tag(name = "管理后台 - 俱乐部积分工作台")
@RestController
@RequestMapping("/clubpoints/admin/dashboard")
@Validated
public class ClubPointDashboardAdminController {

    @Resource
    private ClubPointDashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "管理员工作台汇总")
    @PreAuthorize("@ss.hasPermission('clubpoints:dashboard:query')")
    public CommonResult<AdminDashboardSummaryRespVO> getSummary() {
        return success(BeanUtils.toBean(dashboardService.getAdminSummary(), AdminDashboardSummaryRespVO.class));
    }

}

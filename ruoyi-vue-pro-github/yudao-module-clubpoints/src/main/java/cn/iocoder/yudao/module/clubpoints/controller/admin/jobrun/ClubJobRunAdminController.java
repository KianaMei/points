package cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo.AdminJobRunHandleReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo.AdminJobRunPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.jobrun.vo.AdminJobRunRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.ClubJobRunAdminService;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 俱乐部积分任务运行")
@RestController
@RequestMapping("/clubpoints/job-run")
@Validated
public class ClubJobRunAdminController {

    @Resource
    private ClubJobRunAdminService jobRunAdminService;

    @GetMapping("/page")
    @Operation(summary = "任务运行分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:job:query')")
    public CommonResult<PageResult<AdminJobRunRespVO>> getJobRunPage(@Valid AdminJobRunPageReqVO pageReqVO) {
        PageResult<ClubJobRunDO> pageResult = jobRunAdminService.getJobRunPage(toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminJobRunRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/detail")
    @Operation(summary = "任务运行详情")
    @Parameter(name = "id", description = "任务运行记录 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:job:query')")
    public CommonResult<AdminJobRunRespVO> getJobRunDetail(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(jobRunAdminService.getJobRunDetail(id), AdminJobRunRespVO.class));
    }

    @PostMapping("/handle")
    @Operation(summary = "人工处理任务")
    @PreAuthorize("@ss.hasPermission('clubpoints:job:handle')")
    public CommonResult<String> handleJobRun(@RequestBody @Valid AdminJobRunHandleReqVO reqVO) throws Exception {
        return success(jobRunAdminService.handleJobRun(new ClubJobRunHandleReqBO()
                .setId(reqVO.getId())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    private static ClubJobRunPageReqBO toPageReqBO(AdminJobRunPageReqVO reqVO) {
        ClubJobRunPageReqBO reqBO = BeanUtils.toBean(reqVO, ClubJobRunPageReqBO.class);
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

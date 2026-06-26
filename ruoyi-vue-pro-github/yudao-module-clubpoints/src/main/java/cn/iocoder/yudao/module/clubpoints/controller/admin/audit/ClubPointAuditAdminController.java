package cn.iocoder.yudao.module.clubpoints.controller.admin.audit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo.AdminAuditPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.audit.vo.AdminAuditRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditService;
import cn.iocoder.yudao.module.clubpoints.service.audit.bo.ClubAuditPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 俱乐部积分强审计")
@RestController
@RequestMapping("/clubpoints/audit")
@Validated
public class ClubPointAuditAdminController {

    @Resource
    private ClubAuditService auditService;

    @GetMapping("/page")
    @Operation(summary = "强审计日志分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:audit:query')")
    public CommonResult<PageResult<AdminAuditRespVO>> getAuditPage(@Valid AdminAuditPageReqVO pageReqVO) {
        PageResult<ClubAuditLogDO> pageResult = auditService.getAuditPage(
                BeanUtils.toBean(pageReqVO, ClubAuditPageReqBO.class));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminAuditRespVO.class),
                pageResult.getTotal()));
    }

}

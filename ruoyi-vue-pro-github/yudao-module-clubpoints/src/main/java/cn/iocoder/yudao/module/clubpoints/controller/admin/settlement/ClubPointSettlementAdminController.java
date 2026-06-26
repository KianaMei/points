package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementDetailRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementPendingActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementPendingActivityRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementRunRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo.AdminSettlementTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementAdminService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementDetailBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementManualRunReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementPendingActivityPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointSettlementRunPageReqBO;
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

@Tag(name = "管理后台 - 活动结算")
@RestController
@RequestMapping("/clubpoints/settlement")
@Validated
public class ClubPointSettlementAdminController {

    @Resource
    private ClubPointActivitySettlementAdminService settlementAdminService;

    @GetMapping("/pending-activity-page")
    @Operation(summary = "待结算活动分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:settlement:query')")
    public CommonResult<PageResult<AdminSettlementPendingActivityRespVO>> getPendingActivityPage(
            @Valid AdminSettlementPendingActivityPageReqVO pageReqVO) {
        PageResult<ClubPointActivityDO> pageResult = settlementAdminService.getPendingActivityPage(
                toPendingActivityPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(),
                AdminSettlementPendingActivityRespVO.class), pageResult.getTotal()));
    }

    @PostMapping("/run")
    @Operation(summary = "手动触发活动结算")
    @PreAuthorize("@ss.hasPermission('clubpoints:settlement:run')")
    public CommonResult<String> runSettlement(@RequestBody @Valid AdminSettlementRunReqVO reqVO) throws Exception {
        return success(settlementAdminService.runSettlement(new ClubPointSettlementManualRunReqBO()
                .setActivityId(reqVO.getActivityId())
                .setForce(Boolean.TRUE.equals(reqVO.getForce()))
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    @GetMapping("/page")
    @Operation(summary = "结算运行记录分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:settlement:query')")
    public CommonResult<PageResult<AdminSettlementRunRespVO>> getRunPage(
            @Valid AdminSettlementRunPageReqVO pageReqVO) {
        PageResult<ClubPointActivitySettlementRunDO> pageResult = settlementAdminService.getRunPage(
                toRunPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminSettlementRunRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/detail")
    @Operation(summary = "结算明细")
    @Parameter(name = "id", description = "结算运行记录 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:settlement:query')")
    public CommonResult<AdminSettlementDetailRespVO> getDetail(@RequestParam("id") Long id) {
        ClubPointSettlementDetailBO detailBO = settlementAdminService.getDetail(id);
        return success(new AdminSettlementDetailRespVO()
                .setRun(BeanUtils.toBean(detailBO.getRun(), AdminSettlementRunRespVO.class))
                .setTransactions(BeanUtils.toBean(detailBO.getTransactions(),
                        AdminSettlementTransactionRespVO.class)));
    }

    private static ClubPointSettlementPendingActivityPageReqBO toPendingActivityPageReqBO(
            AdminSettlementPendingActivityPageReqVO reqVO) {
        ClubPointSettlementPendingActivityPageReqBO reqBO = new ClubPointSettlementPendingActivityPageReqBO()
                .setClubId(reqVO.getClubId())
                .setKeyword(reqVO.getKeyword());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointSettlementRunPageReqBO toRunPageReqBO(AdminSettlementRunPageReqVO reqVO) {
        ClubPointSettlementRunPageReqBO reqBO = new ClubPointSettlementRunPageReqBO()
                .setActivityId(reqVO.getActivityId())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerAccountRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo.AdminLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerTransactionBO;
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

@Tag(name = "管理后台 - 俱乐部积分账本")
@RestController
@RequestMapping("/clubpoints/ledger")
@Validated
public class ClubPointLedgerAdminController {

    @Resource
    private ClubPointLedgerQueryService clubPointLedgerQueryService;

    @GetMapping("/account-page")
    @Operation(summary = "积分账户分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:ledger:query')")
    public CommonResult<PageResult<AdminLedgerAccountRespVO>> getAccountPage(
            @Valid AdminLedgerAccountPageReqVO pageReqVO) {
        PageResult<ClubPointAccountDO> pageResult = clubPointLedgerQueryService.getAdminAccountPage(
                toAccountPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminLedgerAccountRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/transaction-page")
    @Operation(summary = "积分流水分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:ledger:query')")
    public CommonResult<PageResult<AdminLedgerTransactionRespVO>> getTransactionPage(
            @Valid AdminLedgerTransactionPageReqVO pageReqVO) {
        PageResult<ClubPointLedgerTransactionBO> pageResult = clubPointLedgerQueryService.getAdminTransactionPage(
                toLedgerPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminLedgerTransactionRespVO.class),
                pageResult.getTotal()));
    }

    private static ClubPointAccountPageReqBO toAccountPageReqBO(AdminLedgerAccountPageReqVO reqVO) {
        ClubPointAccountPageReqBO reqBO = new ClubPointAccountPageReqBO()
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointLedgerPageReqBO toLedgerPageReqBO(AdminLedgerTransactionPageReqVO reqVO) {
        ClubPointLedgerPageReqBO reqBO = new ClubPointLedgerPageReqBO()
                .setUserId(reqVO.getUserId())
                .setClubId(reqVO.getClubId())
                .setDirection(reqVO.getDirection())
                .setPointCategory(reqVO.getPointCategory())
                .setSourceType(reqVO.getSourceType())
                .setStartTime(reqVO.getStartTime())
                .setEndTime(reqVO.getEndTime());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

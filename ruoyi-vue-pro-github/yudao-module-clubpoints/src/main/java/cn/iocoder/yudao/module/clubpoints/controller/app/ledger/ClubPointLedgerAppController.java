package cn.iocoder.yudao.module.clubpoints.controller.app.ledger;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.ledger.vo.AppLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerTransactionBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 俱乐部积分账本")
@RestController
@RequestMapping("/clubpoints/app/ledger")
@Validated
public class ClubPointLedgerAppController {

    @Resource
    private ClubPointLedgerQueryService clubPointLedgerQueryService;

    @GetMapping("/summary")
    @Operation(summary = "我的积分概览")
    public CommonResult<AppLedgerSummaryRespVO> getSummary() {
        return success(BeanUtils.toBean(clubPointLedgerQueryService.getAppSummary(getLoginUserId()),
                AppLedgerSummaryRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "我的积分流水分页")
    public CommonResult<PageResult<AppLedgerTransactionRespVO>> getTransactionPage(
            @Valid AppLedgerTransactionPageReqVO pageReqVO) {
        PageResult<ClubPointLedgerTransactionBO> pageResult = clubPointLedgerQueryService.getAppTransactionPage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AppLedgerTransactionRespVO.class),
                pageResult.getTotal()));
    }

    private static ClubPointLedgerPageReqBO toPageReqBO(AppLedgerTransactionPageReqVO reqVO) {
        ClubPointLedgerPageReqBO reqBO = new ClubPointLedgerPageReqBO()
                .setDirection(reqVO.getDirection())
                .setPointCategory(reqVO.getPointCategory())
                .setSourceType(reqVO.getSourceType())
                .setClubId(reqVO.getClubId())
                .setStartTime(reqVO.getStartTime())
                .setEndTime(reqVO.getEndTime());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

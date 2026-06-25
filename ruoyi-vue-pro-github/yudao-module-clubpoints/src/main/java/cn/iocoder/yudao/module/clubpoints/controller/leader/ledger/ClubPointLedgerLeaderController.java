package cn.iocoder.yudao.module.clubpoints.controller.leader.ledger;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerMemberSummaryPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerMemberSummaryRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerTransactionPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.ledger.vo.LeaderLedgerTransactionRespVO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerQueryService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerMemberSummaryBO;
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
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "负责人端 - 俱乐部积分账本")
@RestController
@RequestMapping("/clubpoints/leader/ledger")
@Validated
public class ClubPointLedgerLeaderController {

    @Resource
    private ClubPointLedgerQueryService clubPointLedgerQueryService;

    @GetMapping("/member-summary-page")
    @Operation(summary = "负责俱乐部成员积分摘要分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:leader')")
    public CommonResult<PageResult<LeaderLedgerMemberSummaryRespVO>> getMemberSummaryPage(
            @Valid LeaderLedgerMemberSummaryPageReqVO pageReqVO) {
        PageResult<ClubPointLedgerMemberSummaryBO> pageResult = clubPointLedgerQueryService.getLeaderMemberSummaryPage(
                getLoginUserId(), toAccountPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), LeaderLedgerMemberSummaryRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/transaction-page")
    @Operation(summary = "负责俱乐部积分流水分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:leader')")
    public CommonResult<PageResult<LeaderLedgerTransactionRespVO>> getTransactionPage(
            @Valid LeaderLedgerTransactionPageReqVO pageReqVO) {
        PageResult<ClubPointLedgerTransactionBO> pageResult = clubPointLedgerQueryService.getLeaderTransactionPage(
                getLoginUserId(), toLedgerPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), LeaderLedgerTransactionRespVO.class),
                pageResult.getTotal()));
    }

    private static ClubPointAccountPageReqBO toAccountPageReqBO(LeaderLedgerMemberSummaryPageReqVO reqVO) {
        ClubPointAccountPageReqBO reqBO = new ClubPointAccountPageReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointLedgerPageReqBO toLedgerPageReqBO(LeaderLedgerTransactionPageReqVO reqVO) {
        ClubPointLedgerPageReqBO reqBO = new ClubPointLedgerPageReqBO()
                .setClubId(reqVO.getClubId())
                .setUserId(reqVO.getUserId())
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

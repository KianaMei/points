package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo.AdminRedemptionGiftStatusReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.ClubPointRedemptionGiftService;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionGiftSaveReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 兑换礼品")
@RestController
@RequestMapping("/clubpoints/redemption-gift")
@Validated
public class ClubPointRedemptionGiftAdminController {

    @Resource
    private ClubPointRedemptionGiftService giftService;

    @GetMapping("/page")
    @Operation(summary = "兑换礼品分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-gift:manage')")
    public CommonResult<PageResult<AdminRedemptionGiftRespVO>> getGiftPage(
            @Valid AdminRedemptionGiftPageReqVO pageReqVO) {
        PageResult<ClubPointRedemptionGiftDO> pageResult =
                giftService.getAdminGiftPage(true, toPageReqBO(pageReqVO));
        return success(new PageResult<>(pageResult.getList().stream()
                .map(ClubPointRedemptionGiftAdminController::toResp)
                .collect(Collectors.toList()), pageResult.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建兑换礼品")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-gift:manage')")
    public CommonResult<Long> createGift(@RequestBody @Valid AdminRedemptionGiftSaveReqVO reqVO) {
        return success(giftService.createGift(toSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改兑换礼品")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-gift:manage')")
    public CommonResult<Boolean> updateGift(@RequestBody @Valid AdminRedemptionGiftSaveReqVO reqVO) {
        giftService.updateGift(toSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/update-status")
    @Operation(summary = "修改兑换礼品状态")
    @PreAuthorize("@ss.hasPermission('clubpoints:redemption-gift:manage')")
    public CommonResult<Boolean> updateGiftStatus(@RequestBody @Valid AdminRedemptionGiftStatusReqVO reqVO) {
        giftService.updateGiftStatus(reqVO.getId(), reqVO.getStatus(), toOperationReqBO(reqVO));
        return success(true);
    }

    private static ClubPointRedemptionGiftPageReqBO toPageReqBO(AdminRedemptionGiftPageReqVO reqVO) {
        ClubPointRedemptionGiftPageReqBO reqBO = new ClubPointRedemptionGiftPageReqBO()
                .setBatchId(reqVO.getBatchId())
                .setStatus(reqVO.getStatus())
                .setKeyword(reqVO.getKeyword());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointRedemptionGiftSaveReqBO toSaveReqBO(AdminRedemptionGiftSaveReqVO reqVO) {
        return new ClubPointRedemptionGiftSaveReqBO()
                .setId(reqVO.getId())
                .setBatchId(reqVO.getBatchId())
                .setName(reqVO.getName())
                .setDescription(reqVO.getDescription())
                .setPointsCost(reqVO.getPointsCost())
                .setTierMinPoints(reqVO.getTierMinPoints())
                .setTierMaxPoints(reqVO.getTierMaxPoints())
                .setReferenceAmountCent(reqVO.getReferenceAmountCent())
                .setStockTotal(reqVO.getStockTotal())
                .setImageFileId(reqVO.getImageFileId())
                .setSort(reqVO.getSort())
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason());
    }

    private static ClubPointRedemptionGiftOperationReqBO toOperationReqBO(AdminRedemptionGiftStatusReqVO reqVO) {
        return new ClubPointRedemptionGiftOperationReqBO()
                .setOperatorGlobalScope(true)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(StringUtils.hasText(reqVO.getReason()) ? reqVO.getReason() : "修改兑换礼品状态");
    }

    static AdminRedemptionGiftRespVO toResp(ClubPointRedemptionGiftDO gift) {
        return new AdminRedemptionGiftRespVO()
                .setId(gift.getId())
                .setBatchId(gift.getBatchId())
                .setName(gift.getName())
                .setDescription(gift.getDescription())
                .setPointsCost(gift.getPointsCost())
                .setTierMinPoints(gift.getTierMinPoints())
                .setTierMaxPoints(gift.getTierMaxPoints())
                .setReferenceAmountCent(gift.getReferenceAmountCent())
                .setStockTotal(gift.getStockTotal())
                .setStockLocked(gift.getStockLocked())
                .setStockUsed(gift.getStockUsed())
                .setStatus(gift.getStatus())
                .setImageFileId(gift.getImageFileId())
                .setSort(gift.getSort())
                .setGiftSnapshotJson(gift.getGiftSnapshotJson());
    }

}

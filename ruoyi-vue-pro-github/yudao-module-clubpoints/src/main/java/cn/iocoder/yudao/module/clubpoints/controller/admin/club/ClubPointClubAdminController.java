package cn.iocoder.yudao.module.clubpoints.controller.admin.club;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubDeleteReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubOperationReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.admin.club.vo.AdminClubSaveReqVO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubQueryService;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubService;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDeleteReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDisableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubSaveReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "管理后台 - 俱乐部")
@RestController
@RequestMapping("/clubpoints/club")
@Validated
public class ClubPointClubAdminController {

    @Resource
    private ClubPointClubQueryService clubPointClubQueryService;
    @Resource
    private ClubPointClubService clubPointClubService;

    @GetMapping("/page")
    @Operation(summary = "俱乐部分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:query')")
    public CommonResult<PageResult<AdminClubRespVO>> getClubPage(@Valid AdminClubPageReqVO pageReqVO) {
        PageResult<ClubPointClubInfoBO> pageResult = clubPointClubQueryService.getAdminClubPage(toPageReqBO(pageReqVO));
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AdminClubRespVO.class),
                pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "俱乐部详情")
    @Parameter(name = "id", description = "俱乐部 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:query')")
    public CommonResult<AdminClubRespVO> getClub(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(clubPointClubQueryService.getAdminClub(id), AdminClubRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "创建俱乐部")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:create')")
    public CommonResult<Long> createClub(@RequestBody @Valid AdminClubSaveReqVO reqVO) {
        return success(clubPointClubService.createClub(toSaveReqBO(reqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "修改俱乐部")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:update')")
    public CommonResult<Boolean> updateClub(@RequestBody @Valid AdminClubSaveReqVO reqVO) {
        clubPointClubService.updateClub(toSaveReqBO(reqVO));
        return success(true);
    }

    @PostMapping("/disable")
    @Operation(summary = "停用俱乐部")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:disable')")
    public CommonResult<Boolean> disableClub(@RequestBody @Valid AdminClubOperationReqVO reqVO) {
        clubPointClubService.disableClub(new ClubPointClubDisableReqBO()
                .setId(reqVO.getId())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason()));
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "物理删除俱乐部")
    @PreAuthorize("@ss.hasPermission('clubpoints:club:delete')")
    public CommonResult<Boolean> deleteClub(@RequestBody @Valid AdminClubDeleteReqVO reqVO) {
        clubPointClubService.deleteClubPhysically(new ClubPointClubDeleteReqBO()
                .setId(reqVO.getId())
                .setStrongConfirm(reqVO.getStrongConfirm())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())
                .setReason(reqVO.getReason()));
        return success(true);
    }

    private static ClubPointClubPageReqBO toPageReqBO(AdminClubPageReqVO reqVO) {
        ClubPointClubPageReqBO reqBO = new ClubPointClubPageReqBO()
                .setKeyword(reqVO.getKeyword())
                .setStatus(reqVO.getStatus());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private static ClubPointClubSaveReqBO toSaveReqBO(AdminClubSaveReqVO reqVO) {
        return BeanUtils.toBean(reqVO, ClubPointClubSaveReqBO.class)
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("admin")
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent());
    }

}

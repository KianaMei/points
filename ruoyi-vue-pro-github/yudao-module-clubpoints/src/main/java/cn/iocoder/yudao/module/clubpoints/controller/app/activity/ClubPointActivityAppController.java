package cn.iocoder.yudao.module.clubpoints.controller.app.activity;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppActivityPageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.activity.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointActivityQueryService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointActivityPageReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 活动")
@RestController
@RequestMapping("/clubpoints/app/activity")
@Validated
public class ClubPointActivityAppController {

    @Resource
    private ClubPointActivityQueryService activityQueryService;

    @GetMapping("/page")
    @Operation(summary = "俱乐部活动分页")
    public CommonResult<PageResult<AppActivityRespVO>> getActivityPage(@Valid AppActivityPageReqVO pageReqVO) {
        PageResult<ClubPointActivityInfoBO> pageResult = activityQueryService.getAppActivityPage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        return success(BeanUtils.toBean(pageResult, AppActivityRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "活动详情")
    @Parameter(name = "id", description = "活动 ID", required = true, example = "1")
    public CommonResult<AppActivityRespVO> getActivity(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(activityQueryService.getAppActivity(getLoginUserId(), id),
                AppActivityRespVO.class));
    }

    private static ClubPointActivityPageReqBO toPageReqBO(AppActivityPageReqVO reqVO) {
        ClubPointActivityPageReqBO reqBO = new ClubPointActivityPageReqBO()
                .setClubId(reqVO.getClubId())
                .setKeyword(reqVO.getKeyword())
                .setStatus(reqVO.getActivityStatus())
                .setStartTime(reqVO.getStartTime())
                .setEndTime(reqVO.getEndTime());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

}

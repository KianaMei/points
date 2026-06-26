package cn.iocoder.yudao.module.clubpoints.controller.app.notify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.clubpoints.controller.app.notify.vo.AppNotifyMessagePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.app.notify.vo.AppNotifyMessageRespVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessageMyPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import cn.iocoder.yudao.module.system.service.notify.NotifyMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "员工端 - 俱乐部积分通知")
@RestController
@RequestMapping("/clubpoints/app/notify")
@Validated
public class ClubPointNotifyAppController {

    @Resource
    private NotifyMessageService notifyMessageService;

    @GetMapping("/my-page")
    @Operation(summary = "我的通知分页")
    public CommonResult<PageResult<AppNotifyMessageRespVO>> getMyNotifyPage(
            @Valid AppNotifyMessagePageReqVO pageReqVO) {
        PageResult<NotifyMessageDO> pageResult = notifyMessageService.getMyMyNotifyMessagePage(
                toMyPageReqVO(pageReqVO), getLoginUserId(), ADMIN.getValue());
        return success(new PageResult<>(BeanUtils.toBean(pageResult.getList(), AppNotifyMessageRespVO.class),
                pageResult.getTotal()));
    }

    @PutMapping("/update-read")
    @Operation(summary = "标记通知已读")
    public CommonResult<Boolean> updateNotifyRead(@RequestParam("ids") List<Long> ids) {
        notifyMessageService.updateNotifyMessageRead(ids, getLoginUserId(), ADMIN.getValue());
        return success(true);
    }

    private static NotifyMessageMyPageReqVO toMyPageReqVO(AppNotifyMessagePageReqVO reqVO) {
        NotifyMessageMyPageReqVO pageReqVO = new NotifyMessageMyPageReqVO();
        pageReqVO.setPageNo(reqVO.getPageNo());
        pageReqVO.setPageSize(reqVO.getPageSize());
        pageReqVO.setReadStatus(reqVO.getReadStatus());
        pageReqVO.setCreateTime(reqVO.getCreateTime());
        return pageReqVO;
    }

}

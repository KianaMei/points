package cn.iocoder.yudao.module.clubpoints.controller.leader.attendance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo.LeaderAttendanceCorrectReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo.LeaderAttendancePageReqVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo.LeaderAttendanceRespVO;
import cn.iocoder.yudao.module.clubpoints.controller.leader.attendance.vo.LeaderAttendanceSupplementReqVO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointAttendanceRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.service.activity.ClubPointAttendanceService;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceCorrectReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendancePageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointAttendanceSupplementReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

@Tag(name = "负责人端 - 签到签退")
@RestController
@RequestMapping("/clubpoints/leader/attendance")
@Validated
public class ClubPointAttendanceLeaderController {

    @Resource
    private ClubPointAttendanceService attendanceService;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;

    @GetMapping("/page")
    @Operation(summary = "负责俱乐部签到签退分页")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:query')")
    public CommonResult<PageResult<LeaderAttendanceRespVO>> getAttendancePage(
            @Valid LeaderAttendancePageReqVO pageReqVO) {
        PageResult<ClubPointAttendanceRecordDO> pageResult = attendanceService.getLeaderAttendancePage(
                getLoginUserId(), toPageReqBO(pageReqVO));
        List<LeaderAttendanceRespVO> list = pageResult.getList().stream()
                .map(this::toRespVO)
                .collect(Collectors.toList());
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @PostMapping("/supplement")
    @Operation(summary = "补录签到签退")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:correct')")
    public CommonResult<Long> supplementAttendance(@RequestBody @Valid LeaderAttendanceSupplementReqVO reqVO) {
        return success(attendanceService.supplementAttendance(new ClubPointAttendanceSupplementReqBO()
                .setRegistrationId(reqVO.getRegistrationId())
                .setTargetType(reqVO.getTargetType())
                .setRecordTime(reqVO.getOccurTime())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setOperatorGlobalScope(false)
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    @PostMapping("/correct")
    @Operation(summary = "修正签到签退")
    @PreAuthorize("@ss.hasPermission('clubpoints:attendance:correct')")
    public CommonResult<Long> correctAttendance(@RequestBody @Valid LeaderAttendanceCorrectReqVO reqVO) {
        return success(attendanceService.correctAttendance(new ClubPointAttendanceCorrectReqBO()
                .setAttendanceRecordId(reqVO.getId())
                .setNewRecordTime(reqVO.getOccurTime())
                .setReason(reqVO.getReason())
                .setOperatorUserId(getLoginUserId())
                .setOperatorNameSnapshot(getLoginUserNickname())
                .setOperatorRoleSnapshot("leader")
                .setOperatorGlobalScope(false)
                .setOperationTime(LocalDateTime.now())
                .setClientIp(ServletUtils.getClientIP())
                .setUserAgent(ServletUtils.getUserAgent())));
    }

    private static ClubPointAttendancePageReqBO toPageReqBO(LeaderAttendancePageReqVO reqVO) {
        ClubPointAttendancePageReqBO reqBO = new ClubPointAttendancePageReqBO()
                .setClubId(reqVO.getClubId())
                .setActivityId(reqVO.getActivityId())
                .setRegistrationId(reqVO.getRegistrationId())
                .setUserId(reqVO.getUserId())
                .setTargetType(reqVO.getTargetType());
        reqBO.setPageNo(reqVO.getPageNo());
        reqBO.setPageSize(reqVO.getPageSize());
        return reqBO;
    }

    private LeaderAttendanceRespVO toRespVO(ClubPointAttendanceRecordDO record) {
        LeaderAttendanceRespVO respVO = BeanUtils.toBean(record, LeaderAttendanceRespVO.class)
                .setOccurTime(record.getRecordTime());
        ClubPointActivityRegistrationDO registration = registrationMapper.selectById(record.getRegistrationId());
        if (registration != null) {
            respVO.setClubId(registration.getClubId())
                    .setUserNameSnapshot(registration.getUserNameSnapshot())
                    .setDeptNameSnapshot(registration.getDeptNameSnapshot())
                    .setClubNameSnapshot(registration.getClubNameSnapshot())
                    .setActivityTitleSnapshot(registration.getActivityTitleSnapshot());
        }
        return respVO;
    }

}

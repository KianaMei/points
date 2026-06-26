package cn.iocoder.yudao.module.clubpoints.controller.admin.activity.vo;

import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * 管理员活动保存请求
 */
@Data
@Accessors(chain = true)
public class AdminActivitySaveReqVO {

    private Long id;
    @NotNull
    private Long clubId;
    @NotNull
    private String title;
    private String location;
    private String description;
    private Long coverFileId;
    @NotNull
    private Integer level;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    @NotNull
    private LocalDateTime registrationDeadline;
    private LocalDateTime cancelDeadlineTime;
    @NotNull
    private LocalDateTime checkinStartTime;
    @NotNull
    private LocalDateTime checkinEndTime;
    @NotNull
    private Integer checkoutMode;
    @NotNull
    private LocalDateTime checkoutStartTime;
    @NotNull
    private LocalDateTime checkoutEndTime;
    private Long ruleVersionId;
    @NotNull
    private Integer basePoints;
    private Integer fullExtraPoints;
    private String remark;
    private String reason;
    private List<AttachmentInputVO> attachments;

}

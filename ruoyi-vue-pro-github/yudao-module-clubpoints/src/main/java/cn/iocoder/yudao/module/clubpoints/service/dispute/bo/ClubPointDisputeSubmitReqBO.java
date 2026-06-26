package cn.iocoder.yudao.module.clubpoints.service.dispute.bo;

import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工提交异议参数
 */
@Data
@Accessors(chain = true)
public class ClubPointDisputeSubmitReqBO {

    private Long userId;
    private String title;
    private String content;
    private Integer targetType;
    private Long targetId;
    private LocalDateTime submitTime;
    private List<ClubAttachmentBindReqBO> attachments;

}

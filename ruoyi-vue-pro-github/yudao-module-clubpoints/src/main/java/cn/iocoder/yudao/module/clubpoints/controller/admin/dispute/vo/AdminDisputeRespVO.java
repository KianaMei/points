package cn.iocoder.yudao.module.clubpoints.controller.admin.dispute.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AdminDisputeRespVO {

    private Long id;
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private String content;
    private Integer status;
    private String replyContent;
    private Integer relatedActionType;
    private Long relatedTransactionId;
    private LocalDateTime createdTime;
    private LocalDateTime handledTime;

}

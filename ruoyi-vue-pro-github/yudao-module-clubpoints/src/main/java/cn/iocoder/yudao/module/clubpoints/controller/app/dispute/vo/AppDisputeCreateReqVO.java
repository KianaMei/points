package cn.iocoder.yudao.module.clubpoints.controller.app.dispute.vo;

import cn.iocoder.yudao.module.clubpoints.controller.admin.rule.vo.AttachmentInputVO;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class AppDisputeCreateReqVO {

    @NotNull
    private Integer targetType;
    @NotNull
    private Long targetId;
    @NotBlank
    private String content;
    private List<AttachmentInputVO> attachments;

}

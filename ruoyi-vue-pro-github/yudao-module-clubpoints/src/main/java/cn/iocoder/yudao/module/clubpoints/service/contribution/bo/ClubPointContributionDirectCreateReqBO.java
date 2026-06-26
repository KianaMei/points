package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 管理员代录非签到积分请求
 */
@Data
@Accessors(chain = true)
public class ClubPointContributionDirectCreateReqBO {

    private String requestNo;
    private Long clubId;
    private Integer type;
    private Long userId;
    private String userNameSnapshot;
    private String deptNameSnapshot;
    private Integer points;
    private Long ruleVersionId;
    private String reason;
    private List<ClubAttachmentBindReqBO> attachments;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;

}

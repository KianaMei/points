package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 管理员弄虚作假处理请求
 */
@Data
@Accessors(chain = true)
public class ClubPointContributionFraudHandleReqBO {

    private String requestNo;
    private Long originalMaterialId;
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

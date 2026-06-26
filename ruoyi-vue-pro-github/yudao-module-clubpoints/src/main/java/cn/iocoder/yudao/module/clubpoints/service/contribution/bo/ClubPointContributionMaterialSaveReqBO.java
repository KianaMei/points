package cn.iocoder.yudao.module.clubpoints.service.contribution.bo;

import cn.iocoder.yudao.module.clubpoints.service.attachment.bo.ClubAttachmentBindReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 非签到积分材料保存参数
 */
@Data
@Accessors(chain = true)
public class ClubPointContributionMaterialSaveReqBO implements ClubPointClubOperationReq {

    private Long id;
    private Long clubId;
    private Integer type;
    private String title;
    private String description;
    private Long ruleVersionId;
    private List<ClubPointContributionItemSaveReqBO> items;
    private List<ClubAttachmentBindReqBO> attachments;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}

package cn.iocoder.yudao.module.clubpoints.service.club.bo;

import cn.iocoder.yudao.module.clubpoints.service.club.ClubPointClubOperationReq;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 俱乐部保存参数
 */
@Data
@Accessors(chain = true)
public class ClubPointClubSaveReqBO implements ClubPointClubOperationReq {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String contactText;
    private Long coverFileId;
    private Integer sort;
    private String remark;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}

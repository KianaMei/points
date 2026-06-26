package cn.iocoder.yudao.module.clubpoints.service.redemption.bo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 兑换礼品保存请求
 */
@Data
@Accessors(chain = true)
public class ClubPointRedemptionGiftSaveReqBO {

    private Long id;
    private Long batchId;
    private String name;
    private String description;
    private Integer pointsCost;
    private Integer tierMinPoints;
    private Integer tierMaxPoints;
    private Long referenceAmountCent;
    private Integer stockTotal;
    private Long imageFileId;
    private Integer sort;
    private Boolean operatorGlobalScope;
    private Long operatorUserId;
    private String operatorNameSnapshot;
    private String operatorRoleSnapshot;
    private String clientIp;
    private String userAgent;
    private String reason;

}

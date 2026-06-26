package cn.iocoder.yudao.module.clubpoints.controller.app.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AppRedemptionGiftRespVO {

    private Long id;
    private Long batchId;
    private String name;
    private String description;
    private Integer pointsCost;
    private Integer tierMinPoints;
    private Integer tierMaxPoints;
    private Long referenceAmountCent;
    private Integer stockTotal;
    private Integer stockLocked;
    private Integer stockUsed;
    private Integer status;
    private Long imageFileId;
    private Integer sort;

}

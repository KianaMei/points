package cn.iocoder.yudao.module.clubpoints.controller.admin.redemption.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class AdminRedemptionGiftSaveReqVO {

    private Long id;
    @NotNull
    private Long batchId;
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private Integer pointsCost;
    private Integer tierMinPoints;
    private Integer tierMaxPoints;
    private Long referenceAmountCent;
    @NotNull
    private Integer stockTotal;
    private Long imageFileId;
    @NotNull
    private Integer sort;
    private String reason;

}

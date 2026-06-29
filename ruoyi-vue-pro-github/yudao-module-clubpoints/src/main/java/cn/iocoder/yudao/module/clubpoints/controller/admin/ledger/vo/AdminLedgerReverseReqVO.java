package cn.iocoder.yudao.module.clubpoints.controller.admin.ledger.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理后台积分流水撤销请求
 */
@Data
@Accessors(chain = true)
public class AdminLedgerReverseReqVO {

    @NotNull
    private Long transactionId;

    @NotBlank
    private String reason;

}

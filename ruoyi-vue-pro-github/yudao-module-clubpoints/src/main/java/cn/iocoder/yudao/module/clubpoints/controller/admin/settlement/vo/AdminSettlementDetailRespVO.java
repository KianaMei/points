package cn.iocoder.yudao.module.clubpoints.controller.admin.settlement.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AdminSettlementDetailRespVO {

    private AdminSettlementRunRespVO run;
    private List<AdminSettlementTransactionRespVO> transactions;

}

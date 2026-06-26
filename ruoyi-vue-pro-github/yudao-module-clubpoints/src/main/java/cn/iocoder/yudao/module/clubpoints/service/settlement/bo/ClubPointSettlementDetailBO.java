package cn.iocoder.yudao.module.clubpoints.service.settlement.bo;

import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.settlement.ClubPointActivitySettlementRunDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ClubPointSettlementDetailBO {

    private ClubPointActivitySettlementRunDO run;
    private List<ClubPointTransactionDO> transactions;

}

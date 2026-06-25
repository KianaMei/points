package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerAdjustReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerReverseReqBO;

/**
 * 积分账本服务
 */
public interface ClubPointLedgerService {

    Long createTransaction(ClubPointLedgerCreateReqBO reqBO);

    Long reverseTransaction(ClubPointLedgerReverseReqBO reqBO);

    Long adjustPoints(ClubPointLedgerAdjustReqBO reqBO);

}

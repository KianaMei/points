package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;

/**
 * 积分账本服务
 */
public interface ClubPointLedgerService {

    Long createTransaction(ClubPointLedgerCreateReqBO reqBO);

}

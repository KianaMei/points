package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeConvertReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeReleaseReqBO;

/**
 * 积分冻结服务
 */
public interface ClubPointFreezeService {

    Long freezePoints(ClubPointFreezeCreateReqBO reqBO);

    void releaseFreeze(ClubPointFreezeReleaseReqBO reqBO);

    Long convertFreezeToDeduction(ClubPointFreezeConvertReqBO reqBO);

}

package cn.iocoder.yudao.module.clubpoints.service.settlement;

import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementRunReqBO;

/**
 * 活动积分结算服务
 */
public interface ClubPointActivitySettlementService {

    Long settleActivity(ClubPointActivitySettlementRunReqBO reqBO);

}

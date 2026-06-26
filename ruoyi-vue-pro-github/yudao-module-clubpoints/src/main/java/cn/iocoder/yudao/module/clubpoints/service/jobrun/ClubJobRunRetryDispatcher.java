package cn.iocoder.yudao.module.clubpoints.service.jobrun;

import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;

public interface ClubJobRunRetryDispatcher {

    String retryActivitySettlement(ClubPointActivitySettlementJobReqBO reqBO) throws Exception;

    String retryAnnualClearing(ClubPointAnnualClearingJobReqBO reqBO) throws Exception;

}

package cn.iocoder.yudao.module.clubpoints.service.jobrun;

import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingJobService;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementJobService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ClubJobRunRetryDispatcherImpl implements ClubJobRunRetryDispatcher {

    @Resource
    private ClubPointActivitySettlementJobService activitySettlementJobService;
    @Resource
    private ClubPointAnnualClearingJobService annualClearingJobService;

    @Override
    public String retryActivitySettlement(ClubPointActivitySettlementJobReqBO reqBO) throws Exception {
        return activitySettlementJobService.run(reqBO);
    }

    @Override
    public String retryAnnualClearing(ClubPointAnnualClearingJobReqBO reqBO) throws Exception {
        return annualClearingJobService.run(reqBO);
    }

}

package cn.iocoder.yudao.module.clubpoints.job.settlement;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.clubpoints.service.settlement.ClubPointActivitySettlementJobService;
import cn.iocoder.yudao.module.clubpoints.service.settlement.bo.ClubPointActivitySettlementJobReqBO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 活动积分结算 Job Handler
 */
@Component
public class ClubPointActivitySettlementJob implements JobHandler {

    @Resource
    private ClubPointActivitySettlementJobService jobService;

    @Override
    public String execute(String param) throws Exception {
        return jobService.run(JsonUtils.parseObject(param, ClubPointActivitySettlementJobReqBO.class));
    }

}

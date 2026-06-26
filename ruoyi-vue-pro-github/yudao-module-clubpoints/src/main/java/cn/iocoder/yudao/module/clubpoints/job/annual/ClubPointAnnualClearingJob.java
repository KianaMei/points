package cn.iocoder.yudao.module.clubpoints.job.annual;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.clubpoints.service.annual.ClubPointAnnualClearingJobService;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearingJobReqBO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 年度清零 Job Handler
 */
@Component("clubPointsAnnualClearingJob")
public class ClubPointAnnualClearingJob implements JobHandler {

    @Resource
    private ClubPointAnnualClearingJobService jobService;

    @Override
    public String execute(String param) throws Exception {
        return jobService.run(JsonUtils.parseObject(param, ClubPointAnnualClearingJobReqBO.class));
    }

}

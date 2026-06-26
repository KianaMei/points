package cn.iocoder.yudao.module.clubpoints.service.jobrun;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.job.ClubJobRunDO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.jobrun.bo.ClubJobRunPageReqBO;

public interface ClubJobRunAdminService {

    PageResult<ClubJobRunDO> getJobRunPage(ClubJobRunPageReqBO reqBO);

    ClubJobRunDO getJobRunDetail(Long id);

    String handleJobRun(ClubJobRunHandleReqBO reqBO) throws Exception;

}

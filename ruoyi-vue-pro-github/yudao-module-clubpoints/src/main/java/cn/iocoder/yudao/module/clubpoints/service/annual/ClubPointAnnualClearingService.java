package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearResultBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearUserReqBO;

/**
 * 年度清零服务
 */
public interface ClubPointAnnualClearingService {

    Long clearUser(ClubPointAnnualClearUserReqBO reqBO);

    ClubPointAnnualClearResultBO clearAll(ClubPointAnnualClearAllReqBO reqBO);

    PageResult<ClubPointAnnualClearingRecordDO> getClearingRecordPage(PageParam pageParam, Integer year,
                                                                      Long userId, Integer status);

}

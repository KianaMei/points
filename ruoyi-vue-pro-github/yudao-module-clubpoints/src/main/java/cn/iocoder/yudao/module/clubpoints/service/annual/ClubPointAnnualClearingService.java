package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearResultBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearUserReqBO;

/**
 * 年度清零服务
 */
public interface ClubPointAnnualClearingService {

    Long clearUser(ClubPointAnnualClearUserReqBO reqBO);

    ClubPointAnnualClearResultBO clearAll(ClubPointAnnualClearAllReqBO reqBO);

}

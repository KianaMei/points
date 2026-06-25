package cn.iocoder.yudao.module.clubpoints.service.activity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityRegistrationDO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCancelReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.activity.bo.ClubPointRegistrationPageReqBO;

/**
 * 活动报名服务
 */
public interface ClubPointRegistrationService {

    Long createRegistration(ClubPointRegistrationCreateReqBO reqBO);

    void cancelRegistration(ClubPointRegistrationCancelReqBO reqBO);

    PageResult<ClubPointActivityRegistrationDO> getLeaderRegistrationPage(Long loginUserId,
                                                                          ClubPointRegistrationPageReqBO reqBO);

    PageResult<ClubPointActivityRegistrationDO> getAdminRegistrationPage(ClubPointRegistrationPageReqBO reqBO);

}

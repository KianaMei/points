package cn.iocoder.yudao.module.clubpoints.service.dispute;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeAcceptReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeSubmitReqBO;

/**
 * 员工积分异议服务
 */
public interface ClubPointDisputeService {

    Long submitDispute(ClubPointDisputeSubmitReqBO reqBO);

    PageResult<ClubPointDisputeDO> getMyDisputePage(Long userId, PageParam pageParam, Integer status);

    ClubPointDisputeDO getMyDispute(Long userId, Long id);

    PageResult<ClubPointDisputeDO> getAdminDisputePage(PageParam pageParam, Long userId, Integer status,
                                                       Integer targetType, Long targetId, Boolean operatorGlobalScope);

    void acceptDispute(ClubPointDisputeAcceptReqBO reqBO);

    Long handleDispute(ClubPointDisputeHandleReqBO reqBO);

    void rejectDispute(ClubPointDisputeHandleReqBO reqBO);

}

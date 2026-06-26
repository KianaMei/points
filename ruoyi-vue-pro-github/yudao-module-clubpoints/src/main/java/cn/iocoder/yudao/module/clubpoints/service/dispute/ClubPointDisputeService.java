package cn.iocoder.yudao.module.clubpoints.service.dispute;

import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeAcceptReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeHandleReqBO;
import cn.iocoder.yudao.module.clubpoints.service.dispute.bo.ClubPointDisputeSubmitReqBO;

/**
 * 员工积分异议服务
 */
public interface ClubPointDisputeService {

    Long submitDispute(ClubPointDisputeSubmitReqBO reqBO);

    void acceptDispute(ClubPointDisputeAcceptReqBO reqBO);

    Long handleDispute(ClubPointDisputeHandleReqBO reqBO);

    void rejectDispute(ClubPointDisputeHandleReqBO reqBO);

}

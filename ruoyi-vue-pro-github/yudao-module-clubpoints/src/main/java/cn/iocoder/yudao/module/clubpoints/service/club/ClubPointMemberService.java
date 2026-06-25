package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberAddReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberExitReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberJoinReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointMemberRemoveReqBO;

/**
 * 俱乐部成员服务
 */
public interface ClubPointMemberService {

    Long joinClub(ClubPointMemberJoinReqBO reqBO);

    Long addMember(ClubPointMemberAddReqBO reqBO);

    void exitClub(ClubPointMemberExitReqBO reqBO);

    void removeMember(ClubPointMemberRemoveReqBO reqBO);

}

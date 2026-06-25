package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderAssignReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointLeaderRemoveReqBO;

/**
 * 俱乐部负责人服务
 */
public interface ClubPointLeaderService {

    Long assignLeader(ClubPointLeaderAssignReqBO reqBO);

    void removeLeader(ClubPointLeaderRemoveReqBO reqBO);

}

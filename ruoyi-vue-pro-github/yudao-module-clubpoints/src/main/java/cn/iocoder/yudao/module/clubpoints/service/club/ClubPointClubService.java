package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDeleteReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubDisableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubEnableReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubSaveReqBO;

/**
 * 俱乐部主数据服务
 */
public interface ClubPointClubService {

    Long createClub(ClubPointClubSaveReqBO reqBO);

    void updateClub(ClubPointClubSaveReqBO reqBO);

    void disableClub(ClubPointClubDisableReqBO reqBO);

    void enableClub(ClubPointClubEnableReqBO reqBO);

    void deleteClubPhysically(ClubPointClubDeleteReqBO reqBO);

}

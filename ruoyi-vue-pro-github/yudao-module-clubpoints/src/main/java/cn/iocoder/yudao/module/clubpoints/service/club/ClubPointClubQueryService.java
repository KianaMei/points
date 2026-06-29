package cn.iocoder.yudao.module.clubpoints.service.club;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubInfoBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubLeaderPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubMemberPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.club.bo.ClubPointClubPageReqBO;

import java.util.List;

/**
 * 俱乐部查询服务
 */
public interface ClubPointClubQueryService {

    List<ClubPointClubInfoBO> getAppMyClubList(Long loginUserId);

    PageResult<ClubPointClubInfoBO> getAppJoinableClubPage(Long loginUserId, ClubPointClubPageReqBO reqBO);

    PageResult<ClubPointClubMemberBO> getAppMemberPage(Long loginUserId, ClubPointClubMemberPageReqBO reqBO);

    List<ClubPointClubInfoBO> getLeaderManagedClubList(Long loginUserId);

    ClubPointClubInfoBO getLeaderClub(Long loginUserId, Long clubId);

    PageResult<ClubPointClubMemberBO> getLeaderMemberPage(Long loginUserId, ClubPointClubMemberPageReqBO reqBO);

    PageResult<ClubPointClubInfoBO> getAdminClubPage(ClubPointClubPageReqBO reqBO);

    ClubPointClubInfoBO getAdminClub(Long clubId);

    PageResult<ClubPointClubMemberBO> getAdminMemberPage(ClubPointClubMemberPageReqBO reqBO);

    PageResult<ClubPointClubLeaderBO> getAdminLeaderPage(ClubPointClubLeaderPageReqBO reqBO);

}

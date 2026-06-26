package cn.iocoder.yudao.module.clubpoints.service.dashboard;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.activity.ClubPointActivityDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.contribution.ClubPointContributionMaterialDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.dispute.ClubPointDisputeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.activity.ClubPointActivityRegistrationMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.contribution.ClubPointContributionMaterialMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.dispute.ClubPointDisputeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionApplicationMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointActivityStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAttendanceTargetTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointContributionMaterialStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointDisputeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointLeaderStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointMemberStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionApplicationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRegistrationStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.AdminDashboardSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.AppDashboardSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.ClubPointDashboardTodoItemBO;
import cn.iocoder.yudao.module.clubpoints.service.dashboard.bo.LeaderDashboardSummaryBO;
import cn.iocoder.yudao.module.system.service.notify.NotifyMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.enums.UserTypeEnum.ADMIN;

@Service
public class ClubPointDashboardServiceImpl implements ClubPointDashboardService {

    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private ClubLeaderMapper clubLeaderMapper;
    @Resource
    private ClubPointActivityMapper activityMapper;
    @Resource
    private ClubPointActivityRegistrationMapper registrationMapper;
    @Resource
    private ClubPointContributionMaterialMapper contributionMaterialMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper redemptionApplicationMapper;
    @Resource
    private ClubPointDisputeMapper disputeMapper;
    @Resource
    private NotifyMessageService notifyMessageService;

    @Override
    @Transactional(readOnly = true)
    public AppDashboardSummaryBO getAppSummary(Long userId) {
        ClubPointAccountDO account = accountMapper.selectByUserId(userId);
        int availablePoints = account == null ? 0 : value(account.getAvailablePoints());
        int frozenPoints = account == null ? 0 : value(account.getFrozenPoints());
        int totalEarnedPoints = account == null ? 0 : value(account.getAnnualEarnedPoints());
        int joinedClubCount = count(clubMemberMapper.selectCount(new LambdaQueryWrapperX<ClubMemberDO>()
                .eq(ClubMemberDO::getUserId, userId)
                .eq(ClubMemberDO::getStatus, ClubPointMemberStatusEnum.ACTIVE.getStatus())));
        int registeredActivityCount = count(registrationMapper.selectRegisteredActiveCountByUserId(userId,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus(), LocalDateTime.now()));
        int pendingRedemptionCount = count(redemptionApplicationMapper.selectCount(
                new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                        .eq(ClubPointRedemptionApplicationDO::getUserId, userId)
                        .eq(ClubPointRedemptionApplicationDO::getStatus,
                                ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus())));
        int unreadNotifyCount = count(notifyMessageService.getUnreadNotifyMessageCount(userId, ADMIN.getValue()));
        List<ClubPointDashboardTodoItemBO> todoItems = Arrays.asList(
                todo("app_activity_registered", "已报名未结束活动", registeredActivityCount,
                        "/clubpoints/app/activity", "{\"status\":\"registered\"}"),
                todo("app_redemption_pending", "待审核兑换", pendingRedemptionCount,
                        "/clubpoints/app/redemption", "{\"status\":1}"),
                todo("app_notify_unread", "未读通知", unreadNotifyCount,
                        "/clubpoints/app/notify", "{\"readStatus\":false}")
        );
        return new AppDashboardSummaryBO()
                .setAvailablePoints(availablePoints)
                .setFrozenPoints(frozenPoints)
                .setTotalEarnedPoints(totalEarnedPoints)
                .setJoinedClubCount(joinedClubCount)
                .setRegisteredActivityCount(registeredActivityCount)
                .setPendingRedemptionCount(pendingRedemptionCount)
                .setUnreadNotifyCount(unreadNotifyCount)
                .setTodoCount(sumTodo(todoItems))
                .setTodoItems(todoItems);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaderDashboardSummaryBO getLeaderSummary(Long userId) {
        List<Long> clubIds = clubLeaderMapper.selectActiveListByUserId(userId,
                        ClubPointLeaderStatusEnum.ACTIVE.getStatus()).stream()
                .map(ClubLeaderDO::getClubId)
                .collect(Collectors.toList());
        if (clubIds.isEmpty()) {
            return new LeaderDashboardSummaryBO()
                    .setManagedClubCount(0)
                    .setDraftActivityCount(0)
                    .setRejectedActivityCount(0)
                    .setAttendanceExceptionCount(0)
                    .setPendingContributionSubmitCount(0)
                    .setTodoCount(0)
                    .setTodoItems(Collections.emptyList());
        }
        int draftActivityCount = countActivityByClubIdsAndStatus(clubIds, ClubPointActivityStatusEnum.DRAFT.getStatus());
        int rejectedActivityCount = countActivityByClubIdsAndStatus(clubIds, ClubPointActivityStatusEnum.REJECTED.getStatus());
        int attendanceExceptionCount = count(registrationMapper.selectAttendanceExceptionCountByClubIds(clubIds,
                ClubPointRegistrationStatusEnum.REGISTERED.getStatus(),
                ClubPointActivityStatusEnum.ENDED.getStatus(),
                ClubPointActivityStatusEnum.SETTLED.getStatus(),
                ClubPointAttendanceTargetTypeEnum.CHECK_IN.getTargetType()));
        int pendingContributionSubmitCount = count(contributionMaterialMapper.selectCount(
                new LambdaQueryWrapperX<ClubPointContributionMaterialDO>()
                        .in(ClubPointContributionMaterialDO::getClubId, clubIds)
                        .in(ClubPointContributionMaterialDO::getStatus, Arrays.asList(
                                ClubPointContributionMaterialStatusEnum.DRAFT.getStatus(),
                                ClubPointContributionMaterialStatusEnum.WITHDRAWN.getStatus(),
                                ClubPointContributionMaterialStatusEnum.REJECTED.getStatus()))
                        .eq(ClubPointContributionMaterialDO::getDirectCreated, false)));
        List<ClubPointDashboardTodoItemBO> todoItems = Arrays.asList(
                todo("leader_activity_draft", "活动草稿", draftActivityCount,
                        "/clubpoints/leader/activity", "{\"status\":1}"),
                todo("leader_activity_rejected", "被驳回活动", rejectedActivityCount,
                        "/clubpoints/leader/activity", "{\"status\":3}"),
                todo("leader_attendance_exception", "签到异常", attendanceExceptionCount,
                        "/clubpoints/leader/attendance", "{\"exception\":true}"),
                todo("leader_contribution_to_submit", "待提交材料", pendingContributionSubmitCount,
                        "/clubpoints/leader/contribution", "{\"status\":\"editable\"}")
        );
        return new LeaderDashboardSummaryBO()
                .setManagedClubCount(clubIds.size())
                .setDraftActivityCount(draftActivityCount)
                .setRejectedActivityCount(rejectedActivityCount)
                .setAttendanceExceptionCount(attendanceExceptionCount)
                .setPendingContributionSubmitCount(pendingContributionSubmitCount)
                .setTodoCount(sumTodo(todoItems))
                .setTodoItems(todoItems);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryBO getAdminSummary() {
        int pendingActivityReviewCount = count(activityMapper.selectCount(new LambdaQueryWrapperX<ClubPointActivityDO>()
                .eq(ClubPointActivityDO::getStatus, ClubPointActivityStatusEnum.PENDING_REVIEW.getStatus())));
        int pendingContributionReviewCount = count(contributionMaterialMapper.selectCount(
                new LambdaQueryWrapperX<ClubPointContributionMaterialDO>()
                        .eq(ClubPointContributionMaterialDO::getStatus,
                                ClubPointContributionMaterialStatusEnum.PENDING_REVIEW.getStatus())));
        int pendingRedemptionReviewCount = count(redemptionApplicationMapper.selectCount(
                new LambdaQueryWrapperX<ClubPointRedemptionApplicationDO>()
                        .eq(ClubPointRedemptionApplicationDO::getStatus,
                                ClubPointRedemptionApplicationStatusEnum.PENDING_REVIEW.getStatus())));
        int pendingDisputeCount = count(disputeMapper.selectCount(new LambdaQueryWrapperX<ClubPointDisputeDO>()
                .eq(ClubPointDisputeDO::getStatus, ClubPointDisputeStatusEnum.PENDING.getStatus())));
        List<ClubPointDashboardTodoItemBO> todoItems = Arrays.asList(
                todo("admin_activity_review", "待审核活动", pendingActivityReviewCount,
                        "/clubpoints/admin/activity", "{\"status\":2}"),
                todo("admin_contribution_review", "待审核材料", pendingContributionReviewCount,
                        "/clubpoints/admin/contribution-review", "{\"status\":2}"),
                todo("admin_redemption_review", "待审核兑换", pendingRedemptionReviewCount,
                        "/clubpoints/admin/redemption-application", "{\"status\":1}"),
                todo("admin_dispute_handle", "待处理异议", pendingDisputeCount,
                        "/clubpoints/admin/dispute", "{\"status\":1}")
        );
        return new AdminDashboardSummaryBO()
                .setPendingActivityReviewCount(pendingActivityReviewCount)
                .setPendingContributionReviewCount(pendingContributionReviewCount)
                .setPendingRedemptionReviewCount(pendingRedemptionReviewCount)
                .setPendingDisputeCount(pendingDisputeCount)
                .setTodoCount(sumTodo(todoItems))
                .setTodoItems(todoItems);
    }

    private int countActivityByClubIdsAndStatus(List<Long> clubIds, Integer status) {
        return count(activityMapper.selectCount(new LambdaQueryWrapperX<ClubPointActivityDO>()
                .in(ClubPointActivityDO::getClubId, clubIds)
                .eq(ClubPointActivityDO::getStatus, status)));
    }

    private static ClubPointDashboardTodoItemBO todo(String code, String name, Integer count,
                                                     String path, String queryJson) {
        return new ClubPointDashboardTodoItemBO()
                .setCode(code)
                .setName(name)
                .setCount(count)
                .setPath(path)
                .setQueryJson(queryJson);
    }

    private static int sumTodo(List<ClubPointDashboardTodoItemBO> todoItems) {
        return todoItems.stream().mapToInt(item -> value(item.getCount())).sum();
    }

    private static int count(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private static int value(Integer value) {
        return value == null ? 0 : value;
    }

}

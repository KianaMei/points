package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_ELIGIBILITY_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_ELIGIBILITY_NOT_QUALIFIED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import({ClubPointRedemptionEligibilityServiceImpl.class, ClubScopeServiceImpl.class})
class ClubPointRedemptionEligibilityServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime GENERATED_TIME = LocalDateTime.of(2026, 8, 1, 9, 0);

    @Resource
    private ClubPointRedemptionEligibilityService redemptionEligibilityService;
    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;

    @Test
    void listBatchSnapshotsShouldRequireGlobalScopeAndSupportQualifiedFilter() {
        ClubPointRedemptionBatchDO batch = insertBatch();
        insertSnapshot(batch.getId(), 1002L, 2, true, 80);
        insertSnapshot(batch.getId(), 1001L, 1, true, 120);
        insertSnapshot(batch.getId(), 1003L, 3, false, 40);

        List<ClubPointRedemptionEligibilitySnapshotDO> all =
                redemptionEligibilityService.listBatchSnapshots(batch.getId(), null, true);
        assertEquals(3, all.size());
        assertEquals(1001L, all.get(0).getUserId());
        assertEquals(1002L, all.get(1).getUserId());
        assertEquals(1003L, all.get(2).getUserId());

        List<ClubPointRedemptionEligibilitySnapshotDO> qualified =
                redemptionEligibilityService.listBatchSnapshots(batch.getId(), true, true);
        assertEquals(2, qualified.size());
        assertEquals(1001L, qualified.get(0).getUserId());
        assertEquals(1002L, qualified.get(1).getUserId());

        assertServiceException(() -> redemptionEligibilityService.listBatchSnapshots(batch.getId(), null, false),
                CLUB_SCOPE_DENIED);
    }

    @Test
    void validateUserQualifiedForApplyShouldUseSnapshotOnly() {
        ClubPointRedemptionBatchDO batch = insertBatch();
        ClubPointRedemptionEligibilitySnapshotDO snapshot = insertSnapshot(batch.getId(), 1001L, 1, true, 120);

        ClubPointRedemptionEligibilitySnapshotDO result =
                redemptionEligibilityService.validateUserQualifiedForApply(batch.getId(), 1001L);

        assertEquals(snapshot.getId(), result.getId());
        assertEquals(120, result.getAvailablePointsSnapshot());
        assertEquals(1, result.getRankNo());
    }

    @Test
    void validateUserQualifiedForApplyShouldRejectMissingOrUnqualifiedSnapshot() {
        ClubPointRedemptionBatchDO batch = insertBatch();
        insertSnapshot(batch.getId(), 1002L, 2, false, 40);

        assertServiceException(() -> redemptionEligibilityService.validateUserQualifiedForApply(batch.getId(), 1001L),
                CLUB_REDEMPTION_ELIGIBILITY_NOT_EXISTS);
        assertServiceException(() -> redemptionEligibilityService.validateUserQualifiedForApply(batch.getId(), 1002L),
                CLUB_REDEMPTION_ELIGIBILITY_NOT_QUALIFIED);
    }

    private ClubPointRedemptionBatchDO insertBatch() {
        ClubPointRedemptionBatchDO batch = new ClubPointRedemptionBatchDO()
                .setYear(2026)
                .setName("2026 夏季兑换批次")
                .setStatus(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus())
                .setOpenTime(GENERATED_TIME)
                .setCloseTime(GENERATED_TIME.plusDays(10))
                .setDescription("批次说明")
                .setMinAvailablePoints(50)
                .setQualifiedCount(180)
                .setIncludeTieAtCutoff(true)
                .setQualificationRuleJson("{\"min\":50}")
                .setSnapshotGenerated(true)
                .setSnapshotGeneratedTime(GENERATED_TIME)
                .setRuleVersionId(6001L)
                .setRuleSnapshotJson("{\"rule\":\"redemption\"}");
        batchMapper.insert(batch);
        return batch;
    }

    private ClubPointRedemptionEligibilitySnapshotDO insertSnapshot(Long batchId, Long userId, Integer rankNo,
                                                                    Boolean qualified, Integer availablePoints) {
        ClubPointRedemptionEligibilitySnapshotDO snapshot = new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(batchId)
                .setUserId(userId)
                .setUserNameSnapshot("员工" + userId)
                .setDeptNameSnapshot("运营部")
                .setNetPointsSnapshot(availablePoints)
                .setFrozenPointsSnapshot(0)
                .setAvailablePointsSnapshot(availablePoints)
                .setAnnualEarnedPointsSnapshot(availablePoints)
                .setRankNo(rankNo)
                .setQualified(qualified)
                .setQualificationReason(Boolean.TRUE.equals(qualified) ? "满足资格规则" : "低于最低可用积分")
                .setTieAtCutoff(false)
                .setGeneratedTime(GENERATED_TIME);
        eligibilitySnapshotMapper.insert(snapshot);
        return snapshot;
    }

}

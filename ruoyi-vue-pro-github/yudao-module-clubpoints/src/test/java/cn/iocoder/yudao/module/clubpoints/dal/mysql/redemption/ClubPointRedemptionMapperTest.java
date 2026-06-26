package cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionApplicationDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionGiftDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionReviewRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointStockLockDO;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClubPointRedemptionMapperTest extends BaseDbUnitTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 8, 1, 10, 0);

    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionGiftMapper giftMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointRedemptionApplicationMapper applicationMapper;
    @Resource
    private ClubPointStockLockMapper stockLockMapper;
    @Resource
    private ClubPointRedemptionReviewRecordMapper reviewRecordMapper;

    @Test
    void redemptionMappersShouldPersistBatchGiftEligibilityApplicationStockLockAndReviewRecord() {
        ClubPointRedemptionBatchDO batch = buildBatch();
        batchMapper.insert(batch);
        ClubPointRedemptionBatchDO savedBatch = batchMapper.selectById(batch.getId());
        assertNotNull(savedBatch);
        assertEquals(2026, savedBatch.getYear());
        assertEquals("2026 夏季兑换批次", savedBatch.getName());
        assertEquals(1, savedBatch.getStatus());
        assertEquals(BASE_TIME, savedBatch.getOpenTime());
        assertEquals(BASE_TIME.plusDays(10), savedBatch.getCloseTime());
        assertEquals("批次说明", savedBatch.getDescription());
        assertEquals(50, savedBatch.getMinAvailablePoints());
        assertEquals(180, savedBatch.getQualifiedCount());
        assertTrue(savedBatch.getIncludeTieAtCutoff());
        assertEquals("{\"min\":50}", savedBatch.getQualificationRuleJson());
        assertTrue(savedBatch.getSnapshotGenerated());
        assertEquals(BASE_TIME.minusHours(1), savedBatch.getSnapshotGeneratedTime());
        assertEquals(6001L, savedBatch.getRuleVersionId());
        assertEquals("{\"version\":\"2026\"}", savedBatch.getRuleSnapshotJson());

        ClubPointRedemptionGiftDO gift = buildGift(savedBatch.getId());
        giftMapper.insert(gift);
        List<ClubPointRedemptionGiftDO> gifts = giftMapper.selectListByBatchId(savedBatch.getId());
        assertEquals(1, gifts.size());
        ClubPointRedemptionGiftDO savedGift = gifts.get(0);
        assertEquals(savedBatch.getId(), savedGift.getBatchId());
        assertEquals("运动水杯", savedGift.getName());
        assertEquals("礼品说明", savedGift.getDescription());
        assertEquals(60, savedGift.getPointsCost());
        assertEquals(50, savedGift.getTierMinPoints());
        assertEquals(100, savedGift.getTierMaxPoints());
        assertEquals(1999L, savedGift.getReferenceAmountCent());
        assertEquals(10, savedGift.getStockTotal());
        assertEquals(2, savedGift.getStockLocked());
        assertEquals(3, savedGift.getStockUsed());
        assertEquals(1, savedGift.getStatus());
        assertEquals(3001L, savedGift.getImageFileId());
        assertEquals(5, savedGift.getSort());
        assertEquals("{\"name\":\"运动水杯\"}", savedGift.getGiftSnapshotJson());

        ClubPointRedemptionEligibilitySnapshotDO eligibility = buildEligibility(savedBatch.getId());
        eligibilitySnapshotMapper.insert(eligibility);
        ClubPointRedemptionEligibilitySnapshotDO savedEligibility =
                eligibilitySnapshotMapper.selectByBatchIdAndUserId(savedBatch.getId(), 8101L);
        assertNotNull(savedEligibility);
        assertEquals("员工8101", savedEligibility.getUserNameSnapshot());
        assertEquals("Operations", savedEligibility.getDeptNameSnapshot());
        assertEquals(150, savedEligibility.getNetPointsSnapshot());
        assertEquals(20, savedEligibility.getFrozenPointsSnapshot());
        assertEquals(130, savedEligibility.getAvailablePointsSnapshot());
        assertEquals(300, savedEligibility.getAnnualEarnedPointsSnapshot());
        assertEquals(12, savedEligibility.getRankNo());
        assertTrue(savedEligibility.getQualified());
        assertEquals("满足最低积分和排名", savedEligibility.getQualificationReason());
        assertTrue(savedEligibility.getTieAtCutoff());
        assertEquals(BASE_TIME.minusMinutes(30), savedEligibility.getGeneratedTime());

        ClubPointRedemptionApplicationDO application = buildApplication(savedBatch.getId(), savedGift.getId(),
                savedEligibility.getId());
        applicationMapper.insert(application);
        ClubPointRedemptionApplicationDO savedApplication =
                applicationMapper.selectByIdempotencyKey("REDEMPTION_APPLY:2026:1:8101:REQ-001");
        assertNotNull(savedApplication);
        assertEquals("APP-2026-001", savedApplication.getApplicationNo());
        assertEquals("REQ-001", savedApplication.getRequestNo());
        assertEquals(savedBatch.getId(), savedApplication.getBatchId());
        assertEquals(savedGift.getId(), savedApplication.getGiftId());
        assertEquals(savedEligibility.getId(), savedApplication.getEligibilitySnapshotId());
        assertEquals(8101L, savedApplication.getUserId());
        assertEquals(1, savedApplication.getStatus());
        assertEquals(60, savedApplication.getPointsCost());
        assertEquals(1, savedApplication.getQuantity());
        assertEquals(7001L, savedApplication.getFreezeId());
        assertEquals(8001L, savedApplication.getStockLockId());
        assertEquals(9001L, savedApplication.getDeductTransactionId());
        assertEquals(12, savedApplication.getQualificationRankSnapshot());
        assertEquals(150, savedApplication.getBeforeNetPoints());
        assertEquals(20, savedApplication.getBeforeFrozenPoints());
        assertEquals(130, savedApplication.getBeforeAvailablePoints());
        assertEquals(90, savedApplication.getAfterNetPoints());
        assertEquals(0, savedApplication.getAfterFrozenPoints());
        assertEquals(90, savedApplication.getAfterAvailablePoints());
        assertEquals("{\"batch\":\"2026\"}", savedApplication.getBatchSnapshotJson());
        assertEquals("{\"gift\":\"cup\"}", savedApplication.getGiftSnapshotJson());
        assertEquals(BASE_TIME.plusMinutes(1), savedApplication.getApplyTime());
        assertEquals(BASE_TIME.plusMinutes(2), savedApplication.getCancelTime());
        assertEquals("员工取消", savedApplication.getCancelReason());
        assertEquals(9101L, savedApplication.getReviewerUserId());
        assertEquals(BASE_TIME.plusHours(1), savedApplication.getReviewTime());
        assertEquals("审核通过", savedApplication.getReviewReason());
        assertEquals(BASE_TIME.plusHours(2), savedApplication.getDirectIssueTime());
        assertEquals(savedApplication.getId(),
                applicationMapper.selectByApplicationNo("APP-2026-001").getId());

        ClubPointStockLockDO stockLock = buildStockLock(savedGift.getId(), savedApplication.getId());
        stockLockMapper.insert(stockLock);
        ClubPointStockLockDO savedStockLock = stockLockMapper.selectByApplicationId(savedApplication.getId());
        assertNotNull(savedStockLock);
        assertEquals(savedGift.getId(), savedStockLock.getGiftId());
        assertEquals(savedApplication.getId(), savedStockLock.getApplicationId());
        assertEquals(8101L, savedStockLock.getUserId());
        assertEquals(1, savedStockLock.getQuantity());
        assertEquals(1, savedStockLock.getStatus());
        assertEquals(BASE_TIME.plusMinutes(1), savedStockLock.getLockedTime());
        assertEquals(BASE_TIME.plusHours(1), savedStockLock.getUsedTime());
        assertEquals(BASE_TIME.plusHours(2), savedStockLock.getReleasedTime());
        assertEquals("审核拒绝释放", savedStockLock.getReleaseReason());
        assertEquals("STOCK_LOCK:APP-2026-001", savedStockLock.getIdempotencyKey());
        assertEquals(savedStockLock.getId(),
                stockLockMapper.selectByIdempotencyKey("STOCK_LOCK:APP-2026-001").getId());

        ClubPointRedemptionReviewRecordDO reviewRecord = buildReviewRecord(savedApplication.getId());
        reviewRecordMapper.insert(reviewRecord);
        List<ClubPointRedemptionReviewRecordDO> reviewRecords =
                reviewRecordMapper.selectListByApplicationId(savedApplication.getId());
        assertEquals(1, reviewRecords.size());
        ClubPointRedemptionReviewRecordDO savedReviewRecord = reviewRecords.get(0);
        assertEquals(savedApplication.getId(), savedReviewRecord.getApplicationId());
        assertEquals(9101L, savedReviewRecord.getReviewerUserId());
        assertEquals(1, savedReviewRecord.getResult());
        assertEquals("审核通过", savedReviewRecord.getReason());
        assertEquals(BASE_TIME.plusHours(1), savedReviewRecord.getReviewTime());
        assertEquals("{\"status\":1}", savedReviewRecord.getApplicationSnapshotJson());
        assertEquals("{\"freeze\":\"converted\"}", savedReviewRecord.getFreezeSnapshotJson());
        assertEquals("{\"stock\":\"used\"}", savedReviewRecord.getStockSnapshotJson());
        assertEquals(9901L, savedReviewRecord.getAuditLogId());
    }

    private static ClubPointRedemptionBatchDO buildBatch() {
        return new ClubPointRedemptionBatchDO()
                .setYear(2026)
                .setName("2026 夏季兑换批次")
                .setStatus(1)
                .setOpenTime(BASE_TIME)
                .setCloseTime(BASE_TIME.plusDays(10))
                .setDescription("批次说明")
                .setMinAvailablePoints(50)
                .setQualifiedCount(180)
                .setIncludeTieAtCutoff(true)
                .setQualificationRuleJson("{\"min\":50}")
                .setSnapshotGenerated(true)
                .setSnapshotGeneratedTime(BASE_TIME.minusHours(1))
                .setRuleVersionId(6001L)
                .setRuleSnapshotJson("{\"version\":\"2026\"}");
    }

    private static ClubPointRedemptionGiftDO buildGift(Long batchId) {
        return new ClubPointRedemptionGiftDO()
                .setBatchId(batchId)
                .setName("运动水杯")
                .setDescription("礼品说明")
                .setPointsCost(60)
                .setTierMinPoints(50)
                .setTierMaxPoints(100)
                .setReferenceAmountCent(1999L)
                .setStockTotal(10)
                .setStockLocked(2)
                .setStockUsed(3)
                .setStatus(1)
                .setImageFileId(3001L)
                .setSort(5)
                .setGiftSnapshotJson("{\"name\":\"运动水杯\"}");
    }

    private static ClubPointRedemptionEligibilitySnapshotDO buildEligibility(Long batchId) {
        return new ClubPointRedemptionEligibilitySnapshotDO()
                .setBatchId(batchId)
                .setUserId(8101L)
                .setUserNameSnapshot("员工8101")
                .setDeptNameSnapshot("Operations")
                .setNetPointsSnapshot(150)
                .setFrozenPointsSnapshot(20)
                .setAvailablePointsSnapshot(130)
                .setAnnualEarnedPointsSnapshot(300)
                .setRankNo(12)
                .setQualified(true)
                .setQualificationReason("满足最低积分和排名")
                .setTieAtCutoff(true)
                .setGeneratedTime(BASE_TIME.minusMinutes(30));
    }

    private static ClubPointRedemptionApplicationDO buildApplication(Long batchId, Long giftId,
                                                                     Long eligibilitySnapshotId) {
        return new ClubPointRedemptionApplicationDO()
                .setApplicationNo("APP-2026-001")
                .setRequestNo("REQ-001")
                .setBatchId(batchId)
                .setGiftId(giftId)
                .setEligibilitySnapshotId(eligibilitySnapshotId)
                .setUserId(8101L)
                .setStatus(1)
                .setPointsCost(60)
                .setQuantity(1)
                .setFreezeId(7001L)
                .setStockLockId(8001L)
                .setDeductTransactionId(9001L)
                .setQualificationRankSnapshot(12)
                .setBeforeNetPoints(150)
                .setBeforeFrozenPoints(20)
                .setBeforeAvailablePoints(130)
                .setAfterNetPoints(90)
                .setAfterFrozenPoints(0)
                .setAfterAvailablePoints(90)
                .setBatchSnapshotJson("{\"batch\":\"2026\"}")
                .setGiftSnapshotJson("{\"gift\":\"cup\"}")
                .setApplyTime(BASE_TIME.plusMinutes(1))
                .setCancelTime(BASE_TIME.plusMinutes(2))
                .setCancelReason("员工取消")
                .setReviewerUserId(9101L)
                .setReviewTime(BASE_TIME.plusHours(1))
                .setReviewReason("审核通过")
                .setDirectIssueTime(BASE_TIME.plusHours(2))
                .setIdempotencyKey("REDEMPTION_APPLY:2026:1:8101:REQ-001");
    }

    private static ClubPointStockLockDO buildStockLock(Long giftId, Long applicationId) {
        return new ClubPointStockLockDO()
                .setGiftId(giftId)
                .setApplicationId(applicationId)
                .setUserId(8101L)
                .setQuantity(1)
                .setStatus(1)
                .setLockedTime(BASE_TIME.plusMinutes(1))
                .setUsedTime(BASE_TIME.plusHours(1))
                .setReleasedTime(BASE_TIME.plusHours(2))
                .setReleaseReason("审核拒绝释放")
                .setIdempotencyKey("STOCK_LOCK:APP-2026-001");
    }

    private static ClubPointRedemptionReviewRecordDO buildReviewRecord(Long applicationId) {
        return new ClubPointRedemptionReviewRecordDO()
                .setApplicationId(applicationId)
                .setReviewerUserId(9101L)
                .setResult(1)
                .setReason("审核通过")
                .setReviewTime(BASE_TIME.plusHours(1))
                .setApplicationSnapshotJson("{\"status\":1}")
                .setFreezeSnapshotJson("{\"freeze\":\"converted\"}")
                .setStockSnapshotJson("{\"stock\":\"used\"}")
                .setAuditLogId(9901L);
    }

}

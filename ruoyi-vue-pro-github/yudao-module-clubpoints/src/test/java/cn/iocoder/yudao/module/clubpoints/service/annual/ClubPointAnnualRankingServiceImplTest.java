package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualRankingGenerateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({
        ClubPointAnnualRankingServiceImpl.class,
        ClubPointRuleServiceImpl.class
})
class ClubPointAnnualRankingServiceImplTest extends BaseDbUnitTest {

    private static final Integer YEAR = 2026;
    private static final LocalDateTime GENERATED_TIME = LocalDateTime.of(2027, 1, 1, 3, 0);

    @Resource
    private ClubPointAnnualRankingService annualRankingService;
    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;

    @Test
    void generateRankingShouldAggregatePositiveIssuedPointsDeductReversalAndIgnoreRedemptionAndAnnualClearing() {
        insertPublishedRuleVersion("M10-5-RANKING-RULE");
        ClubPointTransactionDO clubAActivity = insertIncreaseTransaction(1L, "CLUB-A", "Alpha Club",
                100, ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), "A-ACT-100");
        insertIncreaseTransaction(1L, "CLUB-A", "Alpha Club",
                40, ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory(), "A-CON-40");
        insertReverseTransaction(clubAActivity, 20, "A-REV-20");
        insertDecreaseTransaction(1L, "CLUB-A", "Alpha Club",
                200, ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory(),
                ClubPointTransactionSourceTypeEnum.REDEMPTION.getType(), "A-RED-200");
        insertDecreaseTransaction(1L, "CLUB-A", "Alpha Club",
                50, ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory(),
                ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING.getType(), "A-AC-50");
        insertIncreaseTransaction(2L, "CLUB-B", "Beta Club",
                90, ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), "B-ACT-90");
        insertIncreaseTransaction(2L, "CLUB-B", "Beta Club",
                20, ClubPointCategoryEnum.SPECIAL_REWARD.getCategory(), "B-REWARD-20");

        annualRankingService.generateRanking(buildReq());

        List<ClubPointAnnualRankingRecordDO> records = rankingRecordMapper.selectListByYear(YEAR);
        assertEquals(2, records.size());
        ClubPointAnnualRankingRecordDO clubA = rankingRecordMapper.selectByYearAndClubCode(YEAR, "CLUB-A");
        assertNotNull(clubA);
        assertEquals(1L, clubA.getClubId());
        assertEquals("Alpha Club", clubA.getClubNameSnapshot());
        assertEquals(100, clubA.getActivityPoints());
        assertEquals(40, clubA.getContributionPoints());
        assertEquals(0, clubA.getRewardPoints());
        assertEquals(20, clubA.getReversedPoints());
        assertEquals(120, clubA.getTotalIssuedPoints());
        assertEquals(1, clubA.getRankNo());
        assertEquals(200_000L, clubA.getIncentiveAmountCent());
        assertEquals(1, clubA.getConfirmStatus());
        assertEquals(GENERATED_TIME, clubA.getGeneratedTime());
        assertTrue(clubA.getSnapshotJson().contains("\"rankingFormula\""));
        assertTrue(clubA.getSnapshotJson().contains("\"positiveTransactionIds\""));

        ClubPointAnnualRankingRecordDO clubB = rankingRecordMapper.selectByYearAndClubCode(YEAR, "CLUB-B");
        assertNotNull(clubB);
        assertEquals(90, clubB.getActivityPoints());
        assertEquals(20, clubB.getRewardPoints());
        assertEquals(110, clubB.getTotalIssuedPoints());
        assertEquals(2, clubB.getRankNo());
    }

    @Test
    void generateRankingShouldRegenerateExistingYearWithoutDuplicatingRecords() {
        insertPublishedRuleVersion("M10-5-RERANK-RULE");
        insertIncreaseTransaction(1L, "CLUB-A", "Alpha Club",
                50, ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(), "A-FIRST-50");
        annualRankingService.generateRanking(buildReq());

        insertIncreaseTransaction(1L, "CLUB-A", "Alpha Club",
                40, ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory(), "A-SECOND-40");
        insertIncreaseTransaction(2L, "CLUB-B", "Beta Club",
                120, ClubPointCategoryEnum.SPECIAL_REWARD.getCategory(), "B-FIRST-120");
        annualRankingService.generateRanking(buildReq());

        assertEquals(2L, rankingRecordMapper.selectCount());
        ClubPointAnnualRankingRecordDO clubB = rankingRecordMapper.selectByYearAndClubCode(YEAR, "CLUB-B");
        assertEquals(1, clubB.getRankNo());
        assertEquals(120, clubB.getTotalIssuedPoints());
        ClubPointAnnualRankingRecordDO clubA = rankingRecordMapper.selectByYearAndClubCode(YEAR, "CLUB-A");
        assertEquals(2, clubA.getRankNo());
        assertEquals(90, clubA.getTotalIssuedPoints());
    }

    private static ClubPointAnnualRankingGenerateReqBO buildReq() {
        return new ClubPointAnnualRankingGenerateReqBO()
                .setYear(YEAR)
                .setGeneratedTime(GENERATED_TIME);
    }

    private ClubPointRuleVersionDO insertPublishedRuleVersion(String versionNo) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo(versionNo)
                .setName(versionNo)
                .setStatus(2)
                .setPublicityTime(LocalDateTime.of(2026, 12, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 12, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2026, 12, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);
        return version;
    }

    private ClubPointTransactionDO insertIncreaseTransaction(Long clubId, String clubCode, String clubName,
                                                             Integer points, Integer pointCategory,
                                                             String transactionNo) {
        ClubPointTransactionDO transaction = buildTransaction(clubId, clubCode, clubName, points,
                pointCategory, ClubPointTransactionDirectionEnum.INCREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.ACTIVITY_SETTLEMENT.getType(), transactionNo);
        transactionMapper.insert(transaction);
        return transaction;
    }

    private ClubPointTransactionDO insertDecreaseTransaction(Long clubId, String clubCode, String clubName,
                                                             Integer points, Integer pointCategory,
                                                             Integer sourceType, String transactionNo) {
        ClubPointTransactionDO transaction = buildTransaction(clubId, clubCode, clubName, points,
                pointCategory, ClubPointTransactionDirectionEnum.DECREASE.getDirection(), sourceType, transactionNo);
        transactionMapper.insert(transaction);
        return transaction;
    }

    private ClubPointTransactionDO insertReverseTransaction(ClubPointTransactionDO source, Integer points,
                                                            String transactionNo) {
        ClubPointTransactionDO transaction = buildTransaction(source.getIssuingClubId(),
                source.getIssuingClubCodeSnapshot(), source.getIssuingClubNameSnapshot(), points,
                ClubPointCategoryEnum.REVERSAL.getCategory(),
                ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
                ClubPointTransactionSourceTypeEnum.REVERSAL.getType(), transactionNo)
                .setStatus(ClubPointTransactionStatusEnum.REVERSAL.getStatus())
                .setReverseOfTransactionId(source.getId());
        transactionMapper.insert(transaction);
        return transaction;
    }

    private static ClubPointTransactionDO buildTransaction(Long clubId, String clubCode, String clubName,
                                                           Integer points, Integer pointCategory,
                                                           Integer direction, Integer sourceType,
                                                           String transactionNo) {
        return new ClubPointTransactionDO()
                .setTransactionNo(transactionNo)
                .setUserId(1000L)
                .setUserNameSnapshot("Ranking User")
                .setDirection(direction)
                .setPoints(points)
                .setPointCategory(pointCategory)
                .setPointTypeCode("RANKING_TEST")
                .setStatus(ClubPointTransactionStatusEnum.VALID.getStatus())
                .setSourceType(sourceType)
                .setSourceId(1L)
                .setSourceTitleSnapshot("ranking source")
                .setIssuingClubId(clubId)
                .setIssuingClubCodeSnapshot(clubCode)
                .setIssuingClubNameSnapshot(clubName)
                .setRuleVersionId(1L)
                .setRuleItemCodeSnapshot("RANKING_TEST")
                .setRuleSnapshotJson("{}")
                .setMaterialSummary("ranking")
                .setReason("ranking")
                .setOccurredAt(LocalDateTime.of(YEAR, 6, 1, 10, 0))
                .setBusinessYear(YEAR)
                .setBusinessMonth(YEAR * 100 + 6)
                .setIdempotencyKey("RANKING:" + transactionNo);
    }

}

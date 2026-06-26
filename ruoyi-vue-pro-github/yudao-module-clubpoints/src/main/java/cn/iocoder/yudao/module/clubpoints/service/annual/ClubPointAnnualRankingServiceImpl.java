package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleVersionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualRankingGenerateReqBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 俱乐部年度排名服务实现
 */
@Service
public class ClubPointAnnualRankingServiceImpl implements ClubPointAnnualRankingService {

    private static final Integer CONFIRM_STATUS_PENDING = 1;
    private static final long TOP_THREE_INCENTIVE_AMOUNT_CENT = 200_000L;
    private static final long TOP_SIX_INCENTIVE_AMOUNT_CENT = 100_000L;
    private static final Set<Integer> POSITIVE_RANKING_CATEGORIES = new HashSet<>(Arrays.asList(
            ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory(),
            ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory(),
            ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory(),
            ClubPointCategoryEnum.SPECIAL_REWARD.getCategory()));
    private static final Set<Integer> POSITIVE_SOURCE_STATUSES = new HashSet<>(Arrays.asList(
            ClubPointTransactionStatusEnum.VALID.getStatus(),
            ClubPointTransactionStatusEnum.REVERSED.getStatus()));

    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateRanking(ClubPointAnnualRankingGenerateReqBO reqBO) {
        validateReq(reqBO);
        LocalDateTime generatedTime = reqBO.getGeneratedTime() != null ? reqBO.getGeneratedTime() : LocalDateTime.now();
        ClubPointRuleVersionDO ruleVersion = getEffectiveRuleVersion(generatedTime);

        List<ClubPointTransactionDO> positiveTransactions = filterAnnualPositiveTransactions(
                transactionMapper.selectListForAnnualRanking(reqBO.getYear()), reqBO.getYear());
        Map<Long, ClubPointTransactionDO> positiveById = toPositiveById(positiveTransactions);
        Map<String, RankingAccumulator> accumulators = aggregatePositiveTransactions(positiveTransactions);
        applyReversalTransactions(accumulators, positiveById);

        List<ClubPointAnnualRankingRecordDO> records = buildRankingRecords(reqBO.getYear(), generatedTime,
                ruleVersion, accumulators.values());
        rankingRecordMapper.deletePhysicallyByYear(reqBO.getYear());
        for (ClubPointAnnualRankingRecordDO record : records) {
            rankingRecordMapper.insert(record);
        }
    }

    private List<ClubPointTransactionDO> filterAnnualPositiveTransactions(List<ClubPointTransactionDO> transactions,
                                                                         Integer year) {
        List<ClubPointTransactionDO> result = new ArrayList<>();
        for (ClubPointTransactionDO transaction : transactions) {
            if (isAnnualPositiveTransaction(transaction, year)) {
                result.add(transaction);
            }
        }
        return result;
    }

    private Map<Long, ClubPointTransactionDO> toPositiveById(List<ClubPointTransactionDO> transactions) {
        Map<Long, ClubPointTransactionDO> result = new HashMap<>();
        for (ClubPointTransactionDO transaction : transactions) {
            if (transaction.getId() != null) {
                result.put(transaction.getId(), transaction);
            }
        }
        return result;
    }

    private Map<String, RankingAccumulator> aggregatePositiveTransactions(List<ClubPointTransactionDO> transactions) {
        Map<String, RankingAccumulator> accumulators = new LinkedHashMap<>();
        for (ClubPointTransactionDO transaction : transactions) {
            RankingAccumulator accumulator = accumulators.get(transaction.getIssuingClubCodeSnapshot());
            if (accumulator == null) {
                accumulator = new RankingAccumulator(transaction);
                accumulators.put(transaction.getIssuingClubCodeSnapshot(), accumulator);
            }
            accumulator.addPositive(transaction);
        }
        return accumulators;
    }

    private void applyReversalTransactions(Map<String, RankingAccumulator> accumulators,
                                           Map<Long, ClubPointTransactionDO> positiveById) {
        if (positiveById.isEmpty()) {
            return;
        }
        List<ClubPointTransactionDO> reversals = transactionMapper.selectListByReverseOfTransactionIds(positiveById.keySet());
        for (ClubPointTransactionDO reversal : reversals) {
            if (!isRankingReversal(reversal)) {
                continue;
            }
            ClubPointTransactionDO source = positiveById.get(reversal.getReverseOfTransactionId());
            if (source == null) {
                continue;
            }
            RankingAccumulator accumulator = accumulators.get(source.getIssuingClubCodeSnapshot());
            if (accumulator != null) {
                accumulator.addReversal(reversal);
            }
        }
    }

    private List<ClubPointAnnualRankingRecordDO> buildRankingRecords(Integer year, LocalDateTime generatedTime,
                                                                     ClubPointRuleVersionDO ruleVersion,
                                                                     Collection<RankingAccumulator> accumulators) {
        List<RankingAccumulator> ranking = new ArrayList<>(accumulators);
        Collections.sort(ranking, new Comparator<RankingAccumulator>() {
            @Override
            public int compare(RankingAccumulator left, RankingAccumulator right) {
                int pointCompare = right.totalIssuedPoints().compareTo(left.totalIssuedPoints());
                if (pointCompare != 0) {
                    return pointCompare;
                }
                return left.clubCodeSnapshot.compareTo(right.clubCodeSnapshot);
            }
        });

        List<ClubPointAnnualRankingRecordDO> records = new ArrayList<>();
        int rankNo = 1;
        for (RankingAccumulator accumulator : ranking) {
            records.add(toRecord(year, generatedTime, ruleVersion, accumulator, rankNo));
            rankNo++;
        }
        return records;
    }

    private ClubPointAnnualRankingRecordDO toRecord(Integer year, LocalDateTime generatedTime,
                                                    ClubPointRuleVersionDO ruleVersion,
                                                    RankingAccumulator accumulator, int rankNo) {
        return new ClubPointAnnualRankingRecordDO()
                .setYear(year)
                .setClubId(accumulator.clubId)
                .setClubCodeSnapshot(accumulator.clubCodeSnapshot)
                .setClubNameSnapshot(accumulator.clubNameSnapshot)
                .setActivityPoints(accumulator.activityPoints)
                .setContributionPoints(accumulator.contributionPoints)
                .setRewardPoints(accumulator.rewardPoints)
                .setReversedPoints(accumulator.reversedPoints)
                .setTotalIssuedPoints(accumulator.totalIssuedPoints())
                .setRankNo(rankNo)
                .setIncentiveAmountCent(calculateIncentiveAmount(rankNo))
                .setConfirmStatus(CONFIRM_STATUS_PENDING)
                .setGeneratedTime(generatedTime)
                .setSnapshotJson(buildSnapshotJson(year, ruleVersion, accumulator));
    }

    private String buildSnapshotJson(Integer year, ClubPointRuleVersionDO ruleVersion,
                                     RankingAccumulator accumulator) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("year", year);
        snapshot.put("rankingFormula", "positive issued points by issuing club minus reversal transactions of those positives");
        snapshot.put("ruleVersionId", ruleVersion.getId());
        snapshot.put("ruleVersionNo", ruleVersion.getVersionNo());
        snapshot.put("clubCodeSnapshot", accumulator.clubCodeSnapshot);
        snapshot.put("clubNameSnapshot", accumulator.clubNameSnapshot);
        snapshot.put("activityPoints", accumulator.activityPoints);
        snapshot.put("contributionPoints", accumulator.contributionPoints);
        snapshot.put("rewardPoints", accumulator.rewardPoints);
        snapshot.put("reversedPoints", accumulator.reversedPoints);
        snapshot.put("totalIssuedPoints", accumulator.totalIssuedPoints());
        snapshot.put("positiveTransactionIds", accumulator.positiveTransactionIds);
        snapshot.put("reversalTransactionIds", accumulator.reversalTransactionIds);
        return JsonUtils.toJsonString(snapshot);
    }

    private ClubPointRuleVersionDO getEffectiveRuleVersion(LocalDateTime generatedTime) {
        ClubPointRuleVersionDO ruleVersion = ruleVersionMapper.selectCurrentPublished(
                ClubPointRuleVersionStatusEnum.PUBLISHED.getStatus(), generatedTime);
        if (ruleVersion == null) {
            throw new IllegalStateException("effective rule version is required for annual ranking");
        }
        return ruleVersion;
    }

    private static Long calculateIncentiveAmount(int rankNo) {
        if (rankNo >= 1 && rankNo <= 3) {
            return TOP_THREE_INCENTIVE_AMOUNT_CENT;
        }
        if (rankNo >= 4 && rankNo <= 6) {
            return TOP_SIX_INCENTIVE_AMOUNT_CENT;
        }
        return 0L;
    }

    private static boolean isAnnualPositiveTransaction(ClubPointTransactionDO transaction, Integer year) {
        return transaction != null
                && year.equals(transaction.getBusinessYear())
                && ClubPointTransactionDirectionEnum.INCREASE.getDirection().equals(transaction.getDirection())
                && POSITIVE_SOURCE_STATUSES.contains(transaction.getStatus())
                && POSITIVE_RANKING_CATEGORIES.contains(transaction.getPointCategory())
                && transaction.getPoints() != null && transaction.getPoints() > 0
                && StringUtils.hasText(transaction.getIssuingClubCodeSnapshot());
    }

    private static boolean isRankingReversal(ClubPointTransactionDO transaction) {
        return transaction != null
                && ClubPointTransactionStatusEnum.REVERSAL.getStatus().equals(transaction.getStatus())
                && ClubPointTransactionSourceTypeEnum.REVERSAL.getType().equals(transaction.getSourceType())
                && ClubPointTransactionDirectionEnum.DECREASE.getDirection().equals(transaction.getDirection())
                && transaction.getReverseOfTransactionId() != null
                && transaction.getPoints() != null && transaction.getPoints() > 0;
    }

    private static void validateReq(ClubPointAnnualRankingGenerateReqBO reqBO) {
        if (reqBO == null || reqBO.getYear() == null) {
            throw new IllegalArgumentException("year is required");
        }
    }

    private static final class RankingAccumulator {

        private final String clubCodeSnapshot;
        private Long clubId;
        private String clubNameSnapshot;
        private int activityPoints;
        private int contributionPoints;
        private int rewardPoints;
        private int reversedPoints;
        private final List<Long> positiveTransactionIds = new ArrayList<>();
        private final List<Long> reversalTransactionIds = new ArrayList<>();

        private RankingAccumulator(ClubPointTransactionDO transaction) {
            this.clubCodeSnapshot = transaction.getIssuingClubCodeSnapshot();
            this.clubId = transaction.getIssuingClubId();
            this.clubNameSnapshot = transaction.getIssuingClubNameSnapshot();
        }

        private void addPositive(ClubPointTransactionDO transaction) {
            this.clubId = transaction.getIssuingClubId();
            this.clubNameSnapshot = transaction.getIssuingClubNameSnapshot();
            if (ClubPointCategoryEnum.BASIC_PARTICIPATION.getCategory().equals(transaction.getPointCategory())
                    || ClubPointCategoryEnum.FULL_PARTICIPATION_EXTRA.getCategory().equals(transaction.getPointCategory())) {
                activityPoints += transaction.getPoints();
            } else if (ClubPointCategoryEnum.ACTIVE_CONTRIBUTION.getCategory().equals(transaction.getPointCategory())) {
                contributionPoints += transaction.getPoints();
            } else if (ClubPointCategoryEnum.SPECIAL_REWARD.getCategory().equals(transaction.getPointCategory())) {
                rewardPoints += transaction.getPoints();
            }
            positiveTransactionIds.add(transaction.getId());
        }

        private void addReversal(ClubPointTransactionDO transaction) {
            reversedPoints += transaction.getPoints();
            reversalTransactionIds.add(transaction.getId());
        }

        private Integer totalIssuedPoints() {
            return activityPoints + contributionPoints + rewardPoints - reversedPoints;
        }

    }

}

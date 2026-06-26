package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualRankingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointIncentiveRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualRankingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointIncentiveRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointIncentiveTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointIncentiveSuggestReqBO;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.INCENTIVE_CANCEL;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.INCENTIVE_CONFIRM;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_INCENTIVE_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointIncentiveServiceImpl.class, ClubAuditServiceImpl.class, ClubScopeServiceImpl.class})
class ClubPointIncentiveServiceImplTest extends BaseDbUnitTest {

    private static final Integer YEAR = 2026;
    private static final LocalDateTime OPERATION_TIME = LocalDateTime.of(2027, 1, 3, 10, 0);

    @Resource
    private ClubPointIncentiveService incentiveService;
    @Resource
    private ClubPointIncentiveRecordMapper incentiveRecordMapper;
    @Resource
    private ClubPointAnnualRankingRecordMapper rankingRecordMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;

    @Test
    void generateRankingIncentivesShouldCreateSuggestionsFromRankingAndNotDuplicate() {
        ClubPointAnnualRankingRecordDO first = insertRanking(1L, "CLUB-A", "Alpha Club", 1, 200_000L);
        ClubPointAnnualRankingRecordDO fifth = insertRanking(2L, "CLUB-B", "Beta Club", 5, 100_000L);
        insertRanking(3L, "CLUB-C", "Gamma Club", 7, 0L);

        int created = incentiveService.generateRankingIncentives(buildSuggestReq());

        assertEquals(2, created);
        List<ClubPointIncentiveRecordDO> records = incentiveRecordMapper.selectListByYearTypeStatus(YEAR,
                ClubPointIncentiveTypeEnum.RANKING.getType(), ClubPointIncentiveStatusEnum.SUGGESTED.getStatus());
        assertEquals(2, records.size());
        ClubPointIncentiveRecordDO firstSuggestion = incentiveRecordMapper.selectBySourceTypeAndSourceId(
                ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType(), first.getId());
        assertNotNull(firstSuggestion);
        assertEquals(first.getClubId(), firstSuggestion.getClubId());
        assertEquals(first.getClubNameSnapshot(), firstSuggestion.getClubNameSnapshot());
        assertEquals(200_000L, firstSuggestion.getAmountCent());
        assertEquals(ClubPointIncentiveStatusEnum.SUGGESTED.getStatus(), firstSuggestion.getStatus());
        assertEquals(ClubPointIncentiveTypeEnum.RANKING.getType(), firstSuggestion.getType());
        assertEquals(ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType(), firstSuggestion.getSourceType());
        assertEquals(first.getId(), firstSuggestion.getSourceId());
        assertNull(firstSuggestion.getBudgetRecordId());
        assertTrue(firstSuggestion.getTitle().contains("第1名"));

        ClubPointIncentiveRecordDO fifthSuggestion = incentiveRecordMapper.selectBySourceTypeAndSourceId(
                ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType(), fifth.getId());
        assertNotNull(fifthSuggestion);
        assertEquals(100_000L, fifthSuggestion.getAmountCent());
        assertEquals(0L, transactionMapper.selectCount());

        assertEquals(0, incentiveService.generateRankingIncentives(buildSuggestReq()));
        assertEquals(2L, incentiveRecordMapper.selectCount());
    }

    @Test
    void confirmIncentiveShouldWriteAuditAndMakeStatusFixed() {
        ClubPointIncentiveRecordDO incentive = insertSuggestedIncentive();

        incentiveService.confirmIncentive(buildOperationReq(incentive.getId(), "确认排名激励"));

        ClubPointIncentiveRecordDO confirmed = incentiveRecordMapper.selectById(incentive.getId());
        assertEquals(ClubPointIncentiveStatusEnum.CONFIRMED.getStatus(), confirmed.getStatus());
        assertEquals(9001L, confirmed.getConfirmedBy());
        assertEquals(OPERATION_TIME, confirmed.getConfirmedTime());
        assertEquals("确认排名激励", confirmed.getRemark());
        ClubAuditLogDO audit = auditLogMapper.selectList().get(0);
        assertEquals(INCENTIVE_CONFIRM, audit.getActionType());
        assertEquals("INCENTIVE_RECORD", audit.getBizType());
        assertEquals(incentive.getId(), audit.getBizId());

        assertServiceException(() -> incentiveService.cancelIncentive(
                buildOperationReq(incentive.getId(), "确认后不能取消")), CLUB_INCENTIVE_STATUS_INVALID);
        assertEquals(ClubPointIncentiveStatusEnum.CONFIRMED.getStatus(),
                incentiveRecordMapper.selectById(incentive.getId()).getStatus());
    }

    @Test
    void cancelIncentiveShouldWriteAuditAndAuditFailureShouldRollback() {
        ClubPointIncentiveRecordDO canceledIncentive = insertSuggestedIncentive();
        incentiveService.cancelIncentive(buildOperationReq(canceledIncentive.getId(), "取消排名激励"));

        ClubPointIncentiveRecordDO canceled = incentiveRecordMapper.selectById(canceledIncentive.getId());
        assertEquals(ClubPointIncentiveStatusEnum.CANCELED.getStatus(), canceled.getStatus());
        assertEquals("取消排名激励", canceled.getRemark());
        ClubAuditLogDO cancelAudit = auditLogMapper.selectList().get(0);
        assertEquals(INCENTIVE_CANCEL, cancelAudit.getActionType());
        assertEquals(canceledIncentive.getId(), cancelAudit.getBizId());

        ClubPointIncentiveRecordDO rollbackIncentive = insertSuggestedIncentive();
        assertServiceException(() -> incentiveService.confirmIncentive(
                buildOperationReq(rollbackIncentive.getId(), "审计失败回滚").setOperatorNameSnapshot(null)),
                CLUB_AUDIT_WRITE_FAILED);
        ClubPointIncentiveRecordDO notConfirmed = incentiveRecordMapper.selectById(rollbackIncentive.getId());
        assertEquals(ClubPointIncentiveStatusEnum.SUGGESTED.getStatus(), notConfirmed.getStatus());
    }

    private static ClubPointIncentiveSuggestReqBO buildSuggestReq() {
        return new ClubPointIncentiveSuggestReqBO()
                .setYear(YEAR)
                .setOperatorGlobalScope(true);
    }

    private static ClubPointIncentiveOperationReqBO buildOperationReq(Long id, String reason) {
        return new ClubPointIncentiveOperationReqBO()
                .setId(id)
                .setOperatorGlobalScope(true)
                .setOperatorUserId(9001L)
                .setOperatorNameSnapshot("Annual Admin")
                .setOperatorRoleSnapshot("admin")
                .setOperationTime(OPERATION_TIME)
                .setClientIp("127.0.0.1")
                .setUserAgent("unit-test")
                .setReason(reason);
    }

    private ClubPointAnnualRankingRecordDO insertRanking(Long clubId, String clubCode, String clubName,
                                                         Integer rankNo, Long incentiveAmountCent) {
        ClubPointAnnualRankingRecordDO record = new ClubPointAnnualRankingRecordDO()
                .setYear(YEAR)
                .setClubId(clubId)
                .setClubCodeSnapshot(clubCode)
                .setClubNameSnapshot(clubName)
                .setActivityPoints(100)
                .setContributionPoints(20)
                .setRewardPoints(10)
                .setReversedPoints(0)
                .setTotalIssuedPoints(130 - rankNo)
                .setRankNo(rankNo)
                .setIncentiveAmountCent(incentiveAmountCent)
                .setConfirmStatus(1)
                .setGeneratedTime(OPERATION_TIME.minusDays(1))
                .setSnapshotJson("{}");
        rankingRecordMapper.insert(record);
        return record;
    }

    private ClubPointIncentiveRecordDO insertSuggestedIncentive() {
        ClubPointAnnualRankingRecordDO ranking = insertRanking(10L + incentiveRecordMapper.selectCount(),
                "CLUB-X-" + incentiveRecordMapper.selectCount(),
                "Ranking Club " + incentiveRecordMapper.selectCount(), 1, 200_000L);
        ClubPointIncentiveRecordDO incentive = new ClubPointIncentiveRecordDO()
                .setYear(YEAR)
                .setType(ClubPointIncentiveTypeEnum.RANKING.getType())
                .setClubId(ranking.getClubId())
                .setClubNameSnapshot(ranking.getClubNameSnapshot())
                .setTitle("2026年度俱乐部排名第1名激励")
                .setAmountCent(200_000L)
                .setStatus(ClubPointIncentiveStatusEnum.SUGGESTED.getStatus())
                .setSourceType(ClubPointIncentiveSourceTypeEnum.ANNUAL_RANKING.getType())
                .setSourceId(ranking.getId());
        incentiveRecordMapper.insert(incentive);
        return incentive;
    }

}

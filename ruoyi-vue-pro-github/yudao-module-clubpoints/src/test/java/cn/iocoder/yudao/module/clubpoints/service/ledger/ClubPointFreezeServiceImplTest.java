package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointFreezeDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleItemDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.rule.ClubPointRuleVersionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointFreezeMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleItemMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.rule.ClubPointRuleVersionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointFreezeStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRuleItemCodeEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeConvertReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeCreateReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointFreezeReleaseReqBO;
import cn.iocoder.yudao.module.clubpoints.service.rule.ClubPointRuleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FREEZE_NOT_EXISTS;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_LEDGER_FREEZE_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@Import({ClubPointFreezeServiceImpl.class, ClubPointLedgerServiceImpl.class, ClubPointRuleServiceImpl.class,
        ClubAuditServiceImpl.class})
class ClubPointFreezeServiceImplTest extends BaseDbUnitTest {

    private static final Integer STATUS_PUBLISHED = 2;
    private static final Integer RULE_ITEM_ENABLED = 1;

    @Resource
    private ClubPointFreezeService clubPointFreezeService;
    @Resource
    private ClubPointFreezeMapper freezeMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubPointRuleVersionMapper ruleVersionMapper;
    @Resource
    private ClubPointRuleItemMapper ruleItemMapper;

    @Test
    void freezePointsShouldDecreaseAvailableAndIncreaseFrozen() {
        insertAccount(100, 0);

        Long freezeId = clubPointFreezeService.freezePoints(buildFreezeReq("FRZ-M4-4001", "FREEZE-M4-4001", 40));

        ClubPointFreezeDO freeze = freezeMapper.selectById(freezeId);
        assertNotNull(freeze);
        assertEquals("FRZ-M4-4001", freeze.getFreezeNo());
        assertEquals(100L, freeze.getUserId());
        assertEquals(40, freeze.getPoints());
        assertEquals(ClubPointFreezeStatusEnum.FROZEN.getStatus(), freeze.getStatus());
        assertEquals(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType(), freeze.getSourceType());
        assertEquals(700L, freeze.getSourceId());
        assertEquals(LocalDateTime.of(2026, 6, 2, 9, 0), freeze.getFrozenAt());
        assertNull(freeze.getConvertedAt());
        assertNull(freeze.getReleasedAt());
        assertNull(freeze.getConvertedTransactionId());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(100, account.getNetPoints());
        assertEquals(40, account.getFrozenPoints());
        assertEquals(60, account.getAvailablePoints());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void freezePointsShouldReturnExistingWhenSameIdempotencySubmittedAgain() {
        insertAccount(100, 0);
        ClubPointFreezeCreateReqBO reqBO = buildFreezeReq("FRZ-M4-4002", "FREEZE-M4-4002", 40);
        Long firstId = clubPointFreezeService.freezePoints(reqBO);

        Long secondId = clubPointFreezeService.freezePoints(reqBO);

        assertEquals(firstId, secondId);
        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(40, account.getFrozenPoints());
        assertEquals(60, account.getAvailablePoints());
        assertEquals(1L, freezeMapper.selectCount());
    }

    @Test
    void freezePointsShouldRejectWhenAvailablePointsNotEnough() {
        insertAccount(30, 10);

        assertServiceException(() -> clubPointFreezeService.freezePoints(buildFreezeReq("FRZ-M4-4003", "FREEZE-M4-4003", 25)),
                CLUB_LEDGER_AVAILABLE_POINTS_NOT_ENOUGH);

        assertNull(freezeMapper.selectByIdempotencyKey("FREEZE-M4-4003"));
        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(10, account.getFrozenPoints());
        assertEquals(20, account.getAvailablePoints());
    }

    @Test
    void releaseFreezeShouldRestoreAvailableWithoutCreatingTransaction() {
        insertAccount(100, 40);
        ClubPointFreezeDO freeze = insertFrozen("FRZ-M4-4004", "FREEZE-M4-4004", 40);

        clubPointFreezeService.releaseFreeze(new ClubPointFreezeReleaseReqBO()
                .setFreezeId(freeze.getId())
                .setReleasedAt(LocalDateTime.of(2026, 6, 4, 9, 0))
                .setReleaseReason("审核拒绝释放"));

        ClubPointFreezeDO released = freezeMapper.selectById(freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.RELEASED.getStatus(), released.getStatus());
        assertEquals(LocalDateTime.of(2026, 6, 4, 9, 0), released.getReleasedAt());
        assertEquals("审核拒绝释放", released.getReleaseReason());
        assertNull(released.getConvertedTransactionId());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(100, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(100, account.getAvailablePoints());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void convertFreezeToDeductionShouldCloseFreezeAndCreateRedemptionTransaction() {
        insertPublishedRule(1, 100, 50);
        insertAccount(100, 40);
        ClubPointFreezeDO freeze = insertFrozen("FRZ-M4-4005", "FREEZE-M4-4005", 40);

        Long transactionId = clubPointFreezeService.convertFreezeToDeduction(buildConvertReq(freeze.getId()));

        ClubPointFreezeDO converted = freezeMapper.selectById(freeze.getId());
        assertEquals(ClubPointFreezeStatusEnum.CONVERTED.getStatus(), converted.getStatus());
        assertEquals(LocalDateTime.of(2026, 6, 5, 9, 0), converted.getConvertedAt());
        assertEquals(transactionId, converted.getConvertedTransactionId());

        ClubPointTransactionDO transaction = transactionMapper.selectById(transactionId);
        assertNotNull(transaction);
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(), transaction.getDirection());
        assertEquals(40, transaction.getPoints());
        assertEquals(ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory(), transaction.getPointCategory());
        assertEquals(ClubPointTransactionSourceTypeEnum.REDEMPTION.getType(), transaction.getSourceType());
        assertEquals(700L, transaction.getSourceId());
        assertEquals("REDEMPTION_APPROVE:700", transaction.getIdempotencyKey());

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(100, account.getTotalPositivePoints());
        assertEquals(40, account.getTotalNegativePoints());
        assertEquals(60, account.getNetPoints());
        assertEquals(0, account.getFrozenPoints());
        assertEquals(60, account.getAvailablePoints());
    }

    @Test
    void convertReleasedFreezeShouldRejectAndKeepAccountUnchanged() {
        insertAccount(100, 40);
        ClubPointFreezeDO freeze = insertFrozen("FRZ-M4-4006", "FREEZE-M4-4006", 40)
                .setStatus(ClubPointFreezeStatusEnum.RELEASED.getStatus())
                .setReleasedAt(LocalDateTime.of(2026, 6, 4, 9, 0));
        freezeMapper.updateById(freeze);

        assertServiceException(() -> clubPointFreezeService.convertFreezeToDeduction(buildConvertReq(freeze.getId())),
                CLUB_LEDGER_FREEZE_STATUS_INVALID);

        ClubPointAccountDO account = accountMapper.selectByUserId(100L);
        assertEquals(100, account.getNetPoints());
        assertEquals(40, account.getFrozenPoints());
        assertEquals(60, account.getAvailablePoints());
        assertEquals(0L, transactionMapper.selectCount());
    }

    @Test
    void releaseMissingFreezeShouldReject() {
        assertServiceException(() -> clubPointFreezeService.releaseFreeze(new ClubPointFreezeReleaseReqBO()
                        .setFreezeId(999L)
                        .setReleasedAt(LocalDateTime.of(2026, 6, 4, 9, 0))
                        .setReleaseReason("不存在")),
                CLUB_LEDGER_FREEZE_NOT_EXISTS);
    }

    private static ClubPointFreezeCreateReqBO buildFreezeReq(String freezeNo, String idempotencyKey, Integer points) {
        return new ClubPointFreezeCreateReqBO()
                .setFreezeNo(freezeNo)
                .setUserId(100L)
                .setPoints(points)
                .setSourceType(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType())
                .setSourceId(700L)
                .setFrozenAt(LocalDateTime.of(2026, 6, 2, 9, 0))
                .setIdempotencyKey(idempotencyKey);
    }

    private static ClubPointFreezeConvertReqBO buildConvertReq(Long freezeId) {
        return new ClubPointFreezeConvertReqBO()
                .setFreezeId(freezeId)
                .setTransactionNo("TX-M4-4005")
                .setTransactionIdempotencyKey("REDEMPTION_APPROVE:700")
                .setUserNameSnapshot("员工A")
                .setDeptIdSnapshot(10L)
                .setDeptNameSnapshot("综合部")
                .setSourceTitleSnapshot("兑换审核通过")
                .setReason("审核通过兑换扣减")
                .setConvertedAt(LocalDateTime.of(2026, 6, 5, 9, 0))
                .setRuleItemCode(ClubPointRuleItemCodeEnum.REDEMPTION_MIN_POINTS.getCode())
                .setSourceSnapshotJson("{\"applicationId\":700}")
                .setOperatorUserId(900L);
    }

    private void insertAccount(Integer netPoints, Integer frozenPoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(100L)
                .setTotalPositivePoints(netPoints)
                .setTotalNegativePoints(0)
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(Math.max(netPoints - frozenPoints, 0))
                .setAnnualEarnedPoints(netPoints)
                .setVersion(1));
    }

    private ClubPointFreezeDO insertFrozen(String freezeNo, String idempotencyKey, Integer points) {
        ClubPointFreezeDO freeze = new ClubPointFreezeDO()
                .setFreezeNo(freezeNo)
                .setUserId(100L)
                .setPoints(points)
                .setStatus(ClubPointFreezeStatusEnum.FROZEN.getStatus())
                .setSourceType(ClubPointFreezeSourceTypeEnum.REDEMPTION_APPLICATION.getType())
                .setSourceId(700L)
                .setFrozenAt(LocalDateTime.of(2026, 6, 2, 9, 0))
                .setIdempotencyKey(idempotencyKey);
        freezeMapper.insert(freeze);
        return freeze;
    }

    private void insertPublishedRule(Integer minPoints, Integer maxPoints, Integer defaultPoints) {
        ClubPointRuleVersionDO version = new ClubPointRuleVersionDO()
                .setVersionNo("V-M4-4")
                .setName("M4.4 规则")
                .setStatus(STATUS_PUBLISHED)
                .setPublicityTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setEffectiveTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setPublishedTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setSummary("summary")
                .setContent("content");
        ruleVersionMapper.insert(version);

        ruleItemMapper.insert(new ClubPointRuleItemDO()
                .setRuleVersionId(version.getId())
                .setItemCode(ClubPointRuleItemCodeEnum.REDEMPTION_MIN_POINTS.getCode())
                .setItemName("兑换最低可用积分")
                .setItemType(1)
                .setCategory(ClubPointCategoryEnum.REDEMPTION_DEDUCTION.getCategory())
                .setMinPoints(minPoints)
                .setMaxPoints(maxPoints)
                .setDefaultPoints(defaultPoints)
                .setStatus(RULE_ITEM_ENABLED)
                .setSort(1));
    }

    private static void assertServiceException(Runnable runnable, ErrorCode errorCode) {
        try {
            runnable.run();
            fail("Expected ServiceException");
        } catch (ServiceException ex) {
            assertEquals(errorCode.getCode(), ex.getCode());
            assertEquals(errorCode.getMsg(), ex.getMessage());
        }
    }

}

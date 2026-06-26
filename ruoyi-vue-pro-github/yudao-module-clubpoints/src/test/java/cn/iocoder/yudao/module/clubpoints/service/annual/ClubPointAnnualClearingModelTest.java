package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionSourceTypeEnum;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClubPointAnnualClearingModelTest extends BaseDbUnitTest {

    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;

    @Test
    void annualClearingRecordMapperShouldPersistAndQueryByUserYearAndIdempotency() {
        ClubPointAnnualClearingRecordDO record = buildClearingRecord();
        clearingRecordMapper.insert(record);

        ClubPointAnnualClearingRecordDO saved =
                clearingRecordMapper.selectByUserIdAndYear(100L, 2026);
        assertNotNull(saved);
        assertEquals(2026, saved.getYear());
        assertEquals(100L, saved.getUserId());
        assertEquals(120, saved.getNetPointsBefore());
        assertEquals(30, saved.getFrozenPointsBefore());
        assertEquals(90, saved.getAvailablePointsBefore());
        assertEquals(90, saved.getClearablePoints());
        assertEquals(700L, saved.getClearTransactionId());
        assertEquals(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus(), saved.getStatus());
        assertEquals(900L, saved.getRunId());
        assertEquals("ANNUAL_CLEARING:2026:100", saved.getIdempotencyKey());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), saved.getClearTime());
        assertEquals("清零成功", saved.getErrorMessage());

        assertEquals(saved.getId(), clearingRecordMapper
                .selectByIdempotencyKey("ANNUAL_CLEARING:2026:100").getId());
        assertEquals(1, clearingRecordMapper
                .selectListByYearAndStatus(2026, ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus()).size());
    }

    @Test
    void annualClearingModelShouldUseLedgerAnnualClearingSourceAndBeijingSchedule() {
        assertEquals(ClubPointTransactionSourceTypeEnum.ANNUAL_CLEARING.getType(),
                ClubPointAnnualClearingConstants.TRANSACTION_SOURCE_TYPE);
        assertEquals(ClubPointTransactionDirectionEnum.DECREASE.getDirection(),
                ClubPointAnnualClearingConstants.TRANSACTION_DIRECTION);
        assertEquals(ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory(),
                ClubPointAnnualClearingConstants.POINT_CATEGORY);
        assertEquals("ANNUAL_CLEARING", ClubPointAnnualClearingConstants.POINT_TYPE_CODE);
        assertEquals("ANNUAL_CLEARING:2026:100",
                ClubPointAnnualClearingConstants.buildIdempotencyKey(2026, 100L));
        assertEquals(ZoneId.of("Asia/Shanghai"), ClubPointAnnualClearingConstants.CLEARING_ZONE);
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0),
                ClubPointAnnualClearingConstants.buildScheduledClearTime(2026));
        assertEquals("RELEASE_TO_ACCOUNT_WITHOUT_OVERDUE_CLEARING",
                ClubPointAnnualClearingConstants.CROSS_YEAR_FREEZE_RELEASE_POLICY);
    }

    private static ClubPointAnnualClearingRecordDO buildClearingRecord() {
        return new ClubPointAnnualClearingRecordDO()
                .setYear(2026)
                .setUserId(100L)
                .setNetPointsBefore(120)
                .setFrozenPointsBefore(30)
                .setAvailablePointsBefore(90)
                .setClearablePoints(90)
                .setClearTransactionId(700L)
                .setStatus(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus())
                .setRunId(900L)
                .setIdempotencyKey("ANNUAL_CLEARING:2026:100")
                .setClearTime(LocalDateTime.of(2026, 1, 1, 0, 0))
                .setErrorMessage("清零成功");
    }

}

package cn.iocoder.yudao.module.clubpoints.service.annual;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.annual.ClubPointAnnualClearingRecordDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.annual.ClubPointAnnualClearingRecordMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointAnnualClearingStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearAllReqBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearResultBO;
import cn.iocoder.yudao.module.clubpoints.service.annual.bo.ClubPointAnnualClearUserReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.ClubPointLedgerService;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerCreateReqBO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 年度清零服务实现
 */
@Service
public class ClubPointAnnualClearingServiceImpl implements ClubPointAnnualClearingService {

    @Resource
    private ClubPointAnnualClearingRecordMapper clearingRecordMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointLedgerService ledgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long clearUser(ClubPointAnnualClearUserReqBO reqBO) {
        validateUserReq(reqBO);
        LocalDateTime clearTime = clearTime(reqBO.getYear(), reqBO.getClearTime());
        ClubPointAnnualClearingRecordDO existing = clearingRecordMapper
                .selectByUserIdAndYearForUpdate(reqBO.getUserId(), reqBO.getYear());
        if (isFinished(existing)) {
            return existing.getId();
        }

        ClubPointAccountDO account = accountMapper.selectByUserIdForUpdate(reqBO.getUserId());
        ClubPointAnnualClearingRecordDO record = existing != null ? existing : new ClubPointAnnualClearingRecordDO();
        applySnapshot(record, reqBO, account, clearTime);
        if (record.getId() == null) {
            ClubPointAnnualClearingRecordDO duplicated = insertRecordOrReturnDuplicated(record);
            if (duplicated != null) {
                if (isFinished(duplicated)) {
                    return duplicated.getId();
                }
                record = duplicated;
            }
        }

        if (record.getClearablePoints() <= 0) {
            record.setStatus(ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus())
                    .setClearTransactionId(null)
                    .setErrorMessage("无可清零积分");
            clearingRecordMapper.updateById(record);
            return record.getId();
        }

        Long transactionId = ledgerService.createTransaction(buildLedgerReq(reqBO, record, clearTime));
        record.setStatus(ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus())
                .setClearTransactionId(transactionId)
                .setErrorMessage(null);
        clearingRecordMapper.updateById(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubPointAnnualClearResultBO clearAll(ClubPointAnnualClearAllReqBO reqBO) {
        validateAllReq(reqBO);
        List<ClubPointAccountDO> accounts = accountMapper.selectListForAnnualClearing();
        ClubPointAnnualClearResultBO result = new ClubPointAnnualClearResultBO()
                .setTotalCount(accounts.size())
                .setSuccessCount(0)
                .setSkipCount(0)
                .setFailedCount(0);
        for (ClubPointAccountDO account : accounts) {
            Long recordId = clearUser(new ClubPointAnnualClearUserReqBO()
                    .setYear(reqBO.getYear())
                    .setUserId(account.getUserId())
                    .setRunId(reqBO.getRunId())
                    .setClearTime(reqBO.getClearTime())
                    .setOperatorUserId(reqBO.getOperatorUserId())
                    .setReason(reqBO.getReason()));
            countResult(result, clearingRecordMapper.selectById(recordId));
        }
        return result;
    }

    @Override
    public PageResult<ClubPointAnnualClearingRecordDO> getClearingRecordPage(PageParam pageParam, Integer year,
                                                                             Long userId, Integer status) {
        if (pageParam == null) {
            throw new IllegalArgumentException("pageParam is required");
        }
        return clearingRecordMapper.selectPageByQuery(pageParam, year, userId, status);
    }

    private ClubPointAnnualClearingRecordDO insertRecordOrReturnDuplicated(ClubPointAnnualClearingRecordDO record) {
        try {
            clearingRecordMapper.insert(record);
            return null;
        } catch (DuplicateKeyException ex) {
            ClubPointAnnualClearingRecordDO duplicated = clearingRecordMapper
                    .selectByUserIdAndYearForUpdate(record.getUserId(), record.getYear());
            if (duplicated != null) {
                return duplicated;
            }
            throw ex;
        }
    }

    private static ClubPointLedgerCreateReqBO buildLedgerReq(ClubPointAnnualClearUserReqBO reqBO,
                                                             ClubPointAnnualClearingRecordDO record,
                                                             LocalDateTime clearTime) {
        return new ClubPointLedgerCreateReqBO()
                .setTransactionNo("AC-" + reqBO.getYear() + "-" + reqBO.getUserId())
                .setUserId(reqBO.getUserId())
                .setUserNameSnapshot("年度清零用户" + reqBO.getUserId())
                .setDirection(ClubPointAnnualClearingConstants.TRANSACTION_DIRECTION)
                .setPoints(record.getClearablePoints())
                .setPointCategory(ClubPointAnnualClearingConstants.POINT_CATEGORY)
                .setPointTypeCode(ClubPointAnnualClearingConstants.POINT_TYPE_CODE)
                .setSourceType(ClubPointAnnualClearingConstants.TRANSACTION_SOURCE_TYPE)
                .setSourceId(record.getId())
                .setSourceTitleSnapshot(ClubPointAnnualClearingConstants.SOURCE_TITLE)
                .setMaterialSummary(ClubPointAnnualClearingConstants.SOURCE_TITLE)
                .setReason(reqBO.getReason() != null ? reqBO.getReason() : ClubPointAnnualClearingConstants.SOURCE_TITLE)
                .setOccurredAt(clearTime)
                .setIdempotencyKey(record.getIdempotencyKey())
                .setOperatorUserId(reqBO.getOperatorUserId())
                .setSourceSnapshotJson(buildSourceSnapshot(record));
    }

    private static String buildSourceSnapshot(ClubPointAnnualClearingRecordDO record) {
        return "{\"clearingRecordId\":" + record.getId()
                + ",\"year\":" + record.getYear()
                + ",\"userId\":" + record.getUserId()
                + ",\"netPointsBefore\":" + record.getNetPointsBefore()
                + ",\"frozenPointsBefore\":" + record.getFrozenPointsBefore()
                + ",\"availablePointsBefore\":" + record.getAvailablePointsBefore()
                + ",\"clearablePoints\":" + record.getClearablePoints() + "}";
    }

    private static void applySnapshot(ClubPointAnnualClearingRecordDO record, ClubPointAnnualClearUserReqBO reqBO,
                                      ClubPointAccountDO account, LocalDateTime clearTime) {
        int netPoints = account == null ? 0 : account.getNetPoints();
        int frozenPoints = account == null ? 0 : account.getFrozenPoints();
        int availablePoints = account == null ? 0 : account.getAvailablePoints();
        record.setYear(reqBO.getYear())
                .setUserId(reqBO.getUserId())
                .setNetPointsBefore(netPoints)
                .setFrozenPointsBefore(frozenPoints)
                .setAvailablePointsBefore(availablePoints)
                .setClearablePoints(Math.max(availablePoints, 0))
                .setStatus(ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus())
                .setRunId(reqBO.getRunId())
                .setIdempotencyKey(ClubPointAnnualClearingConstants.buildIdempotencyKey(reqBO.getYear(), reqBO.getUserId()))
                .setClearTime(clearTime)
                .setErrorMessage(null);
    }

    private static void countResult(ClubPointAnnualClearResultBO result, ClubPointAnnualClearingRecordDO record) {
        if (ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus().equals(record.getStatus())) {
            result.setSuccessCount(result.getSuccessCount() + 1);
        } else if (ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus().equals(record.getStatus())) {
            result.setSkipCount(result.getSkipCount() + 1);
        } else if (ClubPointAnnualClearingStatusEnum.FAILED.getStatus().equals(record.getStatus())) {
            result.setFailedCount(result.getFailedCount() + 1);
        }
    }

    private static boolean isFinished(ClubPointAnnualClearingRecordDO record) {
        return record != null && (ClubPointAnnualClearingStatusEnum.SUCCESS.getStatus().equals(record.getStatus())
                || ClubPointAnnualClearingStatusEnum.SKIPPED.getStatus().equals(record.getStatus()));
    }

    private static LocalDateTime clearTime(Integer year, LocalDateTime clearTime) {
        return clearTime != null ? clearTime : ClubPointAnnualClearingConstants.buildScheduledClearTime(year);
    }

    private static void validateUserReq(ClubPointAnnualClearUserReqBO reqBO) {
        if (reqBO == null || reqBO.getYear() == null || reqBO.getUserId() == null) {
            throw new IllegalArgumentException("year and userId are required");
        }
    }

    private static void validateAllReq(ClubPointAnnualClearAllReqBO reqBO) {
        if (reqBO == null || reqBO.getYear() == null) {
            throw new IllegalArgumentException("year is required");
        }
    }

}

package cn.iocoder.yudao.module.clubpoints.service.ledger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointTransactionMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointCategoryEnum;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionDirectionEnum;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointAccountPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerMemberSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerPageReqBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerSummaryBO;
import cn.iocoder.yudao.module.clubpoints.service.ledger.bo.ClubPointLedgerTransactionBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 积分账本查询服务实现
 */
@Service
public class ClubPointLedgerQueryServiceImpl implements ClubPointLedgerQueryService {

    private static final int STATUS_ACTIVE = 1;

    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubPointTransactionMapper transactionMapper;
    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private ClubScopeService clubScopeService;

    @Override
    @Transactional(readOnly = true)
    public ClubPointLedgerSummaryBO getAppSummary(Long loginUserId) {
        clubScopeService.validateSelf(loginUserId, loginUserId);
        ClubPointAccountDO account = accountMapper.selectByUserId(loginUserId);
        return new ClubPointLedgerSummaryBO()
                .setAvailablePoints(account == null ? 0 : account.getAvailablePoints())
                .setFrozenPoints(account == null ? 0 : account.getFrozenPoints())
                .setTotalPositivePoints(account == null ? 0 : account.getTotalPositivePoints())
                .setTotalNegativePoints(account == null ? 0 : account.getTotalNegativePoints())
                .setAnnualClearedPoints(sumAnnualClearedPoints(loginUserId))
                .setLastTransactionTime(resolveLastTransactionTime(account, loginUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointLedgerTransactionBO> getAppTransactionPage(Long loginUserId,
                                                                         ClubPointLedgerPageReqBO reqBO) {
        clubScopeService.validateSelf(loginUserId, loginUserId);
        return toTransactionPage(transactionMapper.selectPageByUserId(reqBO, loginUserId,
                reqBO.getDirection(), reqBO.getPointCategory(), reqBO.getSourceType(), reqBO.getClubId(),
                reqBO.getStartTime(), reqBO.getEndTime()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointLedgerMemberSummaryBO> getLeaderMemberSummaryPage(Long loginUserId,
                                                                                 ClubPointAccountPageReqBO reqBO) {
        clubScopeService.validateManagedClub(loginUserId, reqBO.getClubId());
        PageResult<ClubMemberDO> memberPage = clubMemberMapper.selectPageByClubIdAndStatus(reqBO,
                reqBO.getClubId(), STATUS_ACTIVE, reqBO.getUserId());
        if (memberPage.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), memberPage.getTotal());
        }

        Map<Long, ClubPointLedgerMemberSummaryBO> summaryMap = memberPage.getList().stream()
                .map(member -> new ClubPointLedgerMemberSummaryBO()
                        .setClubId(member.getClubId())
                        .setClubNameSnapshot(member.getClubNameSnapshot())
                        .setUserId(member.getUserId())
                        .setUserNameSnapshot(member.getUserNameSnapshot())
                        .setDeptIdSnapshot(member.getDeptIdSnapshot())
                        .setDeptNameSnapshot(member.getDeptNameSnapshot())
                        .setClubPositivePoints(0)
                        .setClubNegativePoints(0)
                        .setClubNetPoints(0))
                .collect(Collectors.toMap(ClubPointLedgerMemberSummaryBO::getUserId, Function.identity(),
                        (left, right) -> left, java.util.LinkedHashMap::new));

        List<Long> userIds = memberPage.getList().stream()
                .map(ClubMemberDO::getUserId)
                .collect(Collectors.toList());
        for (ClubPointTransactionDO transaction : transactionMapper.selectListByUserIdsAndIssuingClubId(
                userIds, reqBO.getClubId())) {
            ClubPointLedgerMemberSummaryBO summary = summaryMap.get(transaction.getUserId());
            if (summary == null) {
                continue;
            }
            if (isIncrease(transaction)) {
                summary.setClubPositivePoints(summary.getClubPositivePoints() + transaction.getPoints());
            } else {
                summary.setClubNegativePoints(summary.getClubNegativePoints() + transaction.getPoints());
            }
            summary.setClubNetPoints(summary.getClubPositivePoints() - summary.getClubNegativePoints())
                    .setLastTransactionId(transaction.getId())
                    .setLastTransactionTime(transaction.getOccurredAt());
        }
        return new PageResult<>(summaryMap.values().stream().collect(Collectors.toList()), memberPage.getTotal());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointLedgerTransactionBO> getLeaderTransactionPage(Long loginUserId,
                                                                            ClubPointLedgerPageReqBO reqBO) {
        clubScopeService.validateManagedClub(loginUserId, reqBO.getClubId());
        return toTransactionPage(transactionMapper.selectPageByIssuingClubId(reqBO, reqBO.getClubId(),
                reqBO.getUserId(), reqBO.getDirection(), reqBO.getPointCategory(), reqBO.getSourceType(),
                reqBO.getStartTime(), reqBO.getEndTime()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointAccountDO> getAdminAccountPage(ClubPointAccountPageReqBO reqBO) {
        return accountMapper.selectPage(reqBO, reqBO.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ClubPointLedgerTransactionBO> getAdminTransactionPage(ClubPointLedgerPageReqBO reqBO) {
        return toTransactionPage(transactionMapper.selectPageForAdmin(reqBO, reqBO.getUserId(),
                reqBO.getDirection(), reqBO.getPointCategory(), reqBO.getSourceType(), reqBO.getClubId(),
                reqBO.getStartTime(), reqBO.getEndTime()));
    }

    private Integer sumAnnualClearedPoints(Long userId) {
        int sum = 0;
        for (ClubPointTransactionDO transaction : transactionMapper.selectEffectiveListByUserId(userId)) {
            if (ClubPointCategoryEnum.ANNUAL_CLEARING.getCategory().equals(transaction.getPointCategory())) {
                sum += transaction.getPoints();
            }
        }
        return sum;
    }

    private java.time.LocalDateTime resolveLastTransactionTime(ClubPointAccountDO account, Long userId) {
        if (account != null && account.getLastTransactionTime() != null) {
            return account.getLastTransactionTime();
        }
        List<ClubPointTransactionDO> transactions = transactionMapper.selectEffectiveListByUserId(userId);
        return transactions.isEmpty() ? null : transactions.get(transactions.size() - 1).getOccurredAt();
    }

    private PageResult<ClubPointLedgerTransactionBO> toTransactionPage(PageResult<ClubPointTransactionDO> pageResult) {
        List<Long> ids = pageResult.getList().stream()
                .map(ClubPointTransactionDO::getId)
                .collect(Collectors.toList());
        Map<Long, Long> reverseMap = new HashMap<>();
        for (ClubPointTransactionDO reverse : transactionMapper.selectListByReverseOfTransactionIds(ids)) {
            reverseMap.put(reverse.getReverseOfTransactionId(), reverse.getId());
        }
        List<ClubPointLedgerTransactionBO> list = pageResult.getList().stream()
                .map(transaction -> toTransactionBO(transaction, reverseMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    private static ClubPointLedgerTransactionBO toTransactionBO(ClubPointTransactionDO transaction,
                                                               Map<Long, Long> reverseMap) {
        Long reverseTransactionId = reverseMap.get(transaction.getId());
        return new ClubPointLedgerTransactionBO()
                .setId(transaction.getId())
                .setTransactionNo(transaction.getTransactionNo())
                .setUserId(transaction.getUserId())
                .setUserNameSnapshot(transaction.getUserNameSnapshot())
                .setDeptIdSnapshot(transaction.getDeptIdSnapshot())
                .setDeptNameSnapshot(transaction.getDeptNameSnapshot())
                .setDirection(transaction.getDirection())
                .setPoints(transaction.getPoints())
                .setPointCategory(transaction.getPointCategory())
                .setSourceType(transaction.getSourceType())
                .setSourceId(transaction.getSourceId())
                .setSourceItemId(transaction.getSourceItemId())
                .setSourceTitleSnapshot(transaction.getSourceTitleSnapshot())
                .setIssuingClubId(transaction.getIssuingClubId())
                .setIssuingClubNameSnapshot(transaction.getIssuingClubNameSnapshot())
                .setRuleVersionId(transaction.getRuleVersionId())
                .setEvidenceType(transaction.getEvidenceType())
                .setReason(transaction.getReason())
                .setMaterialSummary(transaction.getMaterialSummary())
                .setOccurredTime(transaction.getOccurredAt())
                .setCreatedTime(transaction.getCreateTime())
                .setReversed(reverseTransactionId != null)
                .setReverseTransactionId(reverseTransactionId);
    }

    private static boolean isIncrease(ClubPointTransactionDO transaction) {
        return ClubPointTransactionDirectionEnum.INCREASE.getDirection().equals(transaction.getDirection());
    }

}

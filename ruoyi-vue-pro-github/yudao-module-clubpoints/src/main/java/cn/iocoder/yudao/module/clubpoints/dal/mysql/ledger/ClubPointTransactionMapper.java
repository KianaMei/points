package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface ClubPointTransactionMapper extends BaseMapperX<ClubPointTransactionDO> {

    List<Integer> QUERY_EFFECTIVE_STATUSES = Arrays.asList(
            ClubPointTransactionStatusEnum.VALID.getStatus(),
            ClubPointTransactionStatusEnum.REVERSAL.getStatus());

    default List<ClubPointTransactionDO> selectEffectiveListForRebuild() {
        return selectList(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .in(ClubPointTransactionDO::getStatus, Arrays.asList(
                        ClubPointTransactionStatusEnum.VALID.getStatus(),
                        ClubPointTransactionStatusEnum.REVERSAL.getStatus()))
                .orderByAsc(ClubPointTransactionDO::getUserId)
                .orderByAsc(ClubPointTransactionDO::getOccurredAt)
                .orderByAsc(ClubPointTransactionDO::getId));
    }

    default List<ClubPointTransactionDO> selectEffectiveListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eq(ClubPointTransactionDO::getUserId, userId)
                .in(ClubPointTransactionDO::getStatus, Arrays.asList(
                        ClubPointTransactionStatusEnum.VALID.getStatus(),
                        ClubPointTransactionStatusEnum.REVERSAL.getStatus()))
                .orderByAsc(ClubPointTransactionDO::getOccurredAt)
                .orderByAsc(ClubPointTransactionDO::getId));
    }

    default ClubPointTransactionDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(ClubPointTransactionDO::getId, id);
    }

    default ClubPointTransactionDO selectByTransactionNo(String transactionNo) {
        return selectOne(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eq(ClubPointTransactionDO::getTransactionNo, transactionNo));
    }

    default ClubPointTransactionDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eq(ClubPointTransactionDO::getIdempotencyKey, idempotencyKey));
    }

    default ClubPointTransactionDO selectByReverseOfTransactionId(Long reverseOfTransactionId) {
        return selectOne(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eq(ClubPointTransactionDO::getReverseOfTransactionId, reverseOfTransactionId));
    }

    default List<ClubPointTransactionDO> selectListByReverseOfTransactionIds(Collection<Long> reverseOfTransactionIds) {
        if (reverseOfTransactionIds == null || reverseOfTransactionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .in(ClubPointTransactionDO::getReverseOfTransactionId, reverseOfTransactionIds));
    }

    default PageResult<ClubPointTransactionDO> selectPageByUserId(PageParam pageParam, Long userId,
                                                                  Integer direction, Integer pointCategory,
                                                                  Integer sourceType, Long clubId,
                                                                  java.time.LocalDateTime startTime,
                                                                  java.time.LocalDateTime endTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eq(ClubPointTransactionDO::getUserId, userId)
                .in(ClubPointTransactionDO::getStatus, QUERY_EFFECTIVE_STATUSES)
                .eqIfPresent(ClubPointTransactionDO::getDirection, direction)
                .eqIfPresent(ClubPointTransactionDO::getPointCategory, pointCategory)
                .eqIfPresent(ClubPointTransactionDO::getSourceType, sourceType)
                .eqIfPresent(ClubPointTransactionDO::getIssuingClubId, clubId)
                .betweenIfPresent(ClubPointTransactionDO::getOccurredAt, startTime, endTime)
                .orderByDesc(ClubPointTransactionDO::getOccurredAt)
                .orderByDesc(ClubPointTransactionDO::getId));
    }

    default PageResult<ClubPointTransactionDO> selectPageByIssuingClubId(PageParam pageParam, Long issuingClubId,
                                                                         Long userId, Integer direction,
                                                                         Integer pointCategory, Integer sourceType,
                                                                         java.time.LocalDateTime startTime,
                                                                         java.time.LocalDateTime endTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eq(ClubPointTransactionDO::getIssuingClubId, issuingClubId)
                .eqIfPresent(ClubPointTransactionDO::getUserId, userId)
                .in(ClubPointTransactionDO::getStatus, QUERY_EFFECTIVE_STATUSES)
                .eqIfPresent(ClubPointTransactionDO::getDirection, direction)
                .eqIfPresent(ClubPointTransactionDO::getPointCategory, pointCategory)
                .eqIfPresent(ClubPointTransactionDO::getSourceType, sourceType)
                .betweenIfPresent(ClubPointTransactionDO::getOccurredAt, startTime, endTime)
                .orderByDesc(ClubPointTransactionDO::getOccurredAt)
                .orderByDesc(ClubPointTransactionDO::getId));
    }

    default PageResult<ClubPointTransactionDO> selectPageForAdmin(PageParam pageParam, Long userId,
                                                                  Integer direction, Integer pointCategory,
                                                                  Integer sourceType, Long clubId,
                                                                  java.time.LocalDateTime startTime,
                                                                  java.time.LocalDateTime endTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .eqIfPresent(ClubPointTransactionDO::getUserId, userId)
                .in(ClubPointTransactionDO::getStatus, QUERY_EFFECTIVE_STATUSES)
                .eqIfPresent(ClubPointTransactionDO::getDirection, direction)
                .eqIfPresent(ClubPointTransactionDO::getPointCategory, pointCategory)
                .eqIfPresent(ClubPointTransactionDO::getSourceType, sourceType)
                .eqIfPresent(ClubPointTransactionDO::getIssuingClubId, clubId)
                .betweenIfPresent(ClubPointTransactionDO::getOccurredAt, startTime, endTime)
                .orderByDesc(ClubPointTransactionDO::getOccurredAt)
                .orderByDesc(ClubPointTransactionDO::getId));
    }

    default List<ClubPointTransactionDO> selectListByUserIdsAndIssuingClubId(Collection<Long> userIds, Long issuingClubId) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ClubPointTransactionDO>()
                .in(ClubPointTransactionDO::getUserId, userIds)
                .eq(ClubPointTransactionDO::getIssuingClubId, issuingClubId)
                .in(ClubPointTransactionDO::getStatus, QUERY_EFFECTIVE_STATUSES)
                .orderByAsc(ClubPointTransactionDO::getUserId)
                .orderByAsc(ClubPointTransactionDO::getOccurredAt)
                .orderByAsc(ClubPointTransactionDO::getId));
    }

}

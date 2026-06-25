package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointTransactionStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.List;

@Mapper
public interface ClubPointTransactionMapper extends BaseMapperX<ClubPointTransactionDO> {

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

}

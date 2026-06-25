package cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointTransactionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClubPointTransactionMapper extends BaseMapperX<ClubPointTransactionDO> {

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

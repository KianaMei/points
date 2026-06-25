package cn.iocoder.yudao.module.clubpoints.service.scope;

import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubLeaderMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.club.ClubMemberMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;

/**
 * 俱乐部积分数据范围服务实现
 */
@Service
public class ClubScopeServiceImpl implements ClubScopeService {

    private static final int STATUS_ACTIVE = 1;

    @Resource
    private ClubMemberMapper clubMemberMapper;
    @Resource
    private ClubLeaderMapper clubLeaderMapper;

    @Override
    public void validateSelf(Long loginUserId, Long targetUserId) {
        if (loginUserId == null || !Objects.equals(loginUserId, targetUserId)) {
            throw exception(CLUB_SCOPE_DENIED);
        }
    }

    @Override
    public void validateJoinedClub(Long loginUserId, Long clubId) {
        if (loginUserId == null || clubId == null
                || clubMemberMapper.selectByUserIdAndClubIdAndStatus(loginUserId, clubId, STATUS_ACTIVE) == null) {
            throw exception(CLUB_SCOPE_DENIED);
        }
    }

    @Override
    public void validateManagedClub(Long loginUserId, Long clubId) {
        if (loginUserId == null || clubId == null
                || clubLeaderMapper.selectByUserIdAndClubIdAndStatus(loginUserId, clubId, STATUS_ACTIVE) == null) {
            throw exception(CLUB_SCOPE_DENIED);
        }
    }

    @Override
    public boolean hasGlobalScope(boolean globalScope) {
        return globalScope;
    }

    @Override
    public void validateGlobal(boolean globalScope) {
        if (!hasGlobalScope(globalScope)) {
            throw exception(CLUB_SCOPE_DENIED);
        }
    }

}

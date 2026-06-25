package cn.iocoder.yudao.module.clubpoints.service.scope;

/**
 * 俱乐部积分数据范围服务
 */
public interface ClubScopeService {

    void validateSelf(Long loginUserId, Long targetUserId);

    void validateJoinedClub(Long loginUserId, Long clubId);

    void validateManagedClub(Long loginUserId, Long clubId);

    boolean hasGlobalScope(boolean globalScope);

    void validateGlobal(boolean globalScope);

}

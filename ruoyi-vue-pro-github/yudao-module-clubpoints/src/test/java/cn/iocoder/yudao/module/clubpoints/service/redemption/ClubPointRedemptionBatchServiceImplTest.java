package cn.iocoder.yudao.module.clubpoints.service.redemption;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.audit.ClubAuditLogDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.ledger.ClubPointAccountDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionBatchDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.redemption.ClubPointRedemptionEligibilitySnapshotDO;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.audit.ClubAuditLogMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.ledger.ClubPointAccountMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionBatchMapper;
import cn.iocoder.yudao.module.clubpoints.dal.mysql.redemption.ClubPointRedemptionEligibilitySnapshotMapper;
import cn.iocoder.yudao.module.clubpoints.enums.ClubPointRedemptionBatchStatusEnum;
import cn.iocoder.yudao.module.clubpoints.service.audit.ClubAuditServiceImpl;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchOperationReqBO;
import cn.iocoder.yudao.module.clubpoints.service.redemption.bo.ClubPointRedemptionBatchSaveReqBO;
import cn.iocoder.yudao.module.clubpoints.service.scope.ClubScopeServiceImpl;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.clubpoints.enums.ClubAuditActionTypeConstants.REDEMPTION_BATCH_RULE_UPDATE;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_AUDIT_WRITE_FAILED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_CLOSED;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_REDEMPTION_BATCH_STATUS_INVALID;
import static cn.iocoder.yudao.module.clubpoints.enums.ErrorCodeConstants.CLUB_SCOPE_DENIED;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_IS_DISABLE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import({ClubPointRedemptionBatchServiceImpl.class, ClubAuditServiceImpl.class, ClubScopeServiceImpl.class,
        ClubPointRedemptionBatchServiceImplTest.TestAdminUserApi.class,
        ClubPointRedemptionBatchServiceImplTest.TestDeptApi.class})
class ClubPointRedemptionBatchServiceImplTest extends BaseDbUnitTest {

    private static final LocalDateTime OPEN_TIME = LocalDateTime.of(2026, 7, 1, 9, 0);
    private static final LocalDateTime CLOSE_TIME = LocalDateTime.of(2099, 7, 10, 18, 0);

    @Resource
    private ClubPointRedemptionBatchService redemptionBatchService;
    @Resource
    private ClubPointRedemptionBatchMapper batchMapper;
    @Resource
    private ClubPointRedemptionEligibilitySnapshotMapper eligibilitySnapshotMapper;
    @Resource
    private ClubPointAccountMapper accountMapper;
    @Resource
    private ClubAuditLogMapper auditLogMapper;
    @Resource
    private TestAdminUserApi adminUserApi;
    @Resource
    private TestDeptApi deptApi;

    @BeforeEach
    void setUp() {
        adminUserApi.clear();
        deptApi.clear();
    }

    @Test
    void createBatchShouldPersistDraftBatchAndRequireGlobalScope() {
        Long batchId = redemptionBatchService.createBatch(buildSaveReq(null)
                .setOperatorGlobalScope(true));

        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        assertEquals(2026, batch.getYear());
        assertEquals("2026 夏季兑换批次", batch.getName());
        assertEquals(ClubPointRedemptionBatchStatusEnum.DRAFT.getStatus(), batch.getStatus());
        assertEquals(OPEN_TIME, batch.getOpenTime());
        assertEquals(CLOSE_TIME, batch.getCloseTime());
        assertEquals(50, batch.getMinAvailablePoints());
        assertEquals(2, batch.getQualifiedCount());
        assertTrue(batch.getIncludeTieAtCutoff());
        assertEquals("{\"minAvailablePoints\":50,\"qualifiedCount\":2,\"includeTieAtCutoff\":true}",
                batch.getQualificationRuleJson());
        assertFalse(batch.getSnapshotGenerated());
        assertNull(batch.getSnapshotGeneratedTime());
        assertEquals(6001L, batch.getRuleVersionId());
        assertEquals("{\"rule\":\"redemption\"}", batch.getRuleSnapshotJson());

        assertServiceException(() -> redemptionBatchService.createBatch(buildSaveReq(null)
                .setOperatorGlobalScope(false)), CLUB_SCOPE_DENIED);
    }

    @Test
    void updateDraftBatchShouldWriteAuditWhenQualificationRulesChanged() {
        Long batchId = redemptionBatchService.createBatch(buildSaveReq(null).setOperatorGlobalScope(true));

        redemptionBatchService.updateBatch(buildSaveReq(batchId)
                .setMinAvailablePoints(80)
                .setQualifiedCount(3)
                .setIncludeTieAtCutoff(false)
                .setQualificationRuleJson("{\"minAvailablePoints\":80,\"qualifiedCount\":3,\"includeTieAtCutoff\":false}")
                .setReason("调整资格规则")
                .setOperatorGlobalScope(true));

        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        assertEquals(80, batch.getMinAvailablePoints());
        assertEquals(3, batch.getQualifiedCount());
        assertFalse(batch.getIncludeTieAtCutoff());
        ClubAuditLogDO auditLog = auditLogMapper.selectOne(null);
        assertEquals(REDEMPTION_BATCH_RULE_UPDATE, auditLog.getActionType());
        assertEquals("REDEMPTION_BATCH", auditLog.getBizType());
        assertEquals(batchId, auditLog.getBizId());
        assertTrue(auditLog.getBeforeJson().contains("\"minAvailablePoints\":50"));
        assertTrue(auditLog.getAfterJson().contains("\"minAvailablePoints\":80"));
    }

    @Test
    void updateDraftBatchShouldRollbackWhenRuleAuditFails() {
        Long batchId = redemptionBatchService.createBatch(buildSaveReq(null).setOperatorGlobalScope(true));

        assertServiceException(() -> redemptionBatchService.updateBatch(buildSaveReq(batchId)
                .setMinAvailablePoints(80)
                .setQualificationRuleJson("{\"minAvailablePoints\":80}")
                .setOperatorNameSnapshot(null)
                .setOperatorGlobalScope(true)), CLUB_AUDIT_WRITE_FAILED);

        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        assertEquals(50, batch.getMinAvailablePoints());
        assertEquals("{\"minAvailablePoints\":50,\"qualifiedCount\":2,\"includeTieAtCutoff\":true}",
                batch.getQualificationRuleJson());
        assertEquals(0L, auditLogMapper.selectCount());
    }

    @Test
    void openBatchShouldGenerateEligibilitySnapshotsAndLockBatchRules() {
        Long batchId = redemptionBatchService.createBatch(buildSaveReq(null).setOperatorGlobalScope(true));
        putUserWithDept(1001L, "员工1001", 11L, "运营部");
        putUserWithDept(1002L, "员工1002", 12L, "产品部");
        putUserWithDept(1003L, "员工1003", 12L, "产品部");
        putUserWithDept(1004L, "员工1004", 13L, "后勤部");
        insertAccount(1001L, 120, 0, 120, 180);
        insertAccount(1002L, 80, 5, 80, 160);
        insertAccount(1003L, 80, 0, 80, 150);
        insertAccount(1004L, 49, 0, 49, 60);

        redemptionBatchService.openBatch(batchId, buildOperationReq("开启批次").setOperatorGlobalScope(true));

        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        assertEquals(ClubPointRedemptionBatchStatusEnum.OPENED.getStatus(), batch.getStatus());
        assertTrue(batch.getSnapshotGenerated());
        assertNotNull(batch.getSnapshotGeneratedTime());
        List<ClubPointRedemptionEligibilitySnapshotDO> snapshots =
                eligibilitySnapshotMapper.selectListByBatchId(batchId);
        assertEquals(4, snapshots.size());
        assertSnapshot(batchId, 1001L, "员工1001", "运营部", 1, true, false, "满足资格规则");
        assertSnapshot(batchId, 1002L, "员工1002", "产品部", 2, true, false, "满足资格规则");
        assertSnapshot(batchId, 1003L, "员工1003", "产品部", 3, true, true, "并列 cutoff 进入");
        assertSnapshot(batchId, 1004L, "员工1004", "后勤部", 4, false, false, "低于最低可用积分");

        assertServiceException(() -> redemptionBatchService.updateBatch(buildSaveReq(batchId)
                .setDescription("开启后修改")
                .setOperatorGlobalScope(true)), CLUB_REDEMPTION_BATCH_STATUS_INVALID);
    }

    @Test
    void closeBatchShouldRejectNewApplicationsThroughOpenValidation() {
        Long batchId = redemptionBatchService.createBatch(buildSaveReq(null).setOperatorGlobalScope(true));
        redemptionBatchService.openBatch(batchId, buildOperationReq("开启批次").setOperatorGlobalScope(true));

        redemptionBatchService.validateBatchOpenForApply(batchId);
        redemptionBatchService.closeBatch(batchId, buildOperationReq("关闭批次").setOperatorGlobalScope(true));

        ClubPointRedemptionBatchDO batch = batchMapper.selectById(batchId);
        assertEquals(ClubPointRedemptionBatchStatusEnum.CLOSED.getStatus(), batch.getStatus());
        assertServiceException(() -> redemptionBatchService.validateBatchOpenForApply(batchId),
                CLUB_REDEMPTION_BATCH_CLOSED);
    }

    private void assertSnapshot(Long batchId, Long userId, String userName, String deptName, Integer rankNo,
                                boolean qualified, boolean tieAtCutoff, String reason) {
        ClubPointRedemptionEligibilitySnapshotDO snapshot =
                eligibilitySnapshotMapper.selectByBatchIdAndUserId(batchId, userId);
        assertNotNull(snapshot);
        assertEquals(userName, snapshot.getUserNameSnapshot());
        assertEquals(deptName, snapshot.getDeptNameSnapshot());
        assertEquals(rankNo, snapshot.getRankNo());
        assertEquals(qualified, snapshot.getQualified());
        assertEquals(tieAtCutoff, snapshot.getTieAtCutoff());
        assertEquals(reason, snapshot.getQualificationReason());
    }

    private void putUserWithDept(Long userId, String nickname, Long deptId, String deptName) {
        adminUserApi.putUser(userId, nickname, deptId);
        deptApi.putDept(deptId, deptName);
    }

    private void insertAccount(Long userId, Integer netPoints, Integer frozenPoints,
                               Integer availablePoints, Integer annualEarnedPoints) {
        accountMapper.insert(new ClubPointAccountDO()
                .setUserId(userId)
                .setTotalPositivePoints(annualEarnedPoints)
                .setTotalNegativePoints(Math.max(0, annualEarnedPoints - netPoints))
                .setNetPoints(netPoints)
                .setFrozenPoints(frozenPoints)
                .setAvailablePoints(availablePoints)
                .setAnnualEarnedPoints(annualEarnedPoints)
                .setVersion(0));
    }

    private static ClubPointRedemptionBatchSaveReqBO buildSaveReq(Long id) {
        return new ClubPointRedemptionBatchSaveReqBO()
                .setId(id)
                .setYear(2026)
                .setName("2026 夏季兑换批次")
                .setOpenTime(OPEN_TIME)
                .setCloseTime(CLOSE_TIME)
                .setDescription("批次说明")
                .setMinAvailablePoints(50)
                .setQualifiedCount(2)
                .setIncludeTieAtCutoff(true)
                .setQualificationRuleJson("{\"minAvailablePoints\":50,\"qualifiedCount\":2,\"includeTieAtCutoff\":true}")
                .setRuleVersionId(6001L)
                .setRuleSnapshotJson("{\"rule\":\"redemption\"}")
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason("维护兑换批次");
    }

    private static ClubPointRedemptionBatchOperationReqBO buildOperationReq(String reason) {
        return new ClubPointRedemptionBatchOperationReqBO()
                .setOperatorUserId(900L)
                .setOperatorNameSnapshot("管理员")
                .setOperatorRoleSnapshot("club_points_admin")
                .setClientIp("127.0.0.1")
                .setUserAgent("JUnit")
                .setReason(reason);
    }

    static class TestAdminUserApi implements AdminUserApi {

        private final Map<Long, AdminUserRespDTO> users = new ConcurrentHashMap<>();

        void clear() {
            users.clear();
        }

        void putUser(Long id, String nickname, Long deptId) {
            AdminUserRespDTO user = new AdminUserRespDTO();
            user.setId(id);
            user.setNickname(nickname);
            user.setDeptId(deptId);
            user.setStatus(CommonStatusEnum.ENABLE.getStatus());
            users.put(id, user);
        }

        @Override
        public AdminUserRespDTO getUser(Long id) {
            return users.get(id);
        }

        @Override
        public List<AdminUserRespDTO> getUserListBySubordinate(Long id) {
            return new ArrayList<>();
        }

        @Override
        public List<AdminUserRespDTO> getUserList(Collection<Long> ids) {
            List<AdminUserRespDTO> result = new ArrayList<>();
            for (Long id : ids) {
                AdminUserRespDTO user = users.get(id);
                if (user != null) {
                    result.add(user);
                }
            }
            return result;
        }

        @Override
        public List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds) {
            return new ArrayList<>();
        }

        @Override
        public List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds) {
            return new ArrayList<>();
        }

        @Override
        public List<AdminUserRespDTO> getUserListByNickname(String nickname) {
            return new ArrayList<>();
        }

        @Override
        public void validateUserList(Collection<Long> ids) {
            for (Long id : ids) {
                AdminUserRespDTO user = users.get(id);
                if (user == null) {
                    throw exception(USER_NOT_EXISTS);
                }
                if (CommonStatusEnum.isDisable(user.getStatus())) {
                    throw exception(USER_IS_DISABLE, user.getNickname());
                }
            }
        }

    }

    static class TestDeptApi implements DeptApi {

        private final Map<Long, DeptRespDTO> depts = new ConcurrentHashMap<>();

        void clear() {
            depts.clear();
        }

        void putDept(Long id, String name) {
            DeptRespDTO dept = new DeptRespDTO();
            dept.setId(id);
            dept.setName(name);
            dept.setStatus(CommonStatusEnum.ENABLE.getStatus());
            depts.put(id, dept);
        }

        @Override
        public DeptRespDTO getDept(Long id) {
            return depts.get(id);
        }

        @Override
        public List<DeptRespDTO> getDeptList(Collection<Long> ids) {
            List<DeptRespDTO> result = new ArrayList<>();
            for (Long id : ids) {
                DeptRespDTO dept = depts.get(id);
                if (dept != null) {
                    result.add(dept);
                }
            }
            return result;
        }

        @Override
        public void validateDeptList(Collection<Long> ids) {
        }

        @Override
        public List<DeptRespDTO> getChildDeptList(Long id) {
            return Collections.emptyList();
        }

    }

}

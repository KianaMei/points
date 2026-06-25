package cn.iocoder.yudao.module.clubpoints.dal.mysql.club;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubLeaderDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubMemberDO;
import cn.iocoder.yudao.module.clubpoints.dal.dataobject.club.ClubPointClubDO;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClubPointClubMapperTest extends BaseDbUnitTest {

    @Resource
    private ClubPointClubMapper clubMapper;
    @Resource
    private ClubMemberMapper memberMapper;
    @Resource
    private ClubLeaderMapper leaderMapper;

    @Test
    void clubMappersShouldPersistClubMemberAndLeaderSnapshots() {
        ClubPointClubDO club = buildClub();
        clubMapper.insert(club);

        ClubPointClubDO savedClub = clubMapper.selectByCode("CLUB-M5-001");
        assertNotNull(savedClub);
        assertEquals("篮球俱乐部", savedClub.getName());
        assertEquals(1, savedClub.getStatus());
        assertEquals("面向篮球爱好者", savedClub.getDescription());
        assertEquals("联系人：张三", savedClub.getContactText());
        assertEquals(1000L, savedClub.getCoverFileId());
        assertEquals(10, savedClub.getSort());
        assertEquals(LocalDateTime.of(2026, 6, 2, 10, 0), savedClub.getDisabledTime());
        assertEquals("场地维护", savedClub.getDisabledReason());
        assertEquals("主数据备注", savedClub.getRemark());

        ClubMemberDO member = buildMember(savedClub.getId());
        memberMapper.insert(member);
        ClubMemberDO savedMember = memberMapper.selectByUserIdAndClubIdAndStatus(200L, savedClub.getId(), 1);
        assertNotNull(savedMember);
        assertEquals(20L, savedMember.getDeptIdSnapshot());
        assertEquals("员工A", savedMember.getUserNameSnapshot());
        assertEquals("综合部", savedMember.getDeptNameSnapshot());
        assertEquals("13800000000", savedMember.getMobileSnapshot());
        assertEquals("CLUB-M5-001", savedMember.getClubCodeSnapshot());
        assertEquals("篮球俱乐部", savedMember.getClubNameSnapshot());
        assertEquals(LocalDateTime.of(2026, 6, 1, 9, 0), savedMember.getJoinTime());
        assertEquals("1:200", savedMember.getActiveUniqueKey());

        ClubLeaderDO leader = buildLeader(savedClub.getId());
        leaderMapper.insert(leader);
        ClubLeaderDO savedLeader = leaderMapper.selectByUserIdAndClubIdAndStatus(300L, savedClub.getId(), 1);
        assertNotNull(savedLeader);
        assertEquals(LocalDateTime.of(2026, 6, 1, 10, 0), savedLeader.getAssignedTime());
        assertEquals(900L, savedLeader.getAssignedBy());
        assertEquals("任命负责运营", savedLeader.getReason());
        assertEquals("篮球俱乐部", savedLeader.getClubNameSnapshot());
        assertEquals("负责人A", savedLeader.getUserNameSnapshot());
        assertEquals("1:300", savedLeader.getActiveUniqueKey());
    }

    private static ClubPointClubDO buildClub() {
        return new ClubPointClubDO()
                .setCode("CLUB-M5-001")
                .setName("篮球俱乐部")
                .setStatus(1)
                .setDescription("面向篮球爱好者")
                .setContactText("联系人：张三")
                .setCoverFileId(1000L)
                .setSort(10)
                .setDisabledTime(LocalDateTime.of(2026, 6, 2, 10, 0))
                .setDisabledReason("场地维护")
                .setRemark("主数据备注");
    }

    private static ClubMemberDO buildMember(Long clubId) {
        return new ClubMemberDO()
                .setClubId(clubId)
                .setUserId(200L)
                .setDeptIdSnapshot(20L)
                .setUserNameSnapshot("员工A")
                .setDeptNameSnapshot("综合部")
                .setMobileSnapshot("13800000000")
                .setClubCodeSnapshot("CLUB-M5-001")
                .setClubNameSnapshot("篮球俱乐部")
                .setStatus(1)
                .setJoinTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .setActiveUniqueKey("1:200");
    }

    private static ClubLeaderDO buildLeader(Long clubId) {
        return new ClubLeaderDO()
                .setClubId(clubId)
                .setUserId(300L)
                .setStatus(1)
                .setAssignedTime(LocalDateTime.of(2026, 6, 1, 10, 0))
                .setAssignedBy(900L)
                .setReason("任命负责运营")
                .setClubNameSnapshot("篮球俱乐部")
                .setUserNameSnapshot("负责人A")
                .setActiveUniqueKey("1:300");
    }

}

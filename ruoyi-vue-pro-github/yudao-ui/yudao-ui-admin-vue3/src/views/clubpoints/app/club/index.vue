<template>
  <ContentWrap>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="我的俱乐部" name="mine">
        <el-table v-loading="myLoading" :data="myClubs">
          <el-table-column label="俱乐部" min-width="180" prop="name" />
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_CLUB_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="介绍" min-width="220" prop="description" show-overflow-tooltip />
          <el-table-column align="center" label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="openMemberDialog(row)">成员</el-button>
              <el-button
                v-hasPermi="['clubpoints:club-member:exit']"
                link
                type="danger"
                @click="exitClub(row)"
              >
                退出
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="可加入俱乐部" name="joinable">
        <el-form :inline="true" :model="joinableQuery" class="-mb-15px" label-width="88px">
          <el-form-item label="关键词">
            <el-input
              v-model="joinableQuery.keyword"
              class="!w-240px"
              clearable
              placeholder="俱乐部名称或编号"
              @keyup.enter="getJoinableList"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="getJoinableList">
              <Icon class="mr-5px" icon="ep:search" />搜索
            </el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="joinableLoading" :data="joinableList" class="mt-20px">
          <el-table-column label="俱乐部" min-width="180" prop="name" />
          <el-table-column label="介绍" min-width="260" prop="description" show-overflow-tooltip />
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_CLUB_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="120">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['clubpoints:club-member:join']"
                link
                type="primary"
                @click="joinClub(row)"
              >
                加入
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="joinableQuery.pageSize"
          v-model:page="joinableQuery.pageNo"
          :total="joinableTotal"
          @pagination="getJoinableList"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <Dialog v-model="memberDialogVisible" :title="`${currentClub?.name || ''} - 成员名单`" width="900">
    <el-table v-loading="memberLoading" :data="memberList">
      <el-table-column label="员工" min-width="160" prop="userNameSnapshot">
        <template #default="{ row }">{{ row.userNameSnapshot || row.nickname || row.userId }}</template>
      </el-table-column>
      <el-table-column label="部门" min-width="140" prop="deptNameSnapshot" />
      <el-table-column label="联系方式" min-width="140" prop="mobileSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_MEMBER_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="加入时间"
        prop="joinTime"
        width="180"
      />
    </el-table>
    <Pagination
      v-model:limit="memberQuery.pageSize"
      v-model:page="memberQuery.pageNo"
      :total="memberTotal"
      @pagination="getMemberList"
    />
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ClubApi from '@/api/clubpoints/app/club'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAppClub' })

const message = useMessage()

const activeTab = ref('mine')
const myLoading = ref(false)
const myClubs = ref<ClubApi.AppClubRespVO[]>([])

const joinableLoading = ref(false)
const joinableList = ref<ClubApi.AppClubRespVO[]>([])
const joinableTotal = ref(0)
const joinableQuery = reactive({ pageNo: 1, pageSize: 10, keyword: undefined as string | undefined })

const memberDialogVisible = ref(false)
const currentClub = ref<ClubApi.AppClubRespVO>()
const memberLoading = ref(false)
const memberList = ref<ClubApi.AppClubMemberRespVO[]>([])
const memberTotal = ref(0)
const memberQuery = reactive({ pageNo: 1, pageSize: 10, clubId: undefined as number | undefined })

const getMyClubs = async () => {
  myLoading.value = true
  try {
    myClubs.value = await ClubApi.getMyClubList()
  } finally {
    myLoading.value = false
  }
}

const getJoinableList = async () => {
  joinableLoading.value = true
  try {
    const data = await ClubApi.getJoinableClubPage(joinableQuery)
    joinableList.value = data.list || []
    joinableTotal.value = data.total || 0
  } finally {
    joinableLoading.value = false
  }
}

const joinClub = async (row: ClubApi.AppClubRespVO) => {
  try {
    const result = await message.prompt(`请输入加入 ${row.name} 的原因，可留空`, '加入俱乐部')
    await ClubApi.joinClub({ id: row.id, reason: result.value })
    message.success('加入申请已提交')
    await Promise.all([getMyClubs(), getJoinableList()])
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      message.error('加入俱乐部失败，请重试')
    }
  }
}

const exitClub = async (row: ClubApi.AppClubRespVO) => {
  try {
    await message.confirm('退出后该俱乐部未开始或可取消的报名会自动取消，不产生缺席扣分。确认继续？')
    const result = await message.prompt(`请输入退出 ${row.name} 的原因`, '退出俱乐部')
    await ClubApi.exitClub({ id: row.id, reason: result.value })
    message.success('已退出俱乐部')
    await getMyClubs()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      message.error('退出俱乐部失败，请重试')
    }
  }
}

const openMemberDialog = async (row: ClubApi.AppClubRespVO) => {
  currentClub.value = row
  memberQuery.clubId = row.id
  memberQuery.pageNo = 1
  memberDialogVisible.value = true
  await getMemberList()
}

const getMemberList = async () => {
  if (!memberQuery.clubId) {
    return
  }
  memberLoading.value = true
  try {
    const data = await ClubApi.getClubMemberPage(memberQuery as ClubApi.AppClubMemberPageReqVO)
    memberList.value = data.list || []
    memberTotal.value = data.total || 0
  } finally {
    memberLoading.value = false
  }
}

onMounted(() => {
  getMyClubs()
  getJoinableList()
})
</script>

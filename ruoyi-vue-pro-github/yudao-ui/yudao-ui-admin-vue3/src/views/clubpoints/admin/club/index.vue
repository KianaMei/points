<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="俱乐部名称" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          placeholder="请输入俱乐部名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-200px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_CLUB_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
        <el-button
          v-hasPermi="['clubpoints:club:create']"
          plain
          type="primary"
          @click="openClubForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />新增俱乐部
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="编号" prop="code" width="140" />
      <el-table-column label="俱乐部名称" min-width="180" prop="name" />
      <el-table-column label="介绍" min-width="220" prop="description" show-overflow-tooltip />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_CLUB_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
        prop="createTime"
        width="180"
      />
      <el-table-column align="center" fixed="right" label="操作" width="360">
        <template #default="{ row }">
          <el-button link type="primary" @click="openMemberDialog(row)">成员</el-button>
          <el-button link type="primary" @click="openLeaderDialog(row)">负责人</el-button>
          <el-button
            v-hasPermi="['clubpoints:club:update']"
            link
            type="primary"
            @click="openClubForm('update', row)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:club:disable']"
            link
            type="warning"
            @click="disableClub(row)"
          >
            停用
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:club:delete']"
            link
            type="danger"
            @click="openDeleteDialog(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog v-model="clubFormVisible" :title="clubFormType === 'create' ? '新增俱乐部' : '编辑俱乐部'" width="640">
    <el-form ref="clubFormRef" :model="clubForm" :rules="clubRules" label-width="96px">
      <el-form-item label="俱乐部编号" prop="code">
        <el-input v-model="clubForm.code" placeholder="创建时填写，历史快照使用" />
      </el-form-item>
      <el-form-item label="俱乐部名称" prop="name">
        <el-input v-model="clubForm.name" placeholder="请输入俱乐部名称" />
      </el-form-item>
      <el-form-item label="介绍">
        <el-input v-model="clubForm.description" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item v-if="clubFormType === 'update'" label="修改原因">
        <el-input v-model="clubForm.reason" :rows="2" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="clubFormVisible = false">取消</el-button>
      <el-button :loading="clubSubmitLoading" type="primary" @click="submitClubForm">保存</el-button>
    </template>
  </Dialog>

  <Dialog v-model="memberDialogVisible" :title="`${currentClub?.name || ''} - 成员`" width="900">
    <el-form :inline="true" :model="memberQuery" class="-mb-15px">
      <el-form-item label="员工">
        <UserPicker v-model="memberForm.userId" class="!w-240px" />
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="memberForm.reason" class="!w-240px" placeholder="添加成员原因" />
      </el-form-item>
      <el-form-item>
        <el-button
          v-hasPermi="['clubpoints:club-member:add']"
          type="primary"
          @click="addMember"
        >
          添加成员
        </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="memberLoading" :data="memberList" class="mt-20px">
      <el-table-column label="员工" min-width="160" prop="userNameSnapshot" />
      <el-table-column label="部门" min-width="160" prop="deptNameSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_MEMBER_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['clubpoints:club-member:remove']"
            link
            type="danger"
            @click="removeMember(row)"
          >
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="memberQuery.pageSize"
      v-model:page="memberQuery.pageNo"
      :total="memberTotal"
      @pagination="getMemberList"
    />
  </Dialog>

  <Dialog v-model="leaderDialogVisible" :title="`${currentClub?.name || ''} - 负责人`" width="900">
    <el-form :inline="true" class="-mb-15px">
      <el-form-item label="负责人">
        <UserPicker v-model="leaderForm.userId" class="!w-240px" />
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="leaderForm.reason" class="!w-240px" placeholder="设置负责人原因" />
      </el-form-item>
      <el-form-item>
        <el-button
          v-hasPermi="['clubpoints:club-leader:update']"
          type="primary"
          @click="assignLeader"
        >
          设置负责人
        </el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="leaderLoading" :data="leaderList" class="mt-20px">
      <el-table-column label="负责人" min-width="160" prop="userNameSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_LEADER_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['clubpoints:club-leader:update']"
            link
            type="danger"
            @click="removeLeader(row)"
          >
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="leaderQuery.pageSize"
      v-model:page="leaderQuery.pageNo"
      :total="leaderTotal"
      @pagination="getLeaderList"
    />
  </Dialog>

  <StrongConfirmDialog ref="deleteDialogRef" @confirm="deleteClub" />
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ClubApi from '@/api/clubpoints/admin/club'
import type { StrongConfirmPayload } from '@/api/clubpoints/shared/types'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'
import StrongConfirmDialog from '@/views/clubpoints/components/StrongConfirmDialog.vue'
import UserPicker from '@/views/clubpoints/components/UserPicker.vue'

defineOptions({ name: 'ClubPointsAdminClub' })

const message = useMessage()

const loading = ref(false)
const list = ref<ClubApi.AdminClubRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  status: undefined as number | undefined
})

const clubFormVisible = ref(false)
const clubFormType = ref<'create' | 'update'>('create')
const clubSubmitLoading = ref(false)
const clubFormRef = ref()
const clubForm = reactive<ClubApi.AdminClubSaveReqVO>({
  name: '',
  code: '',
  description: '',
  reason: ''
})
const clubRules = {
  name: [{ required: true, message: '俱乐部名称不能为空', trigger: 'blur' }]
}

const currentClub = ref<ClubApi.AdminClubRespVO>()
const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref<any[]>([])
const memberTotal = ref(0)
const memberQuery = reactive({ pageNo: 1, pageSize: 10, clubId: undefined as number | undefined })
const memberForm = reactive({ userId: undefined as number | undefined, reason: '' })

const leaderDialogVisible = ref(false)
const leaderLoading = ref(false)
const leaderList = ref<any[]>([])
const leaderTotal = ref(0)
const leaderQuery = reactive({ pageNo: 1, pageSize: 10, clubId: undefined as number | undefined })
const leaderForm = reactive({ userId: undefined as number | undefined, reason: '' })

const deleteDialogRef = ref()
const deletingClub = ref<ClubApi.AdminClubRespVO>()

const getList = async () => {
  loading.value = true
  try {
    const data = await ClubApi.getClubPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openClubForm = (type: 'create' | 'update', row?: ClubApi.AdminClubRespVO) => {
  clubFormType.value = type
  Object.assign(clubForm, {
    id: row?.id,
    name: row?.name || '',
    code: row?.code || '',
    description: row?.description || '',
    reason: ''
  })
  clubFormVisible.value = true
}

const submitClubForm = async () => {
  await clubFormRef.value?.validate()
  clubSubmitLoading.value = true
  try {
    if (clubFormType.value === 'create') {
      await ClubApi.createClub(clubForm)
    } else {
      await ClubApi.updateClub(clubForm)
    }
    message.success('俱乐部已保存')
    clubFormVisible.value = false
    await getList()
  } finally {
    clubSubmitLoading.value = false
  }
}

const disableClub = async (row: ClubApi.AdminClubRespVO) => {
  try {
    const result = await message.prompt(`请输入停用 ${row.name} 的原因`, '停用俱乐部')
    await ClubApi.disableClub({ id: row.id, reason: result.value })
    message.success('停用成功')
    await getList()
  } catch {}
}

const openDeleteDialog = (row: ClubApi.AdminClubRespVO) => {
  deletingClub.value = row
  deleteDialogRef.value.open({ targetName: row.name })
}

const deleteClub = async (payload: StrongConfirmPayload) => {
  if (!deletingClub.value) {
    return
  }
  await ClubApi.deleteClub({
    id: deletingClub.value.id,
    reason: payload.reason,
    strongConfirm: {
      confirmText: payload.confirmText,
      confirmedAt: payload.confirmedAt
    }
  })
  message.success('删除成功')
  await getList()
}

const openMemberDialog = async (row: ClubApi.AdminClubRespVO) => {
  currentClub.value = row
  memberQuery.clubId = row.id
  memberQuery.pageNo = 1
  memberForm.userId = undefined
  memberForm.reason = ''
  memberDialogVisible.value = true
  await getMemberList()
}

const getMemberList = async () => {
  memberLoading.value = true
  try {
    const data = await ClubApi.getClubMemberPage(memberQuery)
    memberList.value = data.list || []
    memberTotal.value = data.total || 0
  } finally {
    memberLoading.value = false
  }
}

const addMember = async () => {
  if (!currentClub.value || !memberForm.userId) {
    message.error('请选择员工')
    return
  }
  await ClubApi.addClubMember({
    clubId: currentClub.value.id,
    userId: memberForm.userId,
    reason: memberForm.reason
  })
  message.success('添加成员成功')
  memberForm.userId = undefined
  memberForm.reason = ''
  await getMemberList()
}

const removeMember = async (row: any) => {
  try {
    const result = await message.prompt('请输入移除成员原因', '移除成员')
    await ClubApi.removeClubMember({
      clubId: currentClub.value!.id,
      userId: row.userId,
      reason: result.value
    })
    message.success('移除成员成功')
    await getMemberList()
  } catch {}
}

const openLeaderDialog = async (row: ClubApi.AdminClubRespVO) => {
  currentClub.value = row
  leaderQuery.clubId = row.id
  leaderQuery.pageNo = 1
  leaderForm.userId = undefined
  leaderForm.reason = ''
  leaderDialogVisible.value = true
  await getLeaderList()
}

const getLeaderList = async () => {
  leaderLoading.value = true
  try {
    const data = await ClubApi.getClubLeaderPage(leaderQuery)
    leaderList.value = data.list || []
    leaderTotal.value = data.total || 0
  } finally {
    leaderLoading.value = false
  }
}

const assignLeader = async () => {
  if (!currentClub.value || !leaderForm.userId) {
    message.error('请选择负责人')
    return
  }
  await ClubApi.assignClubLeader({
    clubId: currentClub.value.id,
    userId: leaderForm.userId,
    reason: leaderForm.reason
  })
  message.success('设置负责人成功')
  leaderForm.userId = undefined
  leaderForm.reason = ''
  await getLeaderList()
}

const removeLeader = async (row: any) => {
  try {
    const result = await message.prompt('请输入移除负责人原因', '移除负责人')
    await ClubApi.removeClubLeader({
      clubId: currentClub.value!.id,
      userId: row.userId,
      reason: result.value
    })
    message.success('移除负责人成功')
    await getLeaderList()
  } catch {}
}

onMounted(getList)
</script>

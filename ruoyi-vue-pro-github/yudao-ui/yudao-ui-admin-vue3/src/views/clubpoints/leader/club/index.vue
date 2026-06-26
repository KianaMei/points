<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="负责人只能修改自己负责俱乐部基础信息，不能创建、停用、删除俱乐部，也不能设置负责人或移除成员。"
      type="info"
    />
    <el-table v-loading="loading" :data="clubList">
      <el-table-column label="俱乐部" min-width="180" prop="name" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_CLUB_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="介绍" min-width="220" prop="description" show-overflow-tooltip />
      <el-table-column label="联系方式" min-width="180" prop="contactText" show-overflow-tooltip />
      <el-table-column align="center" label="操作" width="180">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:club-member:query']" link type="primary" @click="openMemberDialog(row)">
            成员
          </el-button>
          <el-button v-hasPermi="['clubpoints:club:update']" link type="primary" @click="openEditDialog(row)">
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <Dialog v-model="editVisible" title="修改负责俱乐部" width="640">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="96px">
      <el-form-item label="俱乐部名称" prop="name">
        <el-input v-model="formData.name" />
      </el-form-item>
      <el-form-item label="介绍">
        <el-input v-model="formData.description" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item label="联系方式">
        <el-input v-model="formData.contactText" />
      </el-form-item>
      <el-form-item label="封面文件ID">
        <el-input v-model="formData.coverFileId" placeholder="可选，复用 infra 文件 ID" />
      </el-form-item>
      <el-form-item label="修改原因" prop="reason">
        <el-input v-model="formData.reason" :rows="2" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitForm">保存</el-button>
    </template>
  </Dialog>

  <Dialog v-model="memberVisible" :title="`${currentClub?.name || ''} - 成员`" width="900">
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
import * as ClubApi from '@/api/clubpoints/leader/club'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsLeaderClub' })

const message = useMessage()
const loading = ref(false)
const clubList = ref<ClubApi.LeaderClubRespVO[]>([])
const editVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<ClubApi.LeaderClubSaveReqVO>({
  id: 0,
  name: '',
  description: '',
  contactText: '',
  coverFileId: undefined,
  reason: ''
})
const formRules = {
  name: [{ required: true, message: '俱乐部名称不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '修改原因不能为空', trigger: 'blur' }]
}

const currentClub = ref<ClubApi.LeaderClubRespVO>()
const memberVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref<ClubApi.LeaderMemberRespVO[]>([])
const memberTotal = ref(0)
const memberQuery = reactive({ pageNo: 1, pageSize: 10, clubId: undefined as number | undefined })

const getClubList = async () => {
  loading.value = true
  try {
    clubList.value = await ClubApi.getMyManagedClubList()
  } finally {
    loading.value = false
  }
}

const openEditDialog = async (row: ClubApi.LeaderClubRespVO) => {
  const detail = await ClubApi.getLeaderClub(row.id)
  Object.assign(formData, {
    id: detail.id,
    name: detail.name,
    description: detail.description || '',
    contactText: detail.contactText || '',
    coverFileId: detail.coverFileId,
    reason: ''
  })
  editVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await ClubApi.updateLeaderClub(formData)
    message.success('保存成功')
    editVisible.value = false
    await getClubList()
  } finally {
    submitLoading.value = false
  }
}

const openMemberDialog = async (row: ClubApi.LeaderClubRespVO) => {
  currentClub.value = row
  memberQuery.clubId = row.id
  memberQuery.pageNo = 1
  memberVisible.value = true
  await getMemberList()
}

const getMemberList = async () => {
  if (!memberQuery.clubId) {
    return
  }
  memberLoading.value = true
  try {
    const data = await ClubApi.getLeaderMemberPage(memberQuery)
    memberList.value = data.list || []
    memberTotal.value = data.total || 0
  } finally {
    memberLoading.value = false
  }
}

onMounted(getClubList)
</script>

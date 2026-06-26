<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="负责人只能管理自己负责俱乐部的活动；取消活动只做普通确认，不使用强确认。"
      type="info"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="负责俱乐部" prop="clubId">
        <div class="!w-240px">
          <ClubSelect v-model="queryParams.clubId" :options="clubOptions" />
        </div>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS)"
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
        <el-button v-hasPermi="['clubpoints:activity:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新建活动
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="标题" min-width="220" prop="title" />
      <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="开始时间" prop="startTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="240">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:activity:update']" link type="primary" @click="openForm('update', row)">
            编辑
          </el-button>
          <el-button v-hasPermi="['clubpoints:activity:submit']" link type="success" @click="submitActivity(row)">
            提交
          </el-button>
          <el-button v-hasPermi="['clubpoints:activity:cancel']" link type="danger" @click="cancelActivity(row)">
            取消
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新建活动' : '编辑活动'" width="760">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="112px">
      <el-form-item label="负责俱乐部" prop="clubId">
        <ClubSelect v-model="formData.clubId" :options="clubOptions" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" />
      </el-form-item>
      <el-form-item label="说明" prop="description">
        <el-input v-model="formData.description" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item label="地点">
        <el-input v-model="formData.location" />
      </el-form-item>
      <el-form-item label="活动时间" prop="startTime">
        <el-date-picker
          v-model="activityTimeRange"
          class="!w-full"
          end-placeholder="结束时间"
          start-placeholder="开始时间"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="规则版本" prop="ruleVersionId">
        <el-input-number v-model="formData.ruleVersionId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="基础积分" prop="basePoints">
        <el-input-number v-model="formData.basePoints" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="全程额外分">
        <el-input-number v-model="formData.fullExtraPoints" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="formData.reason" :rows="2" type="textarea" />
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="formData.attachments" directory="clubpoints/activity" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitForm">保存</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ActivityApi from '@/api/clubpoints/leader/activity'
import * as ClubApi from '@/api/clubpoints/leader/club'
import type { ClubPointClubOption } from '@/api/clubpoints/shared/types'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import ClubSelect from '@/views/clubpoints/components/ClubSelect.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsLeaderActivity' })

const message = useMessage()
const loading = ref(false)
const list = ref<ActivityApi.LeaderActivityRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  clubId: undefined as number | undefined,
  status: undefined as number | undefined
})
const clubOptions = ref<ClubPointClubOption[]>([])

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const activityTimeRange = ref<string[]>([])
const formData = reactive<ActivityApi.LeaderActivitySaveReqVO>({
  clubId: undefined as unknown as number,
  title: '',
  description: '',
  location: '',
  ruleVersionId: undefined as unknown as number,
  basePoints: undefined as unknown as number,
  fullExtraPoints: undefined,
  reason: '',
  attachments: []
})
const formRules = {
  clubId: [{ required: true, message: '俱乐部不能为空', trigger: 'change' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '说明不能为空', trigger: 'blur' }],
  ruleVersionId: [{ required: true, message: '规则版本不能为空', trigger: 'blur' }],
  basePoints: [{ required: true, message: '基础积分不能为空', trigger: 'blur' }]
}

const loadClubOptions = async () => {
  if (clubOptions.value.length > 0) {
    return
  }
  const clubs = await ClubApi.getMyManagedClubList()
  clubOptions.value = clubs.map((club) => ({
    id: club.id,
    name: club.name,
    code: club.code,
    status: club.status
  }))
}

const ensureDefaultClub = async () => {
  await loadClubOptions()
  if (!queryParams.clubId && clubOptions.value.length > 0) {
    queryParams.clubId = clubOptions.value[0].id
  }
}

const getList = async () => {
  loading.value = true
  try {
    await ensureDefaultClub()
    if (!queryParams.clubId) {
      list.value = []
      total.value = 0
      return
    }
    const data = await ActivityApi.getLeaderActivityPage(queryParams)
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

const openForm = async (type: 'create' | 'update', row?: ActivityApi.LeaderActivityRespVO) => {
  formType.value = type
  await loadClubOptions()
  const detail = row ? await ActivityApi.getLeaderActivity(row.id) : undefined
  Object.assign(formData, {
    id: detail?.id,
    clubId: detail?.clubId,
    title: detail?.title || '',
    description: detail?.description || '',
    location: detail?.location || '',
    ruleVersionId: detail?.ruleVersionId,
    basePoints: detail?.basePoints,
    fullExtraPoints: detail?.fullExtraPoints,
    reason: '',
    attachments: detail?.attachments || []
  })
  activityTimeRange.value = detail?.startTime && detail?.endTime ? [detail.startTime, detail.endTime] : []
  formVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (activityTimeRange.value.length === 2 && activityTimeRange.value[0] >= activityTimeRange.value[1]) {
    message.error('开始时间必须早于结束时间')
    return
  }
  formData.startTime = activityTimeRange.value[0]
  formData.endTime = activityTimeRange.value[1]
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await ActivityApi.createLeaderActivity(formData)
    } else {
      await ActivityApi.updateLeaderActivity(formData)
    }
    message.success('活动已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const submitActivity = async (row: ActivityApi.LeaderActivityRespVO) => {
  try {
    const result = await message.prompt('请输入提交审核原因', '提交活动')
    await ActivityApi.submitLeaderActivity({ id: row.id, reason: result.value })
    message.success('已提交审核')
    await getList()
  } catch {}
}

const cancelActivity = async (row: ActivityApi.LeaderActivityRespVO) => {
  try {
    const result = await message.prompt('请输入取消原因', '取消活动')
    await ActivityApi.cancelLeaderActivity({ id: row.id, reason: result.value })
    message.success('已取消')
    await getList()
  } catch {}
}

onMounted(getList)
</script>

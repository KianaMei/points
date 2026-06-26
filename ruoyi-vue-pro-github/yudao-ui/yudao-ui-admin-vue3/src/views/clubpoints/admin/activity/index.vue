<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="管理员可以直接发布活动，也可以审核负责人提交的活动；审核只能通过或驳回，驳回必须填写原因。"
      type="info"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
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
      <el-form-item label="俱乐部ID" prop="clubId">
        <el-input-number v-model="queryParams.clubId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
        <el-button v-hasPermi="['clubpoints:activity:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新增活动
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="活动ID" prop="id" width="100" />
      <el-table-column label="标题" min-width="220" prop="title" />
      <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="开始时间" prop="startTime" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="结束时间" prop="endTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="360">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:activity:update']" link type="primary" @click="openForm('update', row)">
            编辑
          </el-button>
          <el-button v-hasPermi="['clubpoints:activity:publish']" link type="success" @click="publishActivity(row)">
            发布
          </el-button>
          <el-button v-hasPermi="['clubpoints:activity:review']" link type="primary" @click="openReview(row)">
            审核
          </el-button>
          <el-button v-hasPermi="['clubpoints:activity:cancel']" link type="warning" @click="cancelActivity(row)">
            取消
          </el-button>
          <el-button v-hasPermi="['clubpoints:activity:delete']" link type="danger" @click="deleteActivity(row)">
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新增活动' : '编辑活动'" width="760">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="112px">
      <el-form-item label="俱乐部" prop="clubId">
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

  <ReviewDialog ref="reviewRef" title="活动审核" @submit="submitReview" />
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ActivityApi from '@/api/clubpoints/admin/activity'
import * as ClubApi from '@/api/clubpoints/admin/club'
import type { ClubPointClubOption, ReviewReqVO } from '@/api/clubpoints/shared/types'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import ClubSelect from '@/views/clubpoints/components/ClubSelect.vue'
import ReviewDialog from '@/views/clubpoints/components/ReviewDialog.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminActivity' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  clubId: undefined as number | undefined
})

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const activityTimeRange = ref<string[]>([])
const clubOptions = ref<ClubPointClubOption[]>([])
const formData = reactive<ActivityApi.AdminActivitySaveReqVO>({
  clubId: undefined as unknown as number,
  title: '',
  description: '',
  location: '',
  ruleVersionId: undefined as unknown as number,
  reason: '',
  attachments: []
})
const formRules = {
  clubId: [{ required: true, message: '俱乐部不能为空', trigger: 'change' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '说明不能为空', trigger: 'blur' }],
  ruleVersionId: [{ required: true, message: '规则版本不能为空', trigger: 'blur' }]
}
const reviewRef = ref()

const loadClubOptions = async () => {
  const data = await ClubApi.getClubPage({ pageNo: 1, pageSize: 100 })
  clubOptions.value = (data.list || []).map((club) => ({
    id: club.id,
    name: club.name,
    code: club.code,
    status: club.status
  }))
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ActivityApi.getAdminActivityPage(queryParams)
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

const openForm = async (type: 'create' | 'update', row?: any) => {
  formType.value = type
  await loadClubOptions()
  Object.assign(formData, {
    id: row?.id,
    clubId: row?.clubId,
    title: row?.title || '',
    description: row?.description || '',
    location: row?.location || '',
    ruleVersionId: row?.ruleVersionId,
    reason: '',
    attachments: row?.attachments || []
  })
  activityTimeRange.value = row?.startTime && row?.endTime ? [row.startTime, row.endTime] : []
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
      await ActivityApi.createAdminActivity(formData)
    } else {
      await ActivityApi.updateAdminActivity(formData)
    }
    message.success('活动已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const publishActivity = async (row: any) => {
  try {
    const result = await message.prompt(`请输入发布活动 ${row.title || row.id} 的原因`, '直接发布')
    await ActivityApi.publishAdminActivity({ id: row.id, reason: result.value })
    message.success('已发布')
    await getList()
  } catch {}
}

const openReview = (row: any) => {
  reviewRef.value?.open({ id: row.id, subjectName: row.title, approved: true })
}

const submitReview = async (payload: ReviewReqVO) => {
  await ActivityApi.reviewAdminActivity(payload)
  message.success('审核已提交')
  await getList()
}

const cancelActivity = async (row: any) => {
  try {
    const result = await message.prompt(`请输入取消活动 ${row.title || row.id} 的原因`, '取消活动')
    await ActivityApi.cancelAdminActivity({ id: row.id, reason: result.value })
    message.success('已取消')
    await getList()
  } catch {}
}

const deleteActivity = async (row: any) => {
  try {
    await message.delConfirm()
    await ActivityApi.deleteAdminActivity(row.id)
    message.success('已删除')
    await getList()
  } catch {}
}

onMounted(getList)
</script>

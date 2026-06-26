<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="开启批次会生成资格快照；批次开启后资格规则不能在前端绕过修改，状态变更交给后端校验。"
      type="warning"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="年度" prop="year">
        <el-input-number v-model="queryParams.year" :min="2000" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_REDEMPTION_BATCH_STATUS)"
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
          v-hasPermi="['clubpoints:redemption-batch:manage']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />新增批次
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="批次ID" prop="id" width="100" />
      <el-table-column align="center" label="年度" prop="year" width="100" />
      <el-table-column label="批次名称" min-width="180" prop="name" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_BATCH_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="开启时间" prop="openTime" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="关闭时间" prop="closeTime" width="180" />
      <el-table-column align="center" label="最低积分" prop="minAvailablePoints" width="110" />
      <el-table-column align="center" label="资格人数" prop="qualifiedCount" width="110" />
      <el-table-column align="center" label="快照" prop="snapshotGenerated" width="100">
        <template #default="{ row }">
          <el-tag :type="row.snapshotGenerated ? 'success' : 'info'">
            {{ row.snapshotGenerated ? '已生成' : '未生成' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" fixed="right" label="操作" width="260">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['clubpoints:redemption-batch:manage']"
            link
            type="primary"
            @click="openForm('update', row)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:redemption-batch:manage']"
            link
            type="success"
            @click="changeBatchStatus(row, 'open')"
          >
            开启
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:redemption-batch:manage']"
            link
            type="warning"
            @click="changeBatchStatus(row, 'close')"
          >
            关闭
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新增兑换批次' : '编辑兑换批次'" width="760">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="128px">
      <el-form-item label="年度" prop="year">
        <el-input-number v-model="formData.year" :min="2000" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="批次名称" prop="name">
        <el-input v-model="formData.name" />
      </el-form-item>
      <el-form-item label="开放时间" prop="openTime">
        <el-date-picker
          v-model="batchTimeRange"
          class="!w-full"
          end-placeholder="关闭时间"
          start-placeholder="开启时间"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="规则版本" prop="ruleVersionId">
        <el-input-number v-model="formData.ruleVersionId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="最低可用积分" prop="minAvailablePoints">
        <el-input-number v-model="formData.minAvailablePoints" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="资格人数" prop="qualifiedCount">
        <el-input-number v-model="formData.qualifiedCount" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="并列全进" prop="includeTieAtCutoff">
        <el-switch v-model="formData.includeTieAtCutoff" />
      </el-form-item>
      <el-form-item label="资格规则" prop="qualificationRule">
        <el-input v-model="formData.qualificationRule" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="formData.description" :rows="2" type="textarea" />
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="formData.reason" :rows="2" type="textarea" />
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
import * as RedemptionApi from '@/api/clubpoints/admin/redemption'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminRedemptionBatch' })

const message = useMessage()
const currentYear = new Date().getFullYear()

const loading = ref(false)
const list = ref<RedemptionApi.AdminRedemptionBatchRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  year: undefined as number | undefined,
  status: undefined as number | undefined
})

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const batchTimeRange = ref<string[]>([])
const formData = reactive<RedemptionApi.AdminRedemptionBatchSaveReqVO>({
  year: currentYear,
  name: '',
  ruleVersionId: undefined as unknown as number,
  minAvailablePoints: 0,
  qualifiedCount: 1,
  includeTieAtCutoff: true,
  qualificationRule: '',
  description: '',
  reason: ''
})
const formRules = {
  year: [{ required: true, message: '年度不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '批次名称不能为空', trigger: 'blur' }],
  ruleVersionId: [{ required: true, message: '规则版本不能为空', trigger: 'blur' }],
  minAvailablePoints: [{ required: true, message: '最低可用积分不能为空', trigger: 'blur' }],
  qualifiedCount: [{ required: true, message: '资格人数不能为空', trigger: 'blur' }],
  qualificationRule: [{ required: true, message: '资格规则不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await RedemptionApi.getRedemptionBatchPage(queryParams)
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

const openForm = (type: 'create' | 'update', row?: RedemptionApi.AdminRedemptionBatchRespVO) => {
  formType.value = type
  Object.assign(formData, {
    id: row?.id,
    year: row?.year || currentYear,
    name: row?.name || '',
    ruleVersionId: row?.ruleVersionId,
    minAvailablePoints: row?.minAvailablePoints ?? 0,
    qualifiedCount: row?.qualifiedCount ?? 1,
    includeTieAtCutoff: row?.includeTieAtCutoff ?? true,
    qualificationRule: row?.qualificationRule || '',
    description: row?.description || '',
    reason: ''
  })
  batchTimeRange.value = row?.openTime && row?.closeTime ? [String(row.openTime), String(row.closeTime)] : []
  formVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (batchTimeRange.value.length !== 2) {
    message.error('请选择批次开放时间')
    return
  }
  if (batchTimeRange.value[0] >= batchTimeRange.value[1]) {
    message.error('开启时间必须早于关闭时间')
    return
  }
  formData.openTime = batchTimeRange.value[0]
  formData.closeTime = batchTimeRange.value[1]
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await RedemptionApi.createRedemptionBatch(formData)
    } else {
      await RedemptionApi.updateRedemptionBatch(formData)
    }
    message.success('兑换批次已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const changeBatchStatus = async (
  row: RedemptionApi.AdminRedemptionBatchRespVO,
  action: 'open' | 'close'
) => {
  const actionText = action === 'open' ? '开启' : '关闭'
  try {
    const result = await message.prompt(`请输入${actionText}批次 ${row.name} 的原因`, `${actionText}批次`)
    const data = { id: row.id, reason: result.value }
    if (action === 'open') {
      await RedemptionApi.openRedemptionBatch(data)
    } else {
      await RedemptionApi.closeRedemptionBatch(data)
    }
    message.success(`${actionText}成功`)
    await getList()
  } catch {}
}

onMounted(getList)
</script>

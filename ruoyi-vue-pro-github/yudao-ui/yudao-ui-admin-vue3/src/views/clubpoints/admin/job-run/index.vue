<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="任务类型" prop="taskType">
        <el-input v-model="queryParams.taskType" class="!w-220px" clearable placeholder="ACTIVITY_SETTLEMENT" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-input v-model="queryParams.bizType" class="!w-180px" clearable placeholder="业务类型" />
      </el-form-item>
      <el-form-item label="业务ID" prop="bizId">
        <el-input-number v-model="queryParams.bizId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="运行键" prop="runKey">
        <el-input v-model="queryParams.runKey" class="!w-220px" clearable placeholder="runKey" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_JOB_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="触发来源" prop="triggerSource">
        <el-input-number v-model="queryParams.triggerSource" :min="1" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="开始时间" prop="timeRange">
        <el-date-picker
          v-model="queryParams.timeRange"
          end-placeholder="结束时间"
          range-separator="-"
          start-placeholder="开始时间"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="运行ID" prop="id" width="100" />
      <el-table-column label="任务类型" min-width="190" prop="taskType" show-overflow-tooltip />
      <el-table-column label="业务" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.bizType || '-' }} / {{ row.bizId || '-' }}</template>
      </el-table-column>
      <el-table-column label="运行键" min-width="180" prop="runKey" show-overflow-tooltip />
      <el-table-column align="center" label="状态" prop="status" width="130">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_JOB_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="成功/失败" width="120">
        <template #default="{ row }">{{ row.successCount || 0 }} / {{ row.failedCount || 0 }}</template>
      </el-table-column>
      <el-table-column align="center" label="重试" prop="retryCount" width="90" />
      <el-table-column label="失败原因" min-width="220" prop="errorMessage" show-overflow-tooltip />
      <el-table-column :formatter="dateFormatter" align="center" label="开始时间" prop="startTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="190">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="isFailed(row)"
            v-hasPermi="['clubpoints:job:handle']"
            link
            type="warning"
            @click="handleFailedJob(row)"
          >
            重试/人工处理
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

  <Dialog v-model="detailVisible" title="任务运行详情" width="860">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="任务类型">{{ detail.taskType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="业务">{{ detail.bizType || '-' }} / {{ detail.bizId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="运行键">{{ detail.runKey || '-' }}</el-descriptions-item>
      <el-descriptions-item label="幂等键">{{ detail.idempotencyKey || '-' }}</el-descriptions-item>
      <el-descriptions-item label="总数">{{ detail.totalCount || 0 }}</el-descriptions-item>
      <el-descriptions-item label="成功/跳过/失败">
        {{ detail.successCount || 0 }} / {{ detail.skipCount || 0 }} / {{ detail.failedCount || 0 }}
      </el-descriptions-item>
      <el-descriptions-item label="下次重试">{{ detail.nextRetryTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="人工处理原因">{{ detail.manualHandleReason || '-' }}</el-descriptions-item>
      <el-descriptions-item label="错误类型">{{ detail.errorType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="错误信息">{{ detail.errorMessage || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-input class="mt-16px" :model-value="formatJson(detail.resultJson)" :rows="10" readonly type="textarea" />
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
      <el-button
        v-if="isFailed(detail)"
        v-hasPermi="['clubpoints:job:handle']"
        type="warning"
        @click="handleFailedJob(detail)"
      >
        重试/人工处理
      </el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as OperationApi from '@/api/clubpoints/admin/operation'
import type { ClubPointPageParam } from '@/api/clubpoints/shared/types'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminJobRun' })

const FAILED_STATUSES = [4, 5]
const message = useMessage()

const loading = ref(false)
const list = ref<OperationApi.AdminJobRunRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  taskType: '',
  bizType: '',
  bizId: undefined as number | undefined,
  runKey: '',
  status: undefined as number | undefined,
  triggerSource: undefined as number | undefined,
  timeRange: [] as string[]
})

const detailVisible = ref(false)
const detail = reactive<Partial<OperationApi.AdminJobRunRespVO>>({})

const buildQueryParams = (): ClubPointPageParam => {
  const params: ClubPointPageParam = {
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize
  }
  ;['taskType', 'bizType', 'bizId', 'runKey', 'status', 'triggerSource'].forEach((field) => {
    const value = queryParams[field]
    if (value !== undefined && value !== null && value !== '') {
      params[field] = value
    }
  })
  if (queryParams.timeRange?.length === 2) {
    params.startTime = queryParams.timeRange[0]
    params.endTime = queryParams.timeRange[1]
  }
  return params
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OperationApi.getJobRunPage(buildQueryParams())
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
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
  Object.assign(queryParams, {
    pageNo: 1,
    pageSize: queryParams.pageSize,
    taskType: '',
    bizType: '',
    bizId: undefined,
    runKey: '',
    status: undefined,
    triggerSource: undefined,
    timeRange: []
  })
  getList()
}

const openDetail = async (row: OperationApi.AdminJobRunRespVO) => {
  try {
    Object.assign(detail, await OperationApi.getJobRunDetail(row.id))
    detailVisible.value = true
  } catch {}
}

const isFailed = (row: Partial<OperationApi.AdminJobRunRespVO>) => FAILED_STATUSES.includes(Number(row.status))

const handleFailedJob = async (row: Partial<OperationApi.AdminJobRunRespVO>) => {
  if (!row.id) {
    return
  }
  try {
    const result = await message.prompt(`请输入任务 ${row.id} 重试或人工处理原因`, '处理失败任务')
    if (!String(result.value || '').trim()) {
      message.error('人工处理原因不能为空')
      return
    }
    await OperationApi.handleJobRun({ id: row.id, reason: result.value })
    message.success('失败任务已提交处理')
    detailVisible.value = false
    await getList()
  } catch {}
}

const formatJson = (value?: string) => {
  if (!value) {
    return ''
  }
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

onMounted(getList)
</script>

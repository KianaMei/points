<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="审计日志只读展示强审计动作，不能在此页面修改业务数据。"
      type="info"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="92px">
      <el-form-item label="动作类型" prop="actionType">
        <el-input v-model="queryParams.actionType" class="!w-200px" clearable placeholder="REPORT_EXPORT" />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-input v-model="queryParams.bizType" class="!w-180px" clearable placeholder="REPORT" />
      </el-form-item>
      <el-form-item label="业务ID" prop="bizId">
        <el-input-number v-model="queryParams.bizId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="操作人ID" prop="operatorUserId">
        <el-input-number v-model="queryParams.operatorUserId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="操作人" prop="operatorNameSnapshot">
        <el-input v-model="queryParams.operatorNameSnapshot" class="!w-180px" clearable placeholder="操作人快照" />
      </el-form-item>
      <el-form-item label="结果" prop="success">
        <el-select v-model="queryParams.success" class="!w-140px" clearable placeholder="请选择">
          <el-option :value="true" label="成功" />
          <el-option :value="false" label="失败" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间" prop="operationTimeRange">
        <el-date-picker
          v-model="queryParams.operationTimeRange"
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
      <el-table-column align="center" label="ID" prop="id" width="90" />
      <el-table-column label="动作类型" min-width="160" prop="actionType" show-overflow-tooltip />
      <el-table-column label="业务" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">{{ row.bizType || '-' }} / {{ row.bizId || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作人" min-width="170" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.operatorNameSnapshot || '-' }}（{{ row.operatorUserId || '-' }}）
        </template>
      </el-table-column>
      <el-table-column align="center" label="角色" prop="operatorRoleSnapshot" width="120" />
      <el-table-column align="center" label="结果" prop="success" width="100">
        <template #default="{ row }">
          <el-tag :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="原因" min-width="180" prop="reason" show-overflow-tooltip />
      <el-table-column :formatter="dateFormatter" align="center" label="操作时间" prop="operationTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

  <Dialog v-model="detailVisible" title="审计详情" width="860">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="动作类型">{{ detail.actionType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="业务">{{ detail.bizType || '-' }} / {{ detail.bizId || '-' }}</el-descriptions-item>
      <el-descriptions-item label="操作人">
        {{ detail.operatorNameSnapshot || '-' }}（{{ detail.operatorUserId || '-' }}）
      </el-descriptions-item>
      <el-descriptions-item label="角色">{{ detail.operatorRoleSnapshot || '-' }}</el-descriptions-item>
      <el-descriptions-item label="IP">{{ detail.clientIp || '-' }}</el-descriptions-item>
      <el-descriptions-item label="User-Agent">{{ detail.userAgent || '-' }}</el-descriptions-item>
      <el-descriptions-item label="原因" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
      <el-descriptions-item label="错误" :span="2">{{ detail.errorMessage || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-tabs class="mt-16px">
      <el-tab-pane label="目标快照">
        <el-input :model-value="formatJson(detail.targetSnapshotJson)" :rows="8" readonly type="textarea" />
      </el-tab-pane>
      <el-tab-pane label="修改前">
        <el-input :model-value="formatJson(detail.beforeJson)" :rows="8" readonly type="textarea" />
      </el-tab-pane>
      <el-tab-pane label="修改后">
        <el-input :model-value="formatJson(detail.afterJson)" :rows="8" readonly type="textarea" />
      </el-tab-pane>
    </el-tabs>
  </Dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as OperationApi from '@/api/clubpoints/admin/operation'
import type { ClubPointPageParam } from '@/api/clubpoints/shared/types'

defineOptions({ name: 'ClubPointsAdminAudit' })

const loading = ref(false)
const list = ref<OperationApi.AdminAuditRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  actionType: '',
  bizType: '',
  bizId: undefined as number | undefined,
  operatorUserId: undefined as number | undefined,
  operatorNameSnapshot: '',
  success: undefined as boolean | undefined,
  operationTimeRange: [] as string[]
})

const detailVisible = ref(false)
const detail = reactive<Partial<OperationApi.AdminAuditRespVO>>({})

const buildQueryParams = (): ClubPointPageParam => {
  const params: ClubPointPageParam = {
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize
  }
  ;['actionType', 'bizType', 'bizId', 'operatorUserId', 'operatorNameSnapshot', 'success'].forEach((field) => {
    const value = queryParams[field]
    if (value !== undefined && value !== null && value !== '') {
      params[field] = value
    }
  })
  if (queryParams.operationTimeRange?.length === 2) {
    params.operationTimeStart = queryParams.operationTimeRange[0]
    params.operationTimeEnd = queryParams.operationTimeRange[1]
  }
  return params
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OperationApi.getAuditPage(buildQueryParams())
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
    actionType: '',
    bizType: '',
    bizId: undefined,
    operatorUserId: undefined,
    operatorNameSnapshot: '',
    success: undefined,
    operationTimeRange: []
  })
  getList()
}

const openDetail = (row: OperationApi.AdminAuditRespVO) => {
  Object.assign(detail, row)
  detailVisible.value = true
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

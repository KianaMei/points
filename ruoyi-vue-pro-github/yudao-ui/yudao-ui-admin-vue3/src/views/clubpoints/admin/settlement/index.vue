<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="正常活动积分应由系统自动发放；异常补发/重跑仅用于自动任务失败、签到修正后补发等异常补偿场景。"
      type="warning"
    />

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待自动发放" name="pending">
        <el-form ref="pendingQueryFormRef" :inline="true" :model="pendingQuery" class="-mb-15px" label-width="88px">
          <el-form-item label="俱乐部名称" prop="clubName">
            <el-input v-model="pendingQuery.clubName" class="!w-220px" clearable placeholder="请输入俱乐部名称" />
          </el-form-item>
          <el-form-item label="活动标题" prop="activityTitle">
            <el-input v-model="pendingQuery.activityTitle" class="!w-220px" clearable placeholder="请输入活动标题" />
          </el-form-item>
          <el-form-item label="活动时间" prop="timeRange">
            <el-date-picker
              v-model="pendingQuery.timeRange"
              end-placeholder="结束时间"
              range-separator="-"
              start-placeholder="开始时间"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="handlePendingQuery">
              <Icon class="mr-5px" icon="ep:search" />搜索
            </el-button>
            <el-button @click="resetPendingQuery">
              <Icon class="mr-5px" icon="ep:refresh" />重置
            </el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="pendingLoading" :data="pendingList" class="mt-20px">
          <el-table-column label="活动标题" min-width="220" prop="title" show-overflow-tooltip />
          <el-table-column label="俱乐部" min-width="180" prop="clubNameSnapshot" show-overflow-tooltip />
          <el-table-column align="center" label="活动状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="开始时间" prop="startTime" width="180" />
          <el-table-column :formatter="dateFormatter" align="center" label="结束时间" prop="endTime" width="180" />
          <el-table-column label="处理说明" min-width="220">
            <template #default>
              自动任务会按规则发放参与积分、全程积分，并处理缺席扣分。
            </template>
          </el-table-column>
          <el-table-column align="center" fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['clubpoints:settlement:run']"
                link
                type="primary"
                @click="openManualDialog(row)"
              >
                异常补发/重跑
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="pendingQuery.pageSize"
          v-model:page="pendingQuery.pageNo"
          :total="pendingTotal"
          @pagination="getPendingList"
        />
      </el-tab-pane>

      <el-tab-pane label="发放记录" name="run">
        <el-form ref="runQueryFormRef" :inline="true" :model="runQuery" class="-mb-15px" label-width="88px">
          <el-form-item label="俱乐部名称" prop="clubName">
            <el-input v-model="runQuery.clubName" class="!w-220px" clearable placeholder="请输入俱乐部名称" />
          </el-form-item>
          <el-form-item label="活动标题" prop="activityTitle">
            <el-input v-model="runQuery.activityTitle" class="!w-220px" clearable placeholder="请输入活动标题" />
          </el-form-item>
          <el-form-item label="处理状态" prop="status">
            <el-select v-model="runQuery.status" class="!w-180px" clearable placeholder="请选择状态">
              <el-option label="成功" :value="3" />
              <el-option label="可重试失败" :value="4" />
              <el-option label="最终失败" :value="5" />
              <el-option label="人工处理中" :value="6" />
              <el-option label="已关闭" :value="7" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动时间" prop="timeRange">
            <el-date-picker
              v-model="runQuery.timeRange"
              end-placeholder="结束时间"
              range-separator="-"
              start-placeholder="开始时间"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleRunQuery">
              <Icon class="mr-5px" icon="ep:search" />搜索
            </el-button>
            <el-button @click="resetRunQuery">
              <Icon class="mr-5px" icon="ep:refresh" />重置
            </el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="runLoading" :data="runList" class="mt-20px">
          <el-table-column label="活动标题" min-width="220" prop="activityTitle" show-overflow-tooltip />
          <el-table-column label="俱乐部" min-width="180" prop="clubName" show-overflow-tooltip />
          <el-table-column align="center" label="处理状态" prop="status" width="130">
            <template #default="{ row }">
              <el-tag :type="runStatusTagType(row.status)">{{ runStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="触发方式" prop="triggerSource" width="120">
            <template #default="{ row }">{{ triggerSourceText(row.triggerSource) }}</template>
          </el-table-column>
          <el-table-column align="center" label="总/成功/跳过/失败" width="170">
            <template #default="{ row }">
              {{ row.registrationCount || 0 }} / {{ row.successCount || 0 }} /
              {{ row.skipCount || 0 }} / {{ row.failedCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="发放时间" prop="settlementTime" width="180" />
          <el-table-column label="失败原因" min-width="220" prop="errorMessage" show-overflow-tooltip />
          <el-table-column align="center" fixed="right" label="操作" width="110">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="runQuery.pageSize"
          v-model:page="runQuery.pageNo"
          :total="runTotal"
          @pagination="getRunList"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <Dialog v-model="manualDialogVisible" title="异常补发活动积分" width="620">
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="这是异常补偿入口。正常活动积分应由系统自动发放，不需要管理员日常手点。"
      type="warning"
    />
    <el-descriptions :column="1" border>
      <el-descriptions-item label="活动">{{ currentActivity?.title || '-' }}</el-descriptions-item>
      <el-descriptions-item label="俱乐部">{{ currentActivity?.clubNameSnapshot || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-form class="mt-16px" :model="manualForm" label-width="96px">
      <el-form-item label="补发原因">
        <el-radio-group v-model="manualForm.reasonType">
          <el-radio v-for="reason in manualReasonOptions" :key="reason" :label="reason" />
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="manualForm.reasonType === '其他'" label="原因说明">
        <el-input
          v-model="manualForm.customReason"
          maxlength="200"
          placeholder="请说明为什么需要异常补发或重跑"
          show-word-limit
          type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="manualDialogVisible = false">取消</el-button>
      <el-button :loading="manualLoading" type="primary" @click="submitManualSettlement">确认补发/重跑</el-button>
    </template>
  </Dialog>

  <Dialog v-model="detailVisible" title="活动积分发放详情" width="920">
    <el-descriptions v-if="detailData?.run" :column="2" border>
      <el-descriptions-item label="活动">{{ detailData.run.activityTitle || '-' }}</el-descriptions-item>
      <el-descriptions-item label="俱乐部">{{ detailData.run.clubName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="处理状态">
        <el-tag :type="runStatusTagType(detailData.run.status)">
          {{ runStatusText(detailData.run.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="触发方式">
        {{ triggerSourceText(detailData.run.triggerSource) }}
      </el-descriptions-item>
      <el-descriptions-item label="活动时间">
        {{ detailData.run.activityStartTime || '-' }} 至 {{ detailData.run.activityEndTime || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="发放时间">{{ detailData.run.settlementTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="总人数">{{ detailData.run.registrationCount || 0 }}</el-descriptions-item>
      <el-descriptions-item label="成功/跳过/失败">
        {{ detailData.run.successCount || 0 }} / {{ detailData.run.skipCount || 0 }} /
        {{ detailData.run.failedCount || 0 }}
      </el-descriptions-item>
      <el-descriptions-item label="失败原因">{{ detailData.run.errorMessage || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-table :data="detailData?.transactions || []" class="mt-16px">
      <el-table-column label="员工" min-width="150" prop="userNameSnapshot" show-overflow-tooltip />
      <el-table-column align="center" label="方向" prop="direction" width="100">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_TRANSACTION_DIRECTION" :value="row.direction" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="积分" prop="points" width="100">
        <template #default="{ row }">{{ formatPoints(row.direction, row.points) }}</template>
      </el-table-column>
      <el-table-column label="类型" min-width="160" prop="sourceTitleSnapshot" show-overflow-tooltip />
      <el-table-column label="原因" min-width="220" prop="reason" show-overflow-tooltip />
      <el-table-column :formatter="dateFormatter" align="center" label="发生时间" prop="occurredAt" width="180" />
    </el-table>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as SettlementApi from '@/api/clubpoints/admin/settlement'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminSettlement' })

const message = useMessage()

const activeTab = ref<'pending' | 'run'>('pending')
const pendingLoading = ref(false)
const pendingList = ref<SettlementApi.AdminSettlementPendingActivityRespVO[]>([])
const pendingTotal = ref(0)
const pendingQueryFormRef = ref()
const pendingQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  clubName: '',
  activityTitle: '',
  timeRange: [] as string[]
})

const runLoading = ref(false)
const runList = ref<SettlementApi.AdminSettlementRunRespVO[]>([])
const runTotal = ref(0)
const runQueryFormRef = ref()
const runQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  clubName: '',
  activityTitle: '',
  status: undefined as number | undefined,
  timeRange: [] as string[]
})

const manualDialogVisible = ref(false)
const manualLoading = ref(false)
const currentActivity = ref<SettlementApi.AdminSettlementPendingActivityRespVO>()
const manualReasonOptions = ['自动任务失败后补发', '签到/签退修正后重算', '活动状态异常手动处理', '其他']
const manualForm = reactive({
  reasonType: manualReasonOptions[0],
  customReason: ''
})

const detailVisible = ref(false)
const detailData = ref<SettlementApi.AdminSettlementDetailRespVO>()

const buildPendingQueryParams = (): SettlementApi.AdminSettlementPendingActivityPageReqVO => {
  const params: SettlementApi.AdminSettlementPendingActivityPageReqVO = {
    pageNo: pendingQuery.pageNo,
    pageSize: pendingQuery.pageSize
  }
  if (pendingQuery.clubName.trim()) {
    params.clubName = pendingQuery.clubName.trim()
  }
  if (pendingQuery.activityTitle.trim()) {
    params.activityTitle = pendingQuery.activityTitle.trim()
  }
  if (pendingQuery.timeRange?.length === 2) {
    params.startTime = pendingQuery.timeRange[0]
    params.endTime = pendingQuery.timeRange[1]
  }
  return params
}

const buildRunQueryParams = (): SettlementApi.AdminSettlementRunPageReqVO => {
  const params: SettlementApi.AdminSettlementRunPageReqVO = {
    pageNo: runQuery.pageNo,
    pageSize: runQuery.pageSize
  }
  if (runQuery.clubName.trim()) {
    params.clubName = runQuery.clubName.trim()
  }
  if (runQuery.activityTitle.trim()) {
    params.activityTitle = runQuery.activityTitle.trim()
  }
  if (runQuery.status !== undefined) {
    params.status = runQuery.status
  }
  if (runQuery.timeRange?.length === 2) {
    params.startTime = runQuery.timeRange[0]
    params.endTime = runQuery.timeRange[1]
  }
  return params
}

const getPendingList = async () => {
  pendingLoading.value = true
  try {
    const data = await SettlementApi.getPendingActivityPage(buildPendingQueryParams())
    pendingList.value = data.list || []
    pendingTotal.value = data.total || 0
  } finally {
    pendingLoading.value = false
  }
}

const getRunList = async () => {
  runLoading.value = true
  try {
    const data = await SettlementApi.getSettlementRunPage(buildRunQueryParams())
    runList.value = data.list || []
    runTotal.value = data.total || 0
  } finally {
    runLoading.value = false
  }
}

const handlePendingQuery = () => {
  pendingQuery.pageNo = 1
  getPendingList()
}

const resetPendingQuery = () => {
  pendingQueryFormRef.value?.resetFields()
  Object.assign(pendingQuery, {
    pageNo: 1,
    pageSize: pendingQuery.pageSize,
    clubName: '',
    activityTitle: '',
    timeRange: []
  })
  getPendingList()
}

const handleRunQuery = () => {
  runQuery.pageNo = 1
  getRunList()
}

const resetRunQuery = () => {
  runQueryFormRef.value?.resetFields()
  Object.assign(runQuery, {
    pageNo: 1,
    pageSize: runQuery.pageSize,
    clubName: '',
    activityTitle: '',
    status: undefined,
    timeRange: []
  })
  getRunList()
}

const handleTabChange = () => {
  if (activeTab.value === 'pending') {
    getPendingList()
  } else {
    getRunList()
  }
}

const openManualDialog = (row: SettlementApi.AdminSettlementPendingActivityRespVO) => {
  currentActivity.value = row
  manualForm.reasonType = manualReasonOptions[0]
  manualForm.customReason = ''
  manualDialogVisible.value = true
}

const submitManualSettlement = async () => {
  if (!currentActivity.value) {
    return
  }
  const reason =
    manualForm.reasonType === '其他' ? manualForm.customReason.trim() : manualForm.reasonType
  if (!reason) {
    message.error('请选择或填写异常补发原因')
    return
  }
  manualLoading.value = true
  try {
    const result = await SettlementApi.runSettlement({
      activityId: currentActivity.value.id,
      force: true,
      reason
    })
    const detail = await tryLoadManualResult(result)
    if (detail?.run) {
      message.success(
        `已提交异常补发/重跑：成功 ${detail.run.successCount || 0}，跳过 ${
          detail.run.skipCount || 0
        }，失败 ${detail.run.failedCount || 0}`
      )
    } else {
      message.success('异常补发/重跑已提交，发放记录已刷新')
    }
    manualDialogVisible.value = false
    activeTab.value = 'run'
    await Promise.all([getPendingList(), getRunList()])
  } finally {
    manualLoading.value = false
  }
}

const tryLoadManualResult = async (result: string) => {
  const matched = String(result || '').match(/settlementRunId=(\d+)/)
  if (!matched?.[1]) {
    return undefined
  }
  try {
    return await SettlementApi.getSettlementDetail(Number(matched[1]))
  } catch {
    return undefined
  }
}

const openDetail = async (row: SettlementApi.AdminSettlementRunRespVO) => {
  detailData.value = await SettlementApi.getSettlementDetail(row.id)
  detailVisible.value = true
}

const triggerSourceText = (source?: number) => {
  if (source === 1) {
    return '自动任务'
  }
  if (source === 2) {
    return '手动补偿'
  }
  return '-'
}

const runStatusText = (status?: number) => {
  const textMap: Record<number, string> = {
    1: '待处理',
    2: '处理中',
    3: '成功',
    4: '可重试失败',
    5: '最终失败',
    6: '人工处理中',
    7: '已关闭'
  }
  return status ? textMap[status] || '-' : '-'
}

const runStatusTagType = (status?: number) => {
  if (status === 3) {
    return 'success'
  }
  if (status === 4 || status === 6) {
    return 'warning'
  }
  if (status === 5) {
    return 'danger'
  }
  return 'info'
}

const formatPoints = (direction?: number, points?: number) => {
  if (!points) {
    return '0'
  }
  return direction === 2 ? `-${points}` : `+${points}`
}

onMounted(getPendingList)
</script>

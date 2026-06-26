<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="报表导出只在管理员页面开放；筛选条件会随导出请求提交，后端 /clubpoints/report/export-excel 写 REPORT_EXPORT 强审计。"
      type="warning"
    />
    <el-tabs v-model="activeReport" @tab-change="handleReportChange">
      <el-tab-pane
        v-for="item in reportOptions"
        :key="item.key"
        :label="item.label"
        :name="item.key"
      />
    </el-tabs>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item v-if="showField('year')" label="年度" prop="year">
        <el-input-number v-model="queryParams.year" :min="2000" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('userId')" label="员工ID" prop="userId">
        <el-input-number v-model="queryParams.userId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('clubId')" label="俱乐部ID" prop="clubId">
        <el-input-number v-model="queryParams.clubId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('batchId')" label="批次ID" prop="batchId">
        <el-input-number v-model="queryParams.batchId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('direction')" label="方向" prop="direction">
        <el-select v-model="queryParams.direction" class="!w-160px" clearable placeholder="请选择方向">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_TRANSACTION_DIRECTION)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="showField('pointCategory')" label="积分分类" prop="pointCategory">
        <el-select v-model="queryParams.pointCategory" class="!w-180px" clearable placeholder="请选择分类">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_POINT_CATEGORY)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="showField('sourceType')" label="来源类型" prop="sourceType">
        <el-input-number v-model="queryParams.sourceType" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('status')" label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_REDEMPTION_APPLICATION_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="showField('category')" label="预算分类" prop="category">
        <el-input-number v-model="queryParams.category" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('sourceId')" label="来源ID" prop="sourceId">
        <el-input-number v-model="queryParams.sourceId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item v-if="showField('timeRange')" label="时间范围" prop="timeRange">
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
        <el-button v-hasPermi="['clubpoints:report:query']" @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
        <el-button
          v-hasPermi="['clubpoints:report:export']"
          :loading="exportLoading"
          plain
          type="success"
          @click="handleExport"
        >
          <Icon class="mr-5px" icon="ep:download" />导出
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <template v-if="activeReport === 'pointDetail'">
        <el-table-column align="center" label="流水ID" prop="id" width="100" />
        <el-table-column label="流水号" min-width="180" prop="transactionNo" show-overflow-tooltip />
        <el-table-column label="员工" min-width="150" prop="userNameSnapshot" show-overflow-tooltip />
        <el-table-column align="center" label="方向" prop="direction" width="110">
          <template #default="{ row }">
            <StatusTag :type="DICT_TYPE.CLUB_POINTS_TRANSACTION_DIRECTION" :value="row.direction" />
          </template>
        </el-table-column>
        <el-table-column align="center" label="积分" prop="points" width="120">
          <template #default="{ row }">
            <PointAmount
              :direction="row.direction === 1 ? 'increase' : 'decrease'"
              :value="row.points"
            />
          </template>
        </el-table-column>
        <el-table-column label="来源" min-width="180" prop="sourceTitleSnapshot" show-overflow-tooltip />
        <el-table-column label="发放俱乐部" min-width="160" prop="issuingClubNameSnapshot" show-overflow-tooltip />
        <el-table-column :formatter="dateFormatter" align="center" label="发生时间" prop="occurredTime" width="180" />
      </template>
      <template v-else-if="activeReport === 'redemption'">
        <el-table-column align="center" label="申请ID" prop="id" width="100" />
        <el-table-column label="申请号" min-width="180" prop="applicationNo" show-overflow-tooltip />
        <el-table-column align="center" label="员工ID" prop="userId" width="110" />
        <el-table-column align="center" label="状态" prop="status" width="120">
          <template #default="{ row }">
            <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_APPLICATION_STATUS" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column align="center" label="消耗积分" prop="pointsCost" width="120" />
        <el-table-column align="center" label="数量" prop="quantity" width="100" />
        <el-table-column :formatter="dateFormatter" align="center" label="申请时间" prop="applyTime" width="180" />
        <el-table-column :formatter="dateFormatter" align="center" label="审核时间" prop="reviewTime" width="180" />
      </template>
      <template v-else-if="activeReport === 'ledgerSummary'">
        <el-table-column align="center" label="员工ID" prop="userId" width="110" />
        <el-table-column label="员工" min-width="150" prop="userNameSnapshot" show-overflow-tooltip />
        <el-table-column align="center" label="报表正向" prop="reportPositivePoints" width="120" />
        <el-table-column align="center" label="报表负向" prop="reportNegativePoints" width="120" />
        <el-table-column align="center" label="报表净额" prop="reportNetPoints" width="120" />
        <el-table-column align="center" label="可用积分" prop="availablePoints" width="120" />
        <el-table-column align="center" label="冻结积分" prop="frozenPoints" width="120" />
        <el-table-column :formatter="dateFormatter" align="center" label="最后流水" prop="lastTransactionTime" width="180" />
      </template>
      <template v-else-if="activeReport === 'clubRanking'">
        <el-table-column align="center" label="排名" prop="rankNo" width="90" />
        <el-table-column label="俱乐部" min-width="180" prop="clubNameSnapshot" show-overflow-tooltip />
        <el-table-column align="center" label="活动分" prop="activityPoints" width="110" />
        <el-table-column align="center" label="材料分" prop="contributionPoints" width="110" />
        <el-table-column align="center" label="奖励分" prop="rewardPoints" width="110" />
        <el-table-column align="center" label="撤销分" prop="reversedPoints" width="110" />
        <el-table-column align="center" label="总发放" prop="totalIssuedPoints" width="120" />
        <el-table-column :formatter="dateFormatter" align="center" label="生成时间" prop="generatedTime" width="180" />
      </template>
      <template v-else>
        <el-table-column align="center" label="预算ID" prop="id" width="100" />
        <el-table-column align="center" label="分类" prop="category" width="100" />
        <el-table-column align="center" label="预算金额分" prop="budgetAmountCent" width="130" />
        <el-table-column align="center" label="实际金额分" prop="actualAmountCent" width="130" />
        <el-table-column align="center" label="发生日期" prop="occurDate" width="130" />
        <el-table-column align="center" label="来源" width="150">
          <template #default="{ row }">{{ row.sourceType || '-' }} / {{ row.sourceId || '-' }}</template>
        </el-table-column>
        <el-table-column label="说明" min-width="220" prop="description" show-overflow-tooltip />
      </template>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import * as OperationApi from '@/api/clubpoints/admin/operation'
import type { ClubPointPageParam } from '@/api/clubpoints/shared/types'
import PointAmount from '@/views/clubpoints/components/PointAmount.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminReport' })

type ReportKey = 'pointDetail' | 'redemption' | 'ledgerSummary' | 'clubRanking' | 'budget'

const message = useMessage()
const currentYear = new Date().getFullYear()
const reportOptions: Array<{ key: ReportKey; label: string; reportType: number }> = [
  { key: 'pointDetail', label: '积分明细', reportType: 1 },
  { key: 'redemption', label: '兑换记录', reportType: 2 },
  { key: 'ledgerSummary', label: '总台账', reportType: 3 },
  { key: 'clubRanking', label: '俱乐部排名', reportType: 4 },
  { key: 'budget', label: '预算统计', reportType: 5 }
]
const reportFields: Record<ReportKey, string[]> = {
  pointDetail: ['year', 'userId', 'clubId', 'direction', 'pointCategory', 'sourceType', 'timeRange'],
  redemption: ['year', 'batchId', 'userId', 'status', 'timeRange'],
  ledgerSummary: ['year', 'userId', 'clubId', 'timeRange'],
  clubRanking: ['year', 'clubId'],
  budget: ['year', 'category', 'sourceType', 'sourceId']
}

const activeReport = ref<ReportKey>('pointDetail')
const loading = ref(false)
const exportLoading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  year: currentYear,
  userId: undefined as number | undefined,
  clubId: undefined as number | undefined,
  batchId: undefined as number | undefined,
  direction: undefined as number | undefined,
  pointCategory: undefined as number | undefined,
  sourceType: undefined as number | undefined,
  status: undefined as number | undefined,
  category: undefined as number | undefined,
  sourceId: undefined as number | undefined,
  timeRange: [] as string[]
})

const currentReport = computed(() => reportOptions.find((item) => item.key === activeReport.value)!)

const showField = (field: string) => reportFields[activeReport.value].includes(field)

const buildQueryParams = (): ClubPointPageParam => {
  const params: ClubPointPageParam = {
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize
  }
  reportFields[activeReport.value]
    .filter((field) => field !== 'timeRange')
    .forEach((field) => {
      const value = queryParams[field]
      if (value !== undefined && value !== null && value !== '') {
        params[field] = value
      }
    })
  if (showField('timeRange') && queryParams.timeRange?.length === 2) {
    params.startTime = queryParams.timeRange[0]
    params.endTime = queryParams.timeRange[1]
  }
  return params
}

const getList = async () => {
  loading.value = true
  try {
    const params = buildQueryParams()
    const data =
      activeReport.value === 'pointDetail'
        ? await OperationApi.getReportPointDetailPage(params)
        : activeReport.value === 'redemption'
          ? await OperationApi.getReportRedemptionPage(params)
          : activeReport.value === 'ledgerSummary'
            ? await OperationApi.getReportLedgerSummaryPage(params)
            : activeReport.value === 'clubRanking'
              ? await OperationApi.getReportClubRankingPage(params)
              : await OperationApi.getReportBudgetPage(params)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleReportChange = () => {
  queryParams.pageNo = 1
  getList()
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
    year: currentYear,
    userId: undefined,
    clubId: undefined,
    batchId: undefined,
    direction: undefined,
    pointCategory: undefined,
    sourceType: undefined,
    status: undefined,
    category: undefined,
    sourceId: undefined,
    timeRange: []
  })
  getList()
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await OperationApi.exportReportExcel({
      ...buildQueryParams(),
      reportType: currentReport.value.reportType
    })
    download.excel(data, `${currentReport.value.label}.xls`)
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(getList)
</script>

<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="来源统计不代表当前可用积分余额构成；当前可用积分以账户概览为准。"
      type="info"
    />
    <el-row :gutter="16">
      <el-col :md="6" :sm="12" :xs="24">
        <el-statistic title="当前可用积分" :value="summary.availablePoints || 0" />
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-statistic title="冻结积分" :value="summary.frozenPoints || 0" />
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-statistic title="累计积分" :value="summary.totalPoints || 0" />
      </el-col>
      <el-col :md="6" :sm="12" :xs="24">
        <el-statistic title="年度已清零" :value="summary.annualClearedPoints || 0" />
      </el-col>
    </el-row>
  </ContentWrap>

  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="方向" prop="direction">
        <el-select v-model="queryParams.direction" class="!w-180px" clearable placeholder="请选择方向">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_TRANSACTION_DIRECTION)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="来源" prop="sourceType">
        <el-select v-model="queryParams.sourceType" class="!w-200px" clearable placeholder="请选择来源">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_TRANSACTION_SOURCE_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="俱乐部" prop="clubId">
        <el-select v-model="queryParams.clubId" class="!w-220px" clearable placeholder="请选择俱乐部">
          <el-option v-for="club in clubOptions" :key="club.id" :label="club.name" :value="club.id" />
        </el-select>
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
      <el-table-column align="center" label="流水ID" prop="id" width="100" />
      <el-table-column align="center" label="方向" prop="direction" width="100">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_TRANSACTION_DIRECTION" :value="row.direction" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="积分" prop="points" width="130">
        <template #default="{ row }">
          <PointAmount :direction="row.direction === 2 ? 'decrease' : 'increase'" :value="row.points" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="来源" prop="sourceType" width="140">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_TRANSACTION_SOURCE_TYPE" :value="row.sourceType" />
        </template>
      </el-table-column>
      <el-table-column label="发放俱乐部" min-width="160" prop="issuingClubNameSnapshot" />
      <el-table-column label="原因" min-width="220" prop="reason" show-overflow-tooltip />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="登记时间"
        prop="createTime"
        width="180"
      />
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
import * as LedgerApi from '@/api/clubpoints/app/ledger'
import * as ClubApi from '@/api/clubpoints/app/club'
import PointAmount from '@/views/clubpoints/components/PointAmount.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAppLedger' })

const loading = ref(false)
const list = ref<LedgerApi.AppLedgerTransactionRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive<LedgerApi.AppLedgerTransactionPageReqVO>({
  pageNo: 1,
  pageSize: 10
})
const summary = reactive<LedgerApi.AppLedgerSummaryRespVO>({
  availablePoints: 0,
  frozenPoints: 0,
  totalPoints: 0,
  annualClearedPoints: 0
})
const clubOptions = ref<ClubApi.AppClubRespVO[]>([])

const getSummary = async () => {
  Object.assign(summary, await LedgerApi.getAppLedgerSummary())
}

const getList = async () => {
  loading.value = true
  try {
    const data = await LedgerApi.getAppLedgerPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const loadClubOptions = async () => {
  clubOptions.value = await ClubApi.getMyClubList()
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

onMounted(() => {
  getSummary()
  loadClubOptions()
  getList()
})
</script>

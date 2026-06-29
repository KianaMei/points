<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="兑换审核只能通过或拒绝，不能修改申请里的礼品、数量、积分消耗；通过后直接发放，拒绝后释放冻结。"
      type="warning"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="批次ID" prop="batchId">
        <el-input-number v-model="queryParams.batchId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="员工" prop="userId">
        <el-input-number v-model="queryParams.userId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-200px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_REDEMPTION_APPLICATION_STATUS)"
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
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="申请号" min-width="180" prop="applicationNo" />
      <el-table-column align="center" label="员工" prop="userId" width="110" />
      <el-table-column label="批次" min-width="160" prop="batchNameSnapshot">
        <template #default="{ row }">{{ row.batchNameSnapshot || row.batchId }}</template>
      </el-table-column>
      <el-table-column label="礼品" min-width="160" prop="giftNameSnapshot">
        <template #default="{ row }">{{ row.giftNameSnapshot || row.giftId }}</template>
      </el-table-column>
      <el-table-column align="center" label="数量" prop="quantity" width="90" />
      <el-table-column align="center" label="积分消耗" prop="pointsCostSnapshot" width="110" />
      <el-table-column align="center" label="冻结" prop="frozenPoints" width="100" />
      <el-table-column align="center" label="状态" prop="status" width="150">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_APPLICATION_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="申请时间" prop="applyTime" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="审核时间" prop="reviewTime" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="发放时间" prop="directIssueTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="120">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:redemption:review']" link type="primary" @click="openReview(row)">
            审核
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

  <ReviewDialog
    ref="reviewRef"
    approve-text="通过并发放"
    reject-text="拒绝并释放冻结"
    title="兑换审核"
    @submit="submitReview"
  />
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as RedemptionApi from '@/api/clubpoints/admin/redemption'
import type { ReviewReqVO } from '@/api/clubpoints/shared/types'
import ReviewDialog from '@/views/clubpoints/components/ReviewDialog.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminRedemptionApplication' })

const message = useMessage()

const loading = ref(false)
const list = ref<RedemptionApi.AdminRedemptionApplicationRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  batchId: undefined as number | undefined,
  userId: undefined as number | undefined,
  status: undefined as number | undefined
})
const reviewRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await RedemptionApi.getRedemptionApplicationPage(queryParams)
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

const openReview = (row: RedemptionApi.AdminRedemptionApplicationRespVO) => {
  reviewRef.value?.open({
    id: row.id,
    subjectName: `${row.applicationNo || row.id} / ${row.giftNameSnapshot || row.giftId}`,
    approved: true
  })
}

const submitReview = async (payload: ReviewReqVO) => {
  await RedemptionApi.reviewRedemptionApplication({
    id: payload.id,
    result: payload.result,
    reason: payload.reason
  })
  message.success('兑换审核已提交')
  await getList()
}

onMounted(getList)
</script>

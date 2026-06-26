<template>
  <ContentWrap>
    <el-form :inline="true" :model="operateForm" class="-mb-15px" label-width="88px">
      <el-form-item label="年度">
        <el-input-number v-model="operateForm.year" :min="2000" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="原因">
        <el-input v-model="operateForm.reason" class="!w-260px" placeholder="可选，生成操作原因" />
      </el-form-item>
      <el-form-item>
        <el-button
          v-hasPermi="['clubpoints:annual:manage']"
          :loading="operateLoading"
          type="primary"
          @click="generateRanking"
        >
          生成排名
        </el-button>
        <el-button
          v-hasPermi="['clubpoints:annual:manage']"
          :loading="operateLoading"
          type="success"
          @click="suggestIncentive"
        >
          生成激励建议
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="年度" prop="year">
        <el-input-number v-model="queryParams.year" :min="2000" class="!w-160px" controls-position="right" />
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
    <el-table v-loading="loading" :data="list" class="mt-20px">
      <el-table-column align="center" label="排名" prop="rankNo" width="90" />
      <el-table-column label="俱乐部" min-width="180" prop="clubNameSnapshot">
        <template #default="{ row }">{{ row.clubNameSnapshot || row.clubCodeSnapshot || row.clubId }}</template>
      </el-table-column>
      <el-table-column align="center" label="活动分" prop="activityPoints" width="100" />
      <el-table-column align="center" label="贡献分" prop="contributionPoints" width="100" />
      <el-table-column align="center" label="奖励分" prop="rewardPoints" width="100" />
      <el-table-column align="center" label="撤销分" prop="reversedPoints" width="100" />
      <el-table-column align="center" label="总发放分" prop="totalIssuedPoints" width="120" />
      <el-table-column align="center" label="激励金额分" prop="incentiveAmountCent" width="130" />
      <el-table-column align="center" label="确认状态" prop="confirmStatus" width="110" />
      <el-table-column :formatter="dateFormatter" align="center" label="生成时间" prop="generatedTime" width="180" />
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
import { dateFormatter } from '@/utils/formatTime'
import * as OperationApi from '@/api/clubpoints/admin/operation'

defineOptions({ name: 'ClubPointsAdminAnnualRanking' })

const message = useMessage()
const currentYear = new Date().getFullYear()

const operateLoading = ref(false)
const operateForm = reactive({
  year: currentYear,
  reason: ''
})

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  year: currentYear
})

const generateRanking = async () => {
  operateLoading.value = true
  try {
    await OperationApi.generateAnnualRanking({ year: operateForm.year, reason: operateForm.reason })
    message.success('年度排名已生成')
    queryParams.year = operateForm.year
    await getList()
  } finally {
    operateLoading.value = false
  }
}

const suggestIncentive = async () => {
  operateLoading.value = true
  try {
    const count = await OperationApi.suggestAnnualIncentive({ year: operateForm.year, reason: operateForm.reason })
    message.success(`激励建议已生成 ${count || 0} 条`)
    queryParams.year = operateForm.year
    await getList()
  } finally {
    operateLoading.value = false
  }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OperationApi.getAnnualRankingPage(queryParams)
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

onMounted(getList)
</script>

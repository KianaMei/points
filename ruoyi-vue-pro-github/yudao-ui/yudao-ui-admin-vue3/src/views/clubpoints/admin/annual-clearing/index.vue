<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="年度清零只清未冻结可用积分；冻结中的兑换申请后续审核拒绝时释放回账户，不追加过期清零。"
      type="warning"
    />
    <el-form :inline="true" :model="clearForm" class="-mb-15px" label-width="88px">
      <el-form-item label="清零年度" prop="year">
        <el-input-number v-model="clearForm.year" :min="2000" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="clearForm.reason" class="!w-280px" placeholder="请输入年度清零原因" />
      </el-form-item>
      <el-form-item>
        <el-button v-hasPermi="['clubpoints:annual:clear']" :loading="clearLoading" type="primary" @click="clearAnnual">
          执行清零
        </el-button>
      </el-form-item>
    </el-form>
    <el-alert v-if="clearResultText" :closable="false" class="mt-16px" :title="clearResultText" type="success" />
  </ContentWrap>

  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="年度" prop="year">
        <el-input-number v-model="queryParams.year" :min="2000" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="员工ID" prop="userId">
        <el-input-number v-model="queryParams.userId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_ANNUAL_CLEARING_STATUS)"
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
    <el-table v-loading="loading" :data="list" class="mt-20px">
      <el-table-column align="center" label="记录ID" prop="id" width="100" />
      <el-table-column align="center" label="年度" prop="year" width="90" />
      <el-table-column align="center" label="员工ID" prop="userId" width="110" />
      <el-table-column align="center" label="清零前可用" prop="availablePointsBefore" width="120" />
      <el-table-column align="center" label="冻结积分" prop="frozenPointsBefore" width="120" />
      <el-table-column align="center" label="可清积分" prop="clearablePoints" width="120" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_ANNUAL_CLEARING_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="清零时间" prop="clearTime" width="180" />
      <el-table-column label="错误" min-width="220" prop="errorMessage" show-overflow-tooltip />
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
import * as OperationApi from '@/api/clubpoints/admin/operation'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminAnnualClearing' })

const message = useMessage()
const currentYear = new Date().getFullYear()

const clearLoading = ref(false)
const clearResultText = ref('')
const clearForm = reactive({
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
  year: currentYear,
  userId: undefined as number | undefined,
  status: undefined as number | undefined
})

const clearAnnual = async () => {
  if (!clearForm.reason.trim()) {
    message.error('请输入年度清零原因')
    return
  }
  clearLoading.value = true
  try {
    const result = await OperationApi.clearAnnualPoints(clearForm)
    clearResultText.value = `总数 ${result.totalCount || 0}，成功 ${result.successCount || 0}，跳过 ${
      result.skipCount || 0
    }，失败 ${result.failedCount || 0}`
    await getList()
  } finally {
    clearLoading.value = false
  }
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OperationApi.getAnnualClearingRecordPage(queryParams)
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

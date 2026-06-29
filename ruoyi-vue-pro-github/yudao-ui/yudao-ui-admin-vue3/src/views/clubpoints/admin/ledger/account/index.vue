<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="员工" prop="userId">
        <el-input-number v-model="queryParams.userId" :min="1" class="!w-200px" controls-position="right" />
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
      <el-table-column align="center" label="员工" prop="userId" width="120" />
      <el-table-column align="center" label="可用积分" prop="availablePoints" width="160">
        <template #default="{ row }">
          <PointAmount :show-sign="false" :value="row.availablePoints" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="冻结积分" prop="frozenPoints" width="160">
        <template #default="{ row }">
          <PointAmount frozen :show-sign="false" :value="row.frozenPoints" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="累计积分" prop="totalPoints" width="160">
        <template #default="{ row }">
          <PointAmount :show-sign="false" :value="row.totalPoints" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="更新时间"
        prop="updateTime"
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
import { dateFormatter } from '@/utils/formatTime'
import * as LedgerApi from '@/api/clubpoints/admin/ledger'
import PointAmount from '@/views/clubpoints/components/PointAmount.vue'

defineOptions({ name: 'ClubPointsAdminLedgerAccount' })

const loading = ref(false)
const list = ref<LedgerApi.AdminLedgerAccountRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined as number | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await LedgerApi.getAccountPage(queryParams)
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

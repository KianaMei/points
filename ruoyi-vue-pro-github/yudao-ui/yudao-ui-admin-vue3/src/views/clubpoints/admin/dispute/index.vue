<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="员工" prop="userId">
        <el-input-number v-model="queryParams.userId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_DISPUTE_STATUS)"
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
      <el-table-column align="center" label="异议ID" prop="id" width="100" />
      <el-table-column align="center" label="员工" prop="userId" width="110" />
      <el-table-column align="center" label="目标" width="150">
        <template #default="{ row }">{{ row.targetType }} / {{ row.targetId }}</template>
      </el-table-column>
      <el-table-column label="异议内容" min-width="260" prop="content" show-overflow-tooltip />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_DISPUTE_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="回复" min-width="220" prop="replyContent" show-overflow-tooltip />
      <el-table-column :formatter="dateFormatter" align="center" label="提交时间" prop="createdTime" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="处理时间" prop="handledTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="120">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:dispute:handle']" link type="primary" @click="openHandle(row)">
            处理
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

  <Dialog v-model="handleVisible" title="处理异议" width="640">
    <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="104px">
      <el-form-item label="异议ID">
        <el-input v-model="handleForm.id" disabled />
      </el-form-item>
      <el-form-item label="回复内容" prop="replyContent">
        <el-input v-model="handleForm.replyContent" :rows="4" type="textarea" />
      </el-form-item>
      <el-form-item label="关联动作">
        <el-input-number v-model="handleForm.relatedActionType" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="关联流水">
        <el-input-number
          v-model="handleForm.relatedTransactionId"
          :min="1"
          class="!w-240px"
          controls-position="right"
        />
      </el-form-item>
      <el-form-item label="处理原因">
        <el-input v-model="handleForm.reason" :rows="2" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleVisible = false">取消</el-button>
      <el-button :loading="handleLoading" type="primary" @click="submitHandle">提交处理</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as OperationApi from '@/api/clubpoints/admin/operation'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminDispute' })

const message = useMessage()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined as number | undefined,
  status: undefined as number | undefined
})

const handleVisible = ref(false)
const handleLoading = ref(false)
const handleFormRef = ref()
const handleForm = reactive<OperationApi.AdminDisputeHandleReqVO>({
  id: undefined as unknown as number,
  replyContent: '',
  relatedActionType: undefined,
  relatedTransactionId: undefined,
  reason: ''
})
const handleRules = {
  replyContent: [{ required: true, message: '回复内容不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OperationApi.getAdminDisputePage(queryParams)
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

const openHandle = (row: any) => {
  Object.assign(handleForm, {
    id: row.id,
    replyContent: row.replyContent || '',
    relatedActionType: row.relatedActionType,
    relatedTransactionId: row.relatedTransactionId,
    reason: ''
  })
  handleVisible.value = true
}

const submitHandle = async () => {
  await handleFormRef.value?.validate()
  handleLoading.value = true
  try {
    await OperationApi.handleDispute(handleForm)
    message.success('异议已处理')
    handleVisible.value = false
    await getList()
  } finally {
    handleLoading.value = false
  }
}

onMounted(getList)
</script>

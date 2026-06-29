<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="年度" prop="year">
        <el-input-number v-model="queryParams.year" :min="2000" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-input-number v-model="queryParams.category" :min="1" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
        <el-button v-hasPermi="['clubpoints:budget:manage']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新增预算
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="预算ID" prop="id" width="100" />
      <el-table-column align="center" label="分类" prop="category" width="100" />
      <el-table-column align="center" label="预算金额分" prop="budgetAmountCent" width="130" />
      <el-table-column align="center" label="实际金额分" prop="actualAmountCent" width="130" />
      <el-table-column align="center" label="发生日期" prop="occurDate" width="130" />
      <el-table-column align="center" label="来源" width="150">
        <template #default="{ row }">{{ row.sourceType || '-' }} / {{ row.sourceId || '-' }}</template>
      </el-table-column>
      <el-table-column label="说明" min-width="220" prop="description" show-overflow-tooltip />
      <el-table-column label="备注" min-width="180" prop="remark" show-overflow-tooltip />
      <el-table-column align="center" fixed="right" label="操作" width="180">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:budget:manage']" link type="primary" @click="openForm('update', row)">
            编辑
          </el-button>
          <el-button v-hasPermi="['clubpoints:budget:manage']" link type="danger" @click="disableBudget(row)">
            停用
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新增预算' : '编辑预算'" width="760">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="112px">
      <el-form-item label="分类" prop="category">
        <el-input-number v-model="formData.category" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="预算金额分" prop="budgetAmountCent">
        <el-input-number v-model="formData.budgetAmountCent" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="实际金额分">
        <el-input-number v-model="formData.actualAmountCent" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="发生日期">
        <el-date-picker v-model="formData.occurDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="来源类型">
        <el-input-number v-model="formData.sourceType" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="来源ID">
        <el-input-number v-model="formData.sourceId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="formData.description" :rows="2" type="textarea" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" type="textarea" />
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="formData.attachments" directory="clubpoints/budget" />
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="formData.reason" :rows="2" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitForm">保存</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as OperationApi from '@/api/clubpoints/admin/operation'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'

defineOptions({ name: 'ClubPointsAdminBudget' })

const message = useMessage()
const currentYear = new Date().getFullYear()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  year: currentYear,
  category: undefined as number | undefined
})

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<OperationApi.AdminBudgetSaveReqVO>({
  category: 1,
  budgetAmountCent: 0,
  actualAmountCent: undefined,
  occurDate: '',
  sourceType: undefined,
  sourceId: undefined,
  description: '',
  remark: '',
  reason: '',
  attachments: []
})
const formRules = {
  category: [{ required: true, message: '分类不能为空', trigger: 'blur' }],
  budgetAmountCent: [{ required: true, message: '预算金额不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '原因不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OperationApi.getBudgetPage(queryParams)
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

const openForm = (type: 'create' | 'update', row?: any) => {
  formType.value = type
  Object.assign(formData, {
    id: row?.id,
    category: row?.category || 1,
    budgetAmountCent: row?.budgetAmountCent || 0,
    actualAmountCent: row?.actualAmountCent,
    occurDate: row?.occurDate || '',
    sourceType: row?.sourceType,
    sourceId: row?.sourceId,
    description: row?.description || '',
    remark: row?.remark || '',
    reason: '',
    attachments: []
  })
  formVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await OperationApi.createBudget(formData)
    } else {
      await OperationApi.updateBudget(formData)
    }
    message.success('预算已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const disableBudget = async (row: any) => {
  try {
    const result = await message.prompt(`请输入停用预算 ${row.id} 的原因`, '停用预算')
    await OperationApi.disableBudget({ id: row.id, reason: result.value })
    message.success('预算已停用')
    await getList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      message.error('停用预算失败，请重试')
    }
  }
}

onMounted(getList)
</script>

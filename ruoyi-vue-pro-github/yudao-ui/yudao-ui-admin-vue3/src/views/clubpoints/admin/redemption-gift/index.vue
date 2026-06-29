<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="批次ID" prop="batchId">
        <el-input-number v-model="queryParams.batchId" :min="1" class="!w-180px" controls-position="right" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_REDEMPTION_GIFT_STATUS)"
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
        <el-button
          v-hasPermi="['clubpoints:redemption-gift:manage']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />新增礼品
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="礼品ID" prop="id" width="100" />
      <el-table-column align="center" label="批次ID" prop="batchId" width="100" />
      <el-table-column label="礼品名称" min-width="180" prop="name" />
      <el-table-column align="center" label="状态" prop="status" width="110">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_GIFT_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="积分消耗" prop="pointsCost" width="110" />
      <el-table-column align="center" label="总库存" prop="stockTotal" width="100" />
      <el-table-column align="center" label="锁定" prop="stockLocked" width="100" />
      <el-table-column align="center" label="已兑" prop="stockUsed" width="100" />
      <el-table-column align="center" fixed="right" label="操作" width="280">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['clubpoints:redemption-gift:manage']"
            link
            type="primary"
            @click="openForm('update', row)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:redemption-gift:manage']"
            link
            type="success"
            @click="changeGiftStatus(row, 1)"
          >
            上架
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:redemption-gift:manage']"
            link
            type="danger"
            @click="changeGiftStatus(row, 2)"
          >
            下架
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新增礼品' : '编辑礼品'" width="720">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="112px">
      <el-form-item label="批次ID" prop="batchId">
        <el-input-number v-model="formData.batchId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="礼品名称" prop="name">
        <el-input v-model="formData.name" />
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="formData.description" :rows="2" type="textarea" />
      </el-form-item>
      <el-form-item label="积分消耗" prop="pointsCost">
        <el-input-number v-model="formData.pointsCost" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="总库存" prop="stockTotal">
        <el-input-number v-model="formData.stockTotal" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="积分门槛">
        <el-input-number v-model="formData.tierMinPoints" :min="0" class="!w-160px" controls-position="right" />
        <span class="mx-8px">至</span>
        <el-input-number v-model="formData.tierMaxPoints" :min="0" class="!w-160px" controls-position="right" />
      </el-form-item>
      <el-form-item label="参考金额分">
        <el-input-number v-model="formData.referenceAmountCent" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="图片文件ID">
        <el-input-number v-model="formData.imageFileId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="原因">
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
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import * as RedemptionApi from '@/api/clubpoints/admin/redemption'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminRedemptionGift' })

const message = useMessage()

const loading = ref(false)
const list = ref<RedemptionApi.AdminRedemptionGiftRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  batchId: undefined as number | undefined,
  status: undefined as number | undefined
})

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<RedemptionApi.AdminRedemptionGiftSaveReqVO>({
  batchId: undefined as unknown as number,
  name: '',
  description: '',
  pointsCost: 1,
  stockTotal: 0,
  sort: 0,
  reason: ''
})
const formRules = {
  batchId: [{ required: true, message: '批次ID不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '礼品名称不能为空', trigger: 'blur' }],
  pointsCost: [{ required: true, message: '积分消耗不能为空', trigger: 'blur' }],
  stockTotal: [{ required: true, message: '总库存不能为空', trigger: 'blur' }],
  sort: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await RedemptionApi.getRedemptionGiftPage(queryParams)
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

const openForm = (type: 'create' | 'update', row?: RedemptionApi.AdminRedemptionGiftRespVO) => {
  formType.value = type
  Object.assign(formData, {
    id: row?.id,
    batchId: row?.batchId,
    name: row?.name || '',
    description: row?.description || '',
    pointsCost: row?.pointsCost ?? 1,
    tierMinPoints: row?.tierMinPoints,
    tierMaxPoints: row?.tierMaxPoints,
    referenceAmountCent: row?.referenceAmountCent,
    stockTotal: row?.stockTotal ?? 0,
    imageFileId: row?.imageFileId,
    sort: row?.sort ?? 0,
    reason: ''
  })
  formVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await RedemptionApi.createRedemptionGift(formData)
    } else {
      await RedemptionApi.updateRedemptionGift(formData)
    }
    message.success('礼品已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const changeGiftStatus = async (row: RedemptionApi.AdminRedemptionGiftRespVO, status: 1 | 2) => {
  const actionText = status === 1 ? '上架' : '下架'
  try {
    const result = await message.prompt(`请输入${actionText}礼品 ${row.name} 的原因`, `${actionText}礼品`)
    await RedemptionApi.updateRedemptionGiftStatus({ id: row.id, status, reason: result.value })
    message.success(`${actionText}成功`)
    await getList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      message.error(`${actionText}失败，请重试`)
    }
  }
}

onMounted(getList)
</script>

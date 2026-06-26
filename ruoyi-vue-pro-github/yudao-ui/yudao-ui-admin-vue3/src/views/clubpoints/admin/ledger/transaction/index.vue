<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="流水是积分事实源，只能通过调整或撤销生成新流水，不能在页面直接改原流水。"
      type="info"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="员工ID" prop="userId">
        <el-input-number v-model="queryParams.userId" :min="1" class="!w-200px" controls-position="right" />
      </el-form-item>
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
      <el-form-item label="来源类型" prop="sourceType">
        <el-select v-model="queryParams.sourceType" class="!w-200px" clearable placeholder="请选择来源">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_TRANSACTION_SOURCE_TYPE)"
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
          v-hasPermi="['clubpoints:ledger:adjust']"
          plain
          type="primary"
          @click="openAdjustDialog"
        >
          <Icon class="mr-5px" icon="ep:plus" />调整积分
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="流水ID" prop="id" width="100" />
      <el-table-column align="center" label="员工ID" prop="userId" width="120" />
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
      <el-table-column label="原因" min-width="220" prop="reason" show-overflow-tooltip />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="登记时间"
        prop="createTime"
        width="180"
      />
      <el-table-column align="center" fixed="right" label="操作" width="120">
        <template #default="{ row }">
          <el-button
            v-hasPermi="['clubpoints:ledger:reverse']"
            link
            type="danger"
            @click="reverseTransaction(row)"
          >
            撤销
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

  <Dialog v-model="adjustDialogVisible" title="调整积分" width="760">
    <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="112px">
      <el-form-item label="请求号" prop="requestNo">
        <el-input v-model="adjustForm.requestNo" disabled />
      </el-form-item>
      <el-form-item label="员工" prop="userId">
        <UserPicker v-model="adjustForm.userId" />
      </el-form-item>
      <el-form-item label="方向" prop="direction">
        <el-radio-group v-model="adjustForm.direction">
          <el-radio :label="1">增加</el-radio>
          <el-radio :label="2">扣减</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="积分" prop="points">
        <el-input-number v-model="adjustForm.points" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="发放俱乐部">
        <ClubSelect v-model="adjustForm.issuingClubId" :options="clubOptions" />
      </el-form-item>
      <el-form-item label="规则项" prop="ruleVersionId">
        <RuleItemSelect
          v-model="selectedRuleItemId"
          v-model:rule-version-id="adjustForm.ruleVersionId"
          :item-options="ruleItemOptions"
          :version-options="ruleVersionOptions"
          @change="handleRuleItemChange"
        />
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="adjustForm.reason" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="adjustForm.attachments" directory="clubpoints/ledger-adjust" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="adjustDialogVisible = false">取消</el-button>
      <el-button :loading="adjustSubmitLoading" type="primary" @click="submitAdjust">提交调整</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as LedgerApi from '@/api/clubpoints/admin/ledger'
import * as ClubApi from '@/api/clubpoints/admin/club'
import * as RuleApi from '@/api/clubpoints/admin/rule'
import type {
  ClubPointClubOption,
  ClubPointRuleItemOption,
  ClubPointRuleVersionOption
} from '@/api/clubpoints/shared/types'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import ClubSelect from '@/views/clubpoints/components/ClubSelect.vue'
import PointAmount from '@/views/clubpoints/components/PointAmount.vue'
import RuleItemSelect from '@/views/clubpoints/components/RuleItemSelect.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'
import UserPicker from '@/views/clubpoints/components/UserPicker.vue'

defineOptions({ name: 'ClubPointsAdminLedgerTransaction' })

const message = useMessage()

const loading = ref(false)
const list = ref<LedgerApi.AdminLedgerTransactionRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: undefined as number | undefined,
  direction: undefined as number | undefined,
  sourceType: undefined as number | undefined
})

const adjustDialogVisible = ref(false)
const adjustSubmitLoading = ref(false)
const adjustFormRef = ref()
const adjustForm = reactive<LedgerApi.AdminLedgerAdjustReqVO>({
  requestNo: '',
  userId: undefined as unknown as number,
  direction: 1,
  points: 1,
  ruleVersionId: undefined as unknown as number,
  ruleItemCode: '',
  issuingClubId: undefined,
  reason: '',
  attachments: []
})
const adjustRules = {
  userId: [{ required: true, message: '员工不能为空', trigger: 'change' }],
  direction: [{ required: true, message: '方向不能为空', trigger: 'change' }],
  points: [{ required: true, message: '积分不能为空', trigger: 'blur' }],
  ruleVersionId: [{ required: true, message: '规则版本不能为空', trigger: 'change' }],
  reason: [{ required: true, message: '原因不能为空', trigger: 'blur' }]
}
const selectedRuleItemId = ref<number>()
const clubOptions = ref<ClubPointClubOption[]>([])
const ruleVersionOptions = ref<ClubPointRuleVersionOption[]>([])
const ruleItemOptions = ref<ClubPointRuleItemOption[]>([])

const getList = async () => {
  loading.value = true
  try {
    const data = await LedgerApi.getTransactionPage(queryParams)
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

const loadClubOptions = async () => {
  const data = await ClubApi.getClubPage({ pageNo: 1, pageSize: 100 })
  clubOptions.value = (data.list || []).map((item) => ({
    id: item.id,
    name: item.name,
    code: item.code,
    status: item.status
  }))
}

const loadRuleVersionOptions = async () => {
  const data = await RuleApi.getRuleVersionPage({ pageNo: 1, pageSize: 100 })
  ruleVersionOptions.value = (data.list || []).map((item) => ({
    id: item.id,
    versionNo: item.versionNo,
    name: item.name,
    status: item.status
  }))
}

watch(
  () => adjustForm.ruleVersionId,
  async (ruleVersionId) => {
    selectedRuleItemId.value = undefined
    adjustForm.ruleItemCode = ''
    if (!ruleVersionId) {
      ruleItemOptions.value = []
      return
    }
    const data = await RuleApi.getRuleItemList(ruleVersionId)
    ruleItemOptions.value = (data || []).map((item: any) => ({
      id: item.id,
      ruleVersionId: item.ruleVersionId,
      itemCode: item.itemCode,
      itemName: item.itemName,
      minPoints: item.minPoints,
      maxPoints: item.maxPoints,
      defaultPoints: item.defaultPoints,
      status: item.status
    }))
  }
)

const handleRuleItemChange = (item?: ClubPointRuleItemOption) => {
  adjustForm.ruleItemCode = item?.itemCode || ''
}

const openAdjustDialog = async () => {
  Object.assign(adjustForm, {
    requestNo: LedgerApi.resetLedgerAdjustRequestNo('admin-ledger-adjust'),
    userId: undefined,
    direction: 1,
    points: 1,
    ruleVersionId: undefined,
    ruleItemCode: '',
    issuingClubId: undefined,
    reason: '',
    attachments: []
  })
  selectedRuleItemId.value = undefined
  adjustDialogVisible.value = true
  await Promise.all([loadClubOptions(), loadRuleVersionOptions()])
}

const submitAdjust = async () => {
  await adjustFormRef.value?.validate()
  adjustSubmitLoading.value = true
  try {
    await LedgerApi.adjustLedger(adjustForm)
    message.success('积分调整已提交')
    adjustDialogVisible.value = false
    await getList()
  } finally {
    adjustSubmitLoading.value = false
  }
}

const reverseTransaction = async (row: LedgerApi.AdminLedgerTransactionRespVO) => {
  try {
    const result = await message.prompt(`请输入撤销流水 ${row.id} 的原因`, '撤销流水')
    await LedgerApi.reverseLedger({ transactionId: row.id, reason: result.value })
    message.success('撤销成功')
    await getList()
  } catch {}
}

onMounted(getList)
</script>

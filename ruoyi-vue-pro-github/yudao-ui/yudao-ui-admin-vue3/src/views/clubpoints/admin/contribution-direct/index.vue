<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="管理员代录提交后直接生效。请求号用于后端幂等：失败重试保留同一个 requestNo，重开代录才生成新号。"
      type="warning"
    />
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="112px">
      <el-form-item label="请求号" prop="requestNo">
        <el-input v-model="formData.requestNo" disabled />
      </el-form-item>
      <el-form-item label="员工" prop="userId">
        <UserPicker v-model="formData.userId" />
      </el-form-item>
      <el-form-item label="发放俱乐部">
        <ClubSelect v-model="formData.clubId" :options="clubOptions" />
      </el-form-item>
      <el-form-item label="规则项" prop="ruleVersionId">
        <RuleItemSelect
          v-model="selectedRuleItemId"
          v-model:rule-version-id="formData.ruleVersionId"
          :item-options="ruleItemOptions"
          :version-options="ruleVersionOptions"
          @change="handleRuleItemChange"
        />
      </el-form-item>
      <el-form-item label="积分" prop="points">
        <el-input-number v-model="formData.points" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="formData.reason" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="formData.attachments" directory="clubpoints/contribution-direct" />
      </el-form-item>
    </el-form>
    <div class="mt-16px">
      <el-button @click="resetDirectForm">重开代录</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitDirect">提交代录</el-button>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as ClubApi from '@/api/clubpoints/admin/club'
import * as ContributionApi from '@/api/clubpoints/admin/contribution'
import * as RuleApi from '@/api/clubpoints/admin/rule'
import type {
  ClubPointClubOption,
  ClubPointRuleItemOption,
  ClubPointRuleVersionOption
} from '@/api/clubpoints/shared/types'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import ClubSelect from '@/views/clubpoints/components/ClubSelect.vue'
import RuleItemSelect from '@/views/clubpoints/components/RuleItemSelect.vue'
import UserPicker from '@/views/clubpoints/components/UserPicker.vue'

defineOptions({ name: 'ClubPointsAdminContributionDirect' })

const message = useMessage()

const formRef = ref()
const submitLoading = ref(false)
const selectedRuleItemId = ref<number>()
const clubOptions = ref<ClubPointClubOption[]>([])
const ruleVersionOptions = ref<ClubPointRuleVersionOption[]>([])
const ruleItemOptions = ref<ClubPointRuleItemOption[]>([])
const formData = reactive<ContributionApi.AdminContributionDirectCreateReqVO>({
  requestNo: '',
  userId: undefined as unknown as number,
  clubId: undefined,
  points: 1,
  ruleVersionId: undefined as unknown as number,
  ruleItemCode: '',
  reason: '',
  attachments: []
})
const formRules = {
  requestNo: [{ required: true, message: '请求号不能为空', trigger: 'blur' }],
  userId: [{ required: true, message: '员工不能为空', trigger: 'change' }],
  ruleVersionId: [{ required: true, message: '规则版本不能为空', trigger: 'change' }],
  points: [{ required: true, message: '积分不能为空', trigger: 'blur' }],
  reason: [{ required: true, message: '原因不能为空', trigger: 'blur' }]
}

const loadClubOptions = async () => {
  const data = await ClubApi.getClubPage({ pageNo: 1, pageSize: 100 })
  clubOptions.value = (data.list || []).map((club) => ({
    id: club.id,
    name: club.name,
    code: club.code,
    status: club.status
  }))
}

const loadRuleVersionOptions = async () => {
  const data = await RuleApi.getRuleVersionPage({ pageNo: 1, pageSize: 100 })
  ruleVersionOptions.value = (data.list || []).map((version) => ({
    id: version.id,
    versionNo: version.versionNo,
    name: version.name,
    status: version.status
  }))
}

watch(
  () => formData.ruleVersionId,
  async (ruleVersionId) => {
    selectedRuleItemId.value = undefined
    formData.ruleItemCode = ''
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
  formData.ruleItemCode = item?.itemCode || ''
  if (item?.defaultPoints) {
    formData.points = item.defaultPoints
  }
}

const resetDirectForm = () => {
  Object.assign(formData, {
    requestNo: ContributionApi.resetDirectContributionRequestNo('admin-direct-contribution'),
    userId: undefined,
    clubId: undefined,
    points: 1,
    ruleVersionId: undefined,
    ruleItemCode: '',
    reason: '',
    attachments: []
  })
  selectedRuleItemId.value = undefined
}

const submitDirect = async () => {
  await formRef.value?.validate()
  if (!formData.attachments || formData.attachments.length === 0) {
    message.error('管理员代录必须上传附件或填写外部链接')
    return
  }
  submitLoading.value = true
  try {
    await ContributionApi.directCreateContribution(formData)
    message.success('代录已提交')
    resetDirectForm()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  resetDirectForm()
  await Promise.all([loadClubOptions(), loadRuleVersionOptions()])
})
</script>

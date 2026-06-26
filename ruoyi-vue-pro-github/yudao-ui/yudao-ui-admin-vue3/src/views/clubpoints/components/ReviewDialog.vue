<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="520">
    <el-form :model="form" label-width="90px">
      <el-form-item label="审核对象" v-if="subjectName">
        <el-input :model-value="subjectName" disabled />
      </el-form-item>
      <el-form-item label="审核结果" required>
        <el-radio-group v-model="form.approved">
          <el-radio-button :label="true">{{ approveText }}</el-radio-button>
          <el-radio-button :label="false">{{ rejectText }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item :label="reasonLabel" :required="reasonRequired">
        <el-input
          v-model="form.reason"
          :placeholder="reasonPlaceholder"
          :rows="4"
          maxlength="500"
          show-word-limit
          type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="submit">提交审核</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import {
  CLUB_POINT_REVIEW_APPROVED,
  CLUB_POINT_REVIEW_REJECTED,
  ClubPointReviewResult,
  ReviewReqVO
} from '@/api/clubpoints/shared/types'

defineOptions({ name: 'ClubPointsReviewDialog' })

const props = withDefaults(
  defineProps<{
    title?: string
    approveText?: string
    rejectText?: string
    approveResult?: ClubPointReviewResult
    rejectResult?: ClubPointReviewResult
    requireRejectReason?: boolean
  }>(),
  {
    title: '审核',
    approveText: '通过',
    rejectText: '驳回/拒绝',
    approveResult: CLUB_POINT_REVIEW_APPROVED,
    rejectResult: CLUB_POINT_REVIEW_REJECTED,
    requireRejectReason: true
  }
)

const emit = defineEmits<{
  submit: [payload: ReviewReqVO]
  close: []
}>()

const message = useMessage()
const dialogVisible = ref(false)
const targetId = ref<number>()
const subjectName = ref('')

const form = reactive({
  approved: true,
  reason: ''
})

const dialogTitle = computed(() => props.title)
const reasonRequired = computed(() => props.requireRejectReason && !form.approved)
const reasonLabel = computed(() => (form.approved ? '审核说明' : '驳回原因'))
const reasonPlaceholder = computed(() =>
  form.approved ? '可填写审核说明' : '驳回或拒绝时必须填写原因'
)

const open = (options?: { id?: number; subjectName?: string; approved?: boolean }) => {
  targetId.value = options?.id
  subjectName.value = options?.subjectName || ''
  form.approved = options?.approved ?? true
  form.reason = ''
  dialogVisible.value = true
}

const close = () => {
  dialogVisible.value = false
  emit('close')
}

const submit = () => {
  if (reasonRequired.value && !form.reason.trim()) {
    message.error('请填写驳回或拒绝原因')
    return
  }
  const result = form.approved ? props.approveResult : props.rejectResult
  emit('submit', {
    id: targetId.value,
    approved: form.approved,
    result,
    reason: form.reason.trim() || undefined
  })
  dialogVisible.value = false
}

defineExpose({ open, close })
</script>

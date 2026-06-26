<template>
  <Dialog v-model="dialogVisible" :title="title" width="560">
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="该操作只用于管理员物理删除俱乐部，提交后不可撤销。"
      type="error"
    />
    <el-form ref="formRef" :model="form" label-width="96px">
      <el-form-item label="确认文本">
        <el-input :model-value="expectedText" disabled />
      </el-form-item>
      <el-form-item label="手动输入" required>
        <el-input v-model="form.confirmText" :placeholder="expectedText" />
      </el-form-item>
      <el-form-item v-if="requireReason" label="删除原因" required>
        <el-input v-model="form.reason" :rows="3" placeholder="请输入删除原因" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button :disabled="!canSubmit" type="danger" @click="submit">确认删除</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { StrongConfirmPayload } from '@/api/clubpoints/shared/types'

defineOptions({ name: 'ClubPointsStrongConfirmDialog' })

const props = withDefaults(
  defineProps<{
    title?: string
    confirmPrefix?: string
    expectedText?: string
    requireReason?: boolean
  }>(),
  {
    title: '强确认删除俱乐部',
    confirmPrefix: '确认删除俱乐部：',
    requireReason: true
  }
)

const emit = defineEmits<{
  confirm: [payload: StrongConfirmPayload]
  close: []
}>()

const message = useMessage()
const dialogVisible = ref(false)
const targetName = ref('')
const form = reactive({
  confirmText: '',
  reason: ''
})

const expectedText = computed(
  () => props.expectedText || `${props.confirmPrefix}${targetName.value}`
)

const canSubmit = computed(() => {
  return form.confirmText === expectedText.value && (!props.requireReason || !!form.reason.trim())
})

const open = (options?: { targetName?: string; expectedText?: string }) => {
  targetName.value = options?.targetName || ''
  form.confirmText = ''
  form.reason = ''
  dialogVisible.value = true
}

const close = () => {
  dialogVisible.value = false
  emit('close')
}

const submit = () => {
  if (!canSubmit.value) {
    message.error('确认文本或原因不完整')
    return
  }
  emit('confirm', {
    confirmText: form.confirmText,
    confirmedAt: new Date().toISOString(),
    reason: form.reason.trim() || undefined
  })
  dialogVisible.value = false
}

defineExpose({ open, close })
</script>

<template>
  <div class="club-points-attachment-input">
    <UploadFile
      v-model="fileUrls"
      :auto-upload="autoUpload"
      :directory="directory"
      :disabled="disabled"
      :file-size="fileSize"
      :file-type="fileType"
      :limit="limit"
    />

    <div v-if="allowExternal" class="club-points-attachment-input__links">
      <div class="flex items-center justify-between mb-8px">
        <span class="text-13px text-[var(--el-text-color-regular)]">外部链接</span>
        <el-button v-if="!disabled" link type="primary" @click="addLink">添加链接</el-button>
      </div>
      <div
        v-for="(item, index) in linkItems"
        :key="index"
        class="club-points-attachment-input__link-row"
      >
        <el-input
          v-model="item.name"
          :disabled="disabled"
          placeholder="链接名称"
          @input="emitModelValue"
        />
        <el-input
          v-model="item.url"
          :disabled="disabled"
          placeholder="https://..."
          @input="emitModelValue"
        />
        <el-button v-if="!disabled" link type="danger" @click="removeLink(index)">删除</el-button>
      </div>
      <el-empty v-if="linkItems.length === 0" :image-size="48" description="暂无外部链接" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { AttachmentInputVO, ClubPointAttachmentType } from '@/api/clubpoints/shared/types'

defineOptions({ name: 'ClubPointsAttachmentInput' })

const props = withDefaults(
  defineProps<{
    modelValue?: AttachmentInputVO[]
    disabled?: boolean
    allowExternal?: boolean
    limit?: number
    fileType?: string[]
    fileSize?: number
    directory?: string
    autoUpload?: boolean
  }>(),
  {
    modelValue: () => [],
    disabled: false,
    allowExternal: true,
    limit: 5,
    fileType: () => [
      'doc',
      'docx',
      'xls',
      'xlsx',
      'ppt',
      'pptx',
      'txt',
      'pdf',
      'png',
      'jpg',
      'jpeg'
    ],
    fileSize: 10,
    autoUpload: true
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: AttachmentInputVO[]]
  change: [value: AttachmentInputVO[]]
}>()

const fileUrls = ref<string[]>([])
const linkItems = ref<AttachmentInputVO[]>([])
const syncingFromProps = ref(false)

const fileNameFromUrl = (url: string) => {
  const cleanUrl = url.split('?')[0]
  return decodeURIComponent(cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1)) || cleanUrl
}

watch(
  () => props.modelValue,
  (value) => {
    syncingFromProps.value = true
    fileUrls.value = (value || [])
      .filter((item) => item.type !== ClubPointAttachmentType.LINK && !!item.url)
      .map((item) => item.url!)
    linkItems.value = (value || [])
      .filter((item) => item.type === ClubPointAttachmentType.LINK)
      .map((item) => ({ ...item }))
    nextTick(() => {
      syncingFromProps.value = false
    })
  },
  { immediate: true, deep: true }
)

watch(
  fileUrls,
  () => {
    if (!syncingFromProps.value) {
      emitModelValue()
    }
  },
  { deep: true }
)

const addLink = () => {
  linkItems.value.push({ type: ClubPointAttachmentType.LINK, name: '', url: '' })
  emitModelValue()
}

const removeLink = (index: number) => {
  linkItems.value.splice(index, 1)
  emitModelValue()
}

const emitModelValue = () => {
  const fileItems = fileUrls.value
    .filter((url) => !!url)
    .map((url) => ({
      type: ClubPointAttachmentType.FILE,
      name: fileNameFromUrl(url),
      url
    }))
  const externalItems = linkItems.value
    .filter((item) => item.name || item.url)
    .map((item) => ({
      type: ClubPointAttachmentType.LINK,
      name: item.name,
      url: item.url,
      remark: item.remark
    }))
  const value = [...fileItems, ...externalItems]
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style lang="scss" scoped>
.club-points-attachment-input {
  display: flex;
  flex-direction: column;
  gap: 12px;

  &__links {
    padding: 12px;
    border: 1px dashed var(--el-border-color);
    border-radius: 8px;
  }

  &__link-row {
    display: grid;
    grid-template-columns: minmax(120px, 200px) minmax(220px, 1fr) auto;
    gap: 8px;
    align-items: center;
    margin-bottom: 8px;
  }
}

@media (max-width: 768px) {
  .club-points-attachment-input__link-row {
    grid-template-columns: 1fr;
  }
}
</style>

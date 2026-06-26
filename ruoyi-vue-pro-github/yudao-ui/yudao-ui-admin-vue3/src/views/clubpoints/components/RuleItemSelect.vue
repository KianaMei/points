<template>
  <div class="club-points-rule-item-select">
    <el-select
      v-model="selectedVersionId"
      :disabled="disabled"
      class="!w-240px"
      clearable
      placeholder="请选择规则版本"
      @change="handleVersionChange"
    >
      <el-option
        v-for="version in versionOptions"
        :key="version.id"
        :label="versionLabel(version)"
        :value="version.id"
      />
    </el-select>
    <el-select
      v-model="selectedItemId"
      :disabled="disabled || !selectedVersionId"
      class="!w-280px"
      clearable
      placeholder="请选择规则项"
      @change="handleItemChange"
    >
      <el-option
        v-for="item in filteredItems"
        :key="item.id"
        :label="itemLabel(item)"
        :value="item.id"
      />
    </el-select>
    <el-tag v-if="selectedItem" type="info"> 分值 {{ pointsRangeText(selectedItem) }} </el-tag>
  </div>
</template>

<script lang="ts" setup>
import { ClubPointRuleItemOption, ClubPointRuleVersionOption } from '@/api/clubpoints/shared/types'

defineOptions({ name: 'ClubPointsRuleItemSelect' })

const props = withDefaults(
  defineProps<{
    modelValue?: number
    ruleVersionId?: number
    versionOptions?: ClubPointRuleVersionOption[]
    itemOptions?: ClubPointRuleItemOption[]
    disabled?: boolean
  }>(),
  {
    versionOptions: () => [],
    itemOptions: () => [],
    disabled: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: number | undefined]
  'update:ruleVersionId': [value: number | undefined]
  change: [item: ClubPointRuleItemOption | undefined]
}>()

const selectedVersionId = ref<number>()
const selectedItemId = ref<number>()

watch(
  () => props.ruleVersionId,
  (value) => {
    selectedVersionId.value = value
  },
  { immediate: true }
)

watch(
  () => props.modelValue,
  (value) => {
    selectedItemId.value = value
  },
  { immediate: true }
)

const filteredItems = computed(() => {
  if (!selectedVersionId.value) {
    return []
  }
  return props.itemOptions.filter((item) => item.ruleVersionId === selectedVersionId.value)
})

const selectedItem = computed(() => {
  return props.itemOptions.find((item) => item.id === selectedItemId.value)
})

const versionLabel = (version: ClubPointRuleVersionOption) => {
  return version.versionNo ? `${version.name} (${version.versionNo})` : version.name
}

const pointsRangeText = (item: ClubPointRuleItemOption) => {
  if (item.minPoints === undefined || item.maxPoints === undefined) {
    return '-'
  }
  if (item.minPoints === item.maxPoints) {
    return `${item.minPoints}`
  }
  return `${item.minPoints} - ${item.maxPoints}`
}

const itemLabel = (item: ClubPointRuleItemOption) => {
  return `${item.itemName} [${item.itemCode}] ${pointsRangeText(item)}`
}

const handleVersionChange = (value?: number) => {
  selectedVersionId.value = value
  selectedItemId.value = undefined
  emit('update:ruleVersionId', value)
  emit('update:modelValue', undefined)
  emit('change', undefined)
}

const handleItemChange = (value?: number) => {
  selectedItemId.value = value
  emit('update:modelValue', value)
  emit('change', selectedItem.value)
}
</script>

<style lang="scss" scoped>
.club-points-rule-item-select {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
</style>

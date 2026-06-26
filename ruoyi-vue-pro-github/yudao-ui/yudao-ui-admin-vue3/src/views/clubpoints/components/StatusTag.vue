<template>
  <span v-if="resolvedType && hasValue">
    <dict-tag :type="resolvedType" :value="value!" />
  </span>
  <span v-else class="club-points-status-empty">{{ emptyText }}</span>
</template>

<script lang="ts" setup>
defineOptions({ name: 'ClubPointsStatusTag' })

const props = withDefaults(
  defineProps<{
    type?: string
    dictType?: string
    value?: string | number | boolean | Array<string | number>
    emptyText?: string
  }>(),
  {
    emptyText: '-'
  }
)

const resolvedType = computed(() => props.dictType || props.type || '')
const hasValue = computed(
  () => props.value !== undefined && props.value !== null && props.value !== ''
)
</script>

<style lang="scss" scoped>
.club-points-status-empty {
  color: var(--el-text-color-placeholder);
}
</style>

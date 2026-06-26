<template>
  <span class="club-points-amount" :class="amountClass">
    <span class="club-points-amount__value">{{ displayValue }}</span>
    <el-tag v-if="frozen" class="ml-6px" size="small" type="warning">冻结</el-tag>
    <el-tooltip v-if="annualCleared || annualClearTip" placement="top" :content="annualTipText">
      <el-tag class="ml-6px" size="small" type="info">年度清零</el-tag>
    </el-tooltip>
  </span>
</template>

<script lang="ts" setup>
defineOptions({ name: 'ClubPointsPointAmount' })

const props = withDefaults(
  defineProps<{
    value?: number
    direction?: 'increase' | 'decrease'
    frozen?: boolean
    annualCleared?: boolean
    annualClearTip?: string
    showSign?: boolean
    unit?: string
  }>(),
  {
    value: 0,
    frozen: false,
    annualCleared: false,
    showSign: true,
    unit: '分'
  }
)

const normalizedValue = computed(() => Number(props.value || 0))

const signedValue = computed(() => {
  if (props.direction === 'decrease') {
    return -Math.abs(normalizedValue.value)
  }
  if (props.direction === 'increase') {
    return Math.abs(normalizedValue.value)
  }
  return normalizedValue.value
})

const amountClass = computed(() => ({
  'is-positive': signedValue.value > 0,
  'is-negative': signedValue.value < 0,
  'is-zero': signedValue.value === 0,
  'is-frozen': props.frozen
}))

const displayValue = computed(() => {
  const prefix = props.showSign && signedValue.value > 0 ? '+' : ''
  return `${prefix}${signedValue.value}${props.unit}`
})

const annualTipText = computed(() => {
  return props.annualClearTip || '年度清零仅清未冻结可用积分，冻结积分后续释放不追加过期清零'
})
</script>

<style lang="scss" scoped>
.club-points-amount {
  display: inline-flex;
  align-items: center;
  font-weight: 600;

  &__value {
    font-variant-numeric: tabular-nums;
  }

  &.is-positive {
    color: var(--el-color-success);
  }

  &.is-negative {
    color: var(--el-color-danger);
  }

  &.is-zero {
    color: var(--el-text-color-secondary);
  }

  &.is-frozen {
    color: var(--el-color-warning);
  }
}
</style>

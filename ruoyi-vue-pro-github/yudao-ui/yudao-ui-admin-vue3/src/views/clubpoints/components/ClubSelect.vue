<template>
  <el-select
    v-model="currentValue"
    :clearable="clearable"
    :disabled="disabled"
    :multiple="multiple"
    :placeholder="placeholder"
    class="w-full"
    filterable
    @change="handleChange"
  >
    <el-option v-for="club in options" :key="club.id" :label="clubLabel(club)" :value="club.id">
      <div class="club-points-club-option">
        <span>{{ clubLabel(club) }}</span>
        <StatusTag
          v-if="club.status !== undefined"
          :type="DICT_TYPE.CLUB_POINTS_CLUB_STATUS"
          :value="club.status"
        />
      </div>
    </el-option>
  </el-select>
</template>

<script lang="ts" setup>
import { ClubPointClubOption } from '@/api/clubpoints/shared/types'
import { DICT_TYPE } from '@/utils/dict'
import StatusTag from './StatusTag.vue'

defineOptions({ name: 'ClubPointsClubSelect' })

const props = withDefaults(
  defineProps<{
    modelValue?: number | number[]
    options?: ClubPointClubOption[]
    multiple?: boolean
    disabled?: boolean
    clearable?: boolean
    placeholder?: string
  }>(),
  {
    options: () => [],
    multiple: false,
    disabled: false,
    clearable: true,
    placeholder: '请选择俱乐部'
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: number | number[] | undefined]
  change: [value: ClubPointClubOption | ClubPointClubOption[] | undefined]
}>()

const currentValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const clubLabel = (club: ClubPointClubOption) => {
  return club.code ? `${club.name} (${club.code})` : club.name
}

const handleChange = (value: number | number[]) => {
  if (Array.isArray(value)) {
    emit(
      'change',
      props.options.filter((club) => value.includes(club.id))
    )
    return
  }
  emit(
    'change',
    props.options.find((club) => club.id === value)
  )
}
</script>

<style lang="scss" scoped>
.club-points-club-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
</style>

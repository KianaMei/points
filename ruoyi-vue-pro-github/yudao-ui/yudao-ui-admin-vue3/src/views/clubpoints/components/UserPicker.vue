<template>
  <UserSelectV2
    v-model="currentValue"
    :clearable="clearable"
    :default-current-user="defaultCurrentUser"
    :dept-id="deptId"
    :disabled="disabled"
    :disabled-ids="disabledIds"
    :multiple="multiple"
    :placeholder="placeholder"
    @change="handleChange"
  />
</template>

<script lang="ts" setup>
import UserSelectV2 from '@/views/system/user/components/UserSelectV2.vue'

defineOptions({ name: 'ClubPointsUserPicker' })

const props = withDefaults(
  defineProps<{
    modelValue?: number | number[]
    defaultCurrentUser?: boolean
    multiple?: boolean
    disabled?: boolean
    disabledIds?: number[]
    clearable?: boolean
    placeholder?: string
    deptId?: number
  }>(),
  {
    defaultCurrentUser: false,
    multiple: false,
    disabled: false,
    disabledIds: () => [],
    clearable: true,
    placeholder: '请选择员工'
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: number | number[] | undefined]
  change: [item: any]
}>()

const currentValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const handleChange = (item: any) => {
  emit('change', item)
}
</script>

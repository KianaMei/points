<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="结算后修正不能改原流水，只能提示管理员通过补发、撤销或调整流水处理。"
      type="warning"
    />
    <el-tabs v-model="activeTab">
      <el-tab-pane label="报名名单" name="registration">
        <el-table v-loading="registrationLoading" :data="registrationList">
          <el-table-column label="活动" min-width="220" prop="activityTitleSnapshot" />
          <el-table-column label="员工" min-width="160" prop="userNameSnapshot" />
          <el-table-column label="部门" min-width="140" prop="deptNameSnapshot" />
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_REGISTRATION_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column align="center" label="特殊缺席" prop="specialAbsenceFlag" width="120">
            <template #default="{ row }">
              <el-tag :type="row.specialAbsenceFlag ? 'warning' : 'info'">
                {{ row.specialAbsenceFlag ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="180">
            <template #default="{ row }">
              <el-button v-hasPermi="['clubpoints:attendance:correct']" link type="primary" @click="openSupplement(row)">
                补录
              </el-button>
              <el-button
                v-hasPermi="['clubpoints:registration:special-absence']"
                link
                type="warning"
                @click="markSpecialAbsence(row)"
              >
                特殊缺席
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="registrationQuery.pageSize"
          v-model:page="registrationQuery.pageNo"
          :total="registrationTotal"
          @pagination="getRegistrationList"
        />
      </el-tab-pane>

      <el-tab-pane label="签到签退" name="attendance">
        <el-table v-loading="attendanceLoading" :data="attendanceList">
          <el-table-column label="活动" min-width="220" prop="activityTitleSnapshot" />
          <el-table-column label="员工" min-width="160" prop="userNameSnapshot" />
          <el-table-column align="center" label="目标" prop="targetType" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_ATTENDANCE_TARGET_TYPE" :value="row.targetType" />
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="发生时间" prop="occurTime" width="180" />
          <el-table-column align="center" label="操作" width="120">
            <template #default="{ row }">
              <el-button v-hasPermi="['clubpoints:attendance:correct']" link type="primary" @click="openCorrect(row)">
                修正
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="attendanceQuery.pageSize"
          v-model:page="attendanceQuery.pageNo"
          :total="attendanceTotal"
          @pagination="getAttendanceList"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <Dialog v-model="correctVisible" :title="correctMode === 'supplement' ? '补录签到签退' : '修正签到签退'" width="560">
    <el-form ref="correctFormRef" :model="correctForm" :rules="correctRules" label-width="104px">
      <el-form-item label="报名ID" prop="registrationId">
        <el-input-number v-model="correctForm.registrationId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="目标类型" prop="targetType">
        <el-select v-model="correctForm.targetType" class="!w-240px">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_ATTENDANCE_TARGET_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="修正时间" prop="occurTime">
        <el-date-picker
          v-model="correctForm.occurTime"
          class="!w-240px"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input v-model="correctForm.reason" :rows="3" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="correctVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitCorrect">提交</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as AttendanceApi from '@/api/clubpoints/leader/attendance'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsLeaderAttendance' })

const message = useMessage()
const activeTab = ref('registration')

const registrationLoading = ref(false)
const registrationList = ref<any[]>([])
const registrationTotal = ref(0)
const registrationQuery = reactive({ pageNo: 1, pageSize: 10 })

const attendanceLoading = ref(false)
const attendanceList = ref<any[]>([])
const attendanceTotal = ref(0)
const attendanceQuery = reactive({ pageNo: 1, pageSize: 10 })

const correctVisible = ref(false)
const correctMode = ref<'supplement' | 'correct'>('supplement')
const submitLoading = ref(false)
const correctFormRef = ref()
const correctForm = reactive({
  id: undefined as number | undefined,
  registrationId: undefined as unknown as number,
  targetType: 1,
  occurTime: '',
  reason: ''
})
const correctRules = {
  registrationId: [{ required: true, message: '报名ID不能为空', trigger: 'blur' }],
  targetType: [{ required: true, message: '目标类型不能为空', trigger: 'change' }],
  occurTime: [{ required: true, message: '修正时间不能为空', trigger: 'change' }],
  reason: [{ required: true, message: '原因不能为空', trigger: 'blur' }]
}

const getRegistrationList = async () => {
  registrationLoading.value = true
  try {
    const data = await AttendanceApi.getLeaderRegistrationPage(registrationQuery)
    registrationList.value = data.list || []
    registrationTotal.value = data.total || 0
  } finally {
    registrationLoading.value = false
  }
}

const getAttendanceList = async () => {
  attendanceLoading.value = true
  try {
    const data = await AttendanceApi.getLeaderAttendancePage(attendanceQuery)
    attendanceList.value = data.list || []
    attendanceTotal.value = data.total || 0
  } finally {
    attendanceLoading.value = false
  }
}

const openSupplement = (row: any) => {
  correctMode.value = 'supplement'
  Object.assign(correctForm, {
    id: undefined,
    registrationId: row.id,
    targetType: 1,
    occurTime: '',
    reason: ''
  })
  correctVisible.value = true
}

const openCorrect = (row: any) => {
  correctMode.value = 'correct'
  Object.assign(correctForm, {
    id: row.id,
    registrationId: row.registrationId,
    targetType: row.targetType || 1,
    occurTime: row.occurTime,
    reason: ''
  })
  correctVisible.value = true
}

const submitCorrect = async () => {
  await correctFormRef.value?.validate()
  submitLoading.value = true
  try {
    if (correctMode.value === 'supplement') {
      await AttendanceApi.supplementLeaderAttendance(correctForm)
    } else {
      await AttendanceApi.correctLeaderAttendance(correctForm as AttendanceApi.LeaderAttendanceCorrectReqVO)
    }
    message.success('提交成功')
    correctVisible.value = false
    await Promise.all([getRegistrationList(), getAttendanceList()])
  } finally {
    submitLoading.value = false
  }
}

const markSpecialAbsence = async (row: any) => {
  try {
    const result = await message.prompt('请输入特殊缺席原因', '特殊缺席')
    await AttendanceApi.markLeaderSpecialAbsence({ id: row.id, reason: result.value })
    message.success('已标记特殊缺席')
    await getRegistrationList()
  } catch {}
}

onMounted(() => {
  getRegistrationList()
  getAttendanceList()
})
</script>

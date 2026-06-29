<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="签到签退会提交本地 clientTime；最终窗口判断以后端服务器北京时间为准。"
      type="info"
    />
    <el-tabs v-model="activeTab">
      <el-tab-pane label="活动列表" name="activity">
        <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
          <el-form-item label="关键词" prop="keyword">
            <el-input
              v-model="queryParams.keyword"
              class="!w-240px"
              clearable
              placeholder="活动标题"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="状态" prop="activityStatus">
            <el-select v-model="queryParams.activityStatus" class="!w-180px" clearable placeholder="请选择状态">
              <el-option
                v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS)"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery">
              <Icon class="mr-5px" icon="ep:search" />搜索
            </el-button>
            <el-button @click="resetQuery">
              <Icon class="mr-5px" icon="ep:refresh" />重置
            </el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="loading" :data="activityList" class="mt-20px">
          <el-table-column label="活动标题" min-width="220" prop="title" />
          <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
          <el-table-column label="地点" min-width="160" prop="location" />
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="开始时间" prop="startTime" width="180" />
          <el-table-column align="center" fixed="right" label="操作" width="260">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button
                v-hasPermi="['clubpoints:registration:create']"
                link
                type="success"
                @click="registerActivity(row)"
              >
                报名
              </el-button>
              <el-button
                v-hasPermi="['clubpoints:registration:cancel']"
                link
                type="warning"
                @click="cancelActivity(row)"
              >
                取消
              </el-button>
              <el-button
                v-hasPermi="['clubpoints:attendance:check-in']"
                link
                type="primary"
                @click="check(row, 'in')"
              >
                签到
              </el-button>
              <el-button
                v-hasPermi="['clubpoints:attendance:check-out']"
                link
                type="primary"
                @click="check(row, 'out')"
              >
                签退
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="queryParams.pageSize"
          v-model:page="queryParams.pageNo"
          :total="total"
          @pagination="getActivityList"
        />
      </el-tab-pane>

      <el-tab-pane label="我的报名" name="registration">
        <el-table v-loading="registrationLoading" :data="registrationList">
          <el-table-column label="活动" min-width="220" prop="activityTitleSnapshot" />
          <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
          <el-table-column align="center" label="报名状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_REGISTRATION_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="报名时间" prop="registerTime" width="180" />
          <el-table-column align="center" label="操作" width="180">
            <template #default="{ row }">
              <el-button link type="primary" @click="check(row, 'in')">签到</el-button>
              <el-button link type="primary" @click="check(row, 'out')">签退</el-button>
              <el-button link type="warning" @click="cancelActivity(row)">取消</el-button>
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
    </el-tabs>
  </ContentWrap>

  <Dialog v-model="detailVisible" title="活动详情" width="760">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="标题">{{ detail?.title }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS" :value="detail?.status" />
      </el-descriptions-item>
      <el-descriptions-item label="开始时间">{{ detail?.startTime }}</el-descriptions-item>
      <el-descriptions-item label="结束时间">{{ detail?.endTime }}</el-descriptions-item>
      <el-descriptions-item :span="2" label="说明">{{ detail?.description }}</el-descriptions-item>
    </el-descriptions>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ActivityApi from '@/api/clubpoints/app/activity'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAppActivity' })

const message = useMessage()

const activeTab = ref('activity')
const loading = ref(false)
const activityList = ref<ActivityApi.AppActivityRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive<ActivityApi.AppActivityPageReqVO>({
  pageNo: 1,
  pageSize: 10
})

const registrationLoading = ref(false)
const registrationList = ref<any[]>([])
const registrationTotal = ref(0)
const registrationQuery = reactive({ pageNo: 1, pageSize: 10 })

const detailVisible = ref(false)
const detail = ref<any>()

const getActivityList = async () => {
  loading.value = true
  try {
    const data = await ActivityApi.getAppActivityPage(queryParams)
    activityList.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const getRegistrationList = async () => {
  registrationLoading.value = true
  try {
    const data = await ActivityApi.getMyRegistrationPage(registrationQuery)
    registrationList.value = data.list || []
    registrationTotal.value = data.total || 0
  } finally {
    registrationLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getActivityList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const registrationIdOf = (row: any) => row.registrationId || row.myRegistrationId || row.id

const registerActivity = async (row: ActivityApi.AppActivityRespVO) => {
  await ActivityApi.createRegistration({ activityId: row.id })
  message.success('报名成功')
  await Promise.all([getActivityList(), getRegistrationList()])
}

const cancelActivity = async (row: any) => {
  const registrationId = registrationIdOf(row)
  if (!registrationId) {
    message.error('缺少报名记录，不能取消')
    return
  }
  try {
    const result = await message.prompt('请输入取消报名原因', '取消报名')
    await ActivityApi.cancelRegistration({ id: registrationId, reason: result.value })
    message.success('取消报名成功')
    await Promise.all([getActivityList(), getRegistrationList()])
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      message.error('取消报名失败，请重试')
    }
  }
}

const check = async (row: any, type: 'in' | 'out') => {
  const registrationId = registrationIdOf(row)
  if (!registrationId) {
    message.error('缺少报名记录，不能签到签退')
    return
  }
  const data = { registrationId, clientTime: new Date().toISOString() }
  if (type === 'in') {
    await ActivityApi.checkIn(data)
    message.success('签到成功')
  } else {
    await ActivityApi.checkOut(data)
    message.success('签退成功')
  }
  await getRegistrationList()
}

const openDetail = async (row: ActivityApi.AppActivityRespVO) => {
  detail.value = await ActivityApi.getAppActivity(row.id)
  detailVisible.value = true
}

onMounted(() => {
  getActivityList()
  getRegistrationList()
})
</script>

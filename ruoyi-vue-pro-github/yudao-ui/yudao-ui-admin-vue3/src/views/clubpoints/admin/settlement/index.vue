<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="手动重跑结算必须依赖后端 runKey 和流水幂等，前端只发起请求，不自行判断是否重复发分。"
      type="warning"
    />
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待结算活动" name="pending">
        <el-form :inline="true" :model="pendingQuery" class="-mb-15px" label-width="88px">
          <el-form-item label="俱乐部ID" prop="clubId">
            <el-input-number v-model="pendingQuery.clubId" :min="1" class="!w-200px" controls-position="right" />
          </el-form-item>
          <el-form-item>
            <el-button @click="getPendingList">
              <Icon class="mr-5px" icon="ep:search" />搜索
            </el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="pendingLoading" :data="pendingList" class="mt-20px">
          <el-table-column align="center" label="活动ID" prop="id" width="100" />
          <el-table-column label="活动标题" min-width="220" prop="title" />
          <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column
            :formatter="dateFormatter"
            align="center"
            label="开始时间"
            prop="startTime"
            width="180"
          />
          <el-table-column align="center" fixed="right" label="操作" width="140">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['clubpoints:settlement:run']"
                link
                type="primary"
                @click="runSettlement(row)"
              >
                运行结算
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="pendingQuery.pageSize"
          v-model:page="pendingQuery.pageNo"
          :total="pendingTotal"
          @pagination="getPendingList"
        />
      </el-tab-pane>

      <el-tab-pane label="结算运行记录" name="run">
        <el-form :inline="true" :model="runQuery" class="-mb-15px" label-width="88px">
          <el-form-item label="活动ID" prop="activityId">
            <el-input-number v-model="runQuery.activityId" :min="1" class="!w-200px" controls-position="right" />
          </el-form-item>
          <el-form-item>
            <el-button @click="getRunList">
              <Icon class="mr-5px" icon="ep:search" />搜索
            </el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="runLoading" :data="runList" class="mt-20px">
          <el-table-column align="center" label="运行ID" prop="id" width="100" />
          <el-table-column align="center" label="活动ID" prop="activityId" width="100" />
          <el-table-column align="center" label="状态" prop="status" width="130">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_ACTIVITY_SETTLEMENT_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column label="结果" min-width="260" prop="resultJson" show-overflow-tooltip />
          <el-table-column
            :formatter="dateFormatter"
            align="center"
            label="创建时间"
            prop="createTime"
            width="180"
          />
          <el-table-column align="center" fixed="right" label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="runQuery.pageSize"
          v-model:page="runQuery.pageNo"
          :total="runTotal"
          @pagination="getRunList"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <Dialog v-model="detailVisible" title="结算明细" width="760">
    <pre class="club-points-settlement-detail">{{ detailText }}</pre>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as SettlementApi from '@/api/clubpoints/admin/settlement'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminSettlement' })

const message = useMessage()

const activeTab = ref<'pending' | 'run'>('pending')
const pendingLoading = ref(false)
const pendingList = ref<any[]>([])
const pendingTotal = ref(0)
const pendingQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  clubId: undefined as number | undefined
})

const runLoading = ref(false)
const runList = ref<SettlementApi.AdminSettlementRunRespVO[]>([])
const runTotal = ref(0)
const runQuery = reactive({
  pageNo: 1,
  pageSize: 10,
  activityId: undefined as number | undefined
})

const detailVisible = ref(false)
const detailText = ref('')

const getPendingList = async () => {
  pendingLoading.value = true
  try {
    const data = await SettlementApi.getPendingActivityPage(pendingQuery)
    pendingList.value = data.list || []
    pendingTotal.value = data.total || 0
  } finally {
    pendingLoading.value = false
  }
}

const getRunList = async () => {
  runLoading.value = true
  try {
    const data = await SettlementApi.getSettlementRunPage(runQuery)
    runList.value = data.list || []
    runTotal.value = data.total || 0
  } finally {
    runLoading.value = false
  }
}

const handleTabChange = () => {
  if (activeTab.value === 'pending') {
    getPendingList()
  } else {
    getRunList()
  }
}

const runSettlement = async (row: any) => {
  try {
    const result = await message.prompt(`请输入运行活动 ${row.title || row.id} 结算的原因`, '运行结算')
    await SettlementApi.runSettlement({ activityId: row.id, reason: result.value })
    message.success('结算已提交')
    await Promise.all([getPendingList(), getRunList()])
  } catch {}
}

const openDetail = async (row: SettlementApi.AdminSettlementRunRespVO) => {
  const data = await SettlementApi.getSettlementDetail(row.id)
  detailText.value = JSON.stringify(data || row, null, 2)
  detailVisible.value = true
}

onMounted(getPendingList)
</script>

<style lang="scss" scoped>
.club-points-settlement-detail {
  max-height: 520px;
  padding: 12px;
  overflow: auto;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}
</style>

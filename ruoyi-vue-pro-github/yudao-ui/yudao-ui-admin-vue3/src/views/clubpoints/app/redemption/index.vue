<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="兑换申请提交会生成提交编号；失败重试不重新生成。资格、库存、积分不足均以后端返回为准。"
      type="info"
    />
    <el-tabs v-model="activeTab">
      <el-tab-pane label="开放批次" name="batch">
        <el-table v-loading="batchLoading" :data="batchList">
          <el-table-column label="批次名称" min-width="220" prop="name" />
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_BATCH_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="开始时间" prop="startTime" width="180" />
          <el-table-column :formatter="dateFormatter" align="center" label="结束时间" prop="endTime" width="180" />
          <el-table-column align="center" label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="selectBatch(row)">查看礼品</el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="batchQuery.pageSize"
          v-model:page="batchQuery.pageNo"
          :total="batchTotal"
          @pagination="getBatchList"
        />
      </el-tab-pane>

      <el-tab-pane label="礼品" name="gift">
        <el-empty v-if="!selectedBatchId" description="请先选择批次" />
        <el-table v-else v-loading="giftLoading" :data="giftList">
          <el-table-column label="礼品名称" min-width="220" prop="name" />
          <el-table-column align="center" label="消耗积分" prop="pointsCost" width="120">
            <template #default="{ row }">
              <PointAmount :show-sign="false" :value="row.pointsCost" />
            </template>
          </el-table-column>
          <el-table-column align="center" label="库存" width="160">
            <template #default="{ row }">
              {{ row.stockUsed || 0 }} / {{ row.stockTotal || 0 }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="状态" prop="status" width="120">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_GIFT_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="120">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['clubpoints:redemption:apply']"
                link
                type="primary"
                @click="openApplyDialog(row)"
              >
                申请
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="我的兑换" name="mine">
        <el-table v-loading="myLoading" :data="myList">
          <el-table-column label="申请单号" min-width="180" prop="applicationNo" />
          <el-table-column align="center" label="消耗积分" prop="points" width="120" />
          <el-table-column align="center" label="状态" prop="status" width="140">
            <template #default="{ row }">
              <StatusTag :type="DICT_TYPE.CLUB_POINTS_REDEMPTION_APPLICATION_STATUS" :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column :formatter="dateFormatter" align="center" label="申请时间" prop="createTime" width="180" />
          <el-table-column align="center" label="操作" width="120">
            <template #default="{ row }">
              <el-button
                v-hasPermi="['clubpoints:redemption:cancel-own']"
                link
                type="danger"
                @click="cancelApplication(row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          v-model:limit="myQuery.pageSize"
          v-model:page="myQuery.pageNo"
          :total="myTotal"
          @pagination="getMyList"
        />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

  <Dialog v-model="applyDialogVisible" title="兑换申请" width="560">
    <el-form :model="applyForm" label-width="96px">
      <el-form-item label="提交编号">
        <el-input v-model="applyForm.requestNo" disabled />
      </el-form-item>
      <el-form-item label="礼品">
        <el-input :model-value="selectedGift?.name" disabled />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="applyForm.remark" :rows="3" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="applyDialogVisible = false">取消</el-button>
      <el-button :loading="applyLoading" type="primary" @click="submitApply">提交申请</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as RedemptionApi from '@/api/clubpoints/app/redemption'
import PointAmount from '@/views/clubpoints/components/PointAmount.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAppRedemption' })

const message = useMessage()

const activeTab = ref('batch')
const batchLoading = ref(false)
const batchList = ref<RedemptionApi.AppRedemptionBatchRespVO[]>([])
const batchTotal = ref(0)
const batchQuery = reactive({ pageNo: 1, pageSize: 10 })

const selectedBatchId = ref<number>()
const giftLoading = ref(false)
const giftList = ref<RedemptionApi.AppRedemptionGiftRespVO[]>([])

const myLoading = ref(false)
const myList = ref<RedemptionApi.AppRedemptionApplicationRespVO[]>([])
const myTotal = ref(0)
const myQuery = reactive({ pageNo: 1, pageSize: 10 })

const applyDialogVisible = ref(false)
const applyLoading = ref(false)
const selectedGift = ref<RedemptionApi.AppRedemptionGiftRespVO>()
const applyForm = reactive<RedemptionApi.AppRedemptionApplyReqVO>({
  batchId: 0,
  giftId: 0,
  quantity: 1,
  requestNo: '',
  remark: ''
})

const getBatchList = async () => {
  batchLoading.value = true
  try {
    const data = await RedemptionApi.getOpenBatchPage(batchQuery)
    batchList.value = data.list || []
    batchTotal.value = data.total || 0
  } finally {
    batchLoading.value = false
  }
}

const selectBatch = async (row: RedemptionApi.AppRedemptionBatchRespVO) => {
  selectedBatchId.value = row.id
  activeTab.value = 'gift'
  await getGiftList()
}

const getGiftList = async () => {
  if (!selectedBatchId.value) {
    return
  }
  giftLoading.value = true
  try {
    const data = await RedemptionApi.getGiftPage({
      pageNo: 1,
      pageSize: 100,
      batchId: selectedBatchId.value
    })
    giftList.value = data.list || []
  } finally {
    giftLoading.value = false
  }
}

const getMyList = async () => {
  myLoading.value = true
  try {
    const data = await RedemptionApi.getMyRedemptionPage(myQuery)
    myList.value = data.list || []
    myTotal.value = data.total || 0
  } finally {
    myLoading.value = false
  }
}

const openApplyDialog = (row: RedemptionApi.AppRedemptionGiftRespVO) => {
  selectedGift.value = row
  const contextKey = `${row.batchId}:${row.id}`
  Object.assign(applyForm, {
    batchId: row.batchId,
    giftId: row.id,
    quantity: 1,
    requestNo: RedemptionApi.resetRedemptionApplyRequestNo(contextKey),
    remark: ''
  })
  applyDialogVisible.value = true
}

const submitApply = async () => {
  applyLoading.value = true
  try {
    await RedemptionApi.applyRedemption(applyForm)
    message.success('兑换申请已提交')
    applyDialogVisible.value = false
    await getMyList()
  } finally {
    applyLoading.value = false
  }
}

const cancelApplication = async (row: RedemptionApi.AppRedemptionApplicationRespVO) => {
  try {
    const result = await message.prompt('请输入取消兑换原因', '取消兑换')
    await RedemptionApi.cancelRedemption({ id: row.id, reason: result.value })
    message.success('取消成功')
    await getMyList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      message.error('取消兑换失败，请重试')
    }
  }
}

onMounted(() => {
  getBatchList()
  getMyList()
})
</script>

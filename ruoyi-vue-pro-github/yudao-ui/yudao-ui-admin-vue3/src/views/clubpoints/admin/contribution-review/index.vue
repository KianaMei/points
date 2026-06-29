<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="材料审核通过后由后端锁定附件并按明细发分；页面不允许审核时修改材料内容。"
      type="warning"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-180px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_MATERIAL_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="俱乐部" prop="clubId">
        <el-input-number v-model="queryParams.clubId" :min="1" class="!w-180px" controls-position="right" />
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
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="材料ID" prop="id" width="100" />
      <el-table-column label="标题" min-width="220" prop="title" />
      <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_MATERIAL_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="提交时间" prop="submitTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button v-hasPermi="['clubpoints:contribution:review']" link type="success" @click="openReview(row)">
            审核
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <Dialog v-model="detailVisible" title="材料详情" width="860">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="材料ID">{{ detailData?.id }}</el-descriptions-item>
      <el-descriptions-item label="标题">{{ detailData?.title }}</el-descriptions-item>
      <el-descriptions-item label="俱乐部">{{ detailData?.clubNameSnapshot || detailData?.clubId }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <StatusTag v-if="detailData" :type="DICT_TYPE.CLUB_POINTS_MATERIAL_STATUS" :value="detailData.status" />
      </el-descriptions-item>
      <el-descriptions-item label="说明" :span="2">{{ detailData?.description || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-table :data="detailData?.items || []" class="mt-16px">
      <el-table-column label="员工" min-width="160" prop="userNameSnapshot">
        <template #default="{ row }">{{ row.userNameSnapshot || row.userId }}</template>
      </el-table-column>
      <el-table-column label="规则项" min-width="160" prop="ruleItemCode" />
      <el-table-column align="center" label="积分" prop="points" width="120" />
      <el-table-column label="原因" min-width="200" prop="reason" show-overflow-tooltip />
      <el-table-column label="材料摘要" min-width="200" prop="materialSummary" show-overflow-tooltip />
    </el-table>
  </Dialog>

  <ReviewDialog ref="reviewRef" title="材料审核" @submit="submitReview" />
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ContributionApi from '@/api/clubpoints/admin/contribution'
import type { ReviewReqVO } from '@/api/clubpoints/shared/types'
import ReviewDialog from '@/views/clubpoints/components/ReviewDialog.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminContributionReview' })

const message = useMessage()

const loading = ref(false)
const list = ref<ContributionApi.AdminContributionRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  clubId: undefined as number | undefined
})

const detailVisible = ref(false)
const detailData = ref<any>()
const reviewRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await ContributionApi.getContributionReviewPage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openDetail = async (row: ContributionApi.AdminContributionRespVO) => {
  detailData.value = await ContributionApi.getContribution(row.id)
  detailVisible.value = true
}

const openReview = (row: ContributionApi.AdminContributionRespVO) => {
  reviewRef.value?.open({ id: row.id, subjectName: row.title, approved: true })
}

const submitReview = async (payload: ReviewReqVO) => {
  await ContributionApi.reviewContribution(payload)
  message.success('材料审核已提交')
  await getList()
}

onMounted(getList)
</script>

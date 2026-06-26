<template>
  <ContentWrap>
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.key" :lg="6" :md="12" :sm="12" :xs="24">
        <el-card class="dashboard-card" shadow="hover" @click="go(card.path, card.queryJson)">
          <div class="dashboard-card__label">{{ card.label }}</div>
          <div class="dashboard-card__value">{{ card.value }}</div>
          <div class="dashboard-card__hint">{{ card.hint }}</div>
        </el-card>
      </el-col>
    </el-row>
  </ContentWrap>

  <ContentWrap>
    <template #header>
      <div class="flex items-center justify-between">
        <span>管理员待办</span>
        <el-tag type="warning">{{ summary.todoCount || 0 }}</el-tag>
      </div>
    </template>
    <el-table v-loading="loading" :data="summary.todoItems || []">
      <el-table-column label="待办" min-width="180" prop="name" />
      <el-table-column align="center" label="数量" prop="count" width="120" />
      <el-table-column align="center" label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="go(row.path, row.queryJson)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as DashboardApi from '@/api/clubpoints/admin/dashboard'

defineOptions({ name: 'ClubPointsAdminDashboard' })

const router = useRouter()
const loading = ref(false)
const summary = reactive<DashboardApi.AdminDashboardSummaryRespVO>({
  pendingActivityReviewCount: 0,
  pendingContributionReviewCount: 0,
  pendingRedemptionReviewCount: 0,
  pendingDisputeCount: 0,
  todoCount: 0,
  todoItems: []
})

const cards = computed(() => [
  {
    key: 'activity',
    label: '待审核活动',
    value: summary.pendingActivityReviewCount || 0,
    hint: '负责人提交活动',
    path: '/clubpoints/admin/activity',
    queryJson: '{"status":2}'
  },
  {
    key: 'contribution',
    label: '待审核材料',
    value: summary.pendingContributionReviewCount || 0,
    hint: '非签到积分材料',
    path: '/clubpoints/admin/contribution-review',
    queryJson: '{"status":2}'
  },
  {
    key: 'redemption',
    label: '待审核兑换',
    value: summary.pendingRedemptionReviewCount || 0,
    hint: '审核只通过或拒绝',
    path: '/clubpoints/admin/redemption-application',
    queryJson: '{"status":1}'
  },
  {
    key: 'dispute',
    label: '待处理异议',
    value: summary.pendingDisputeCount || 0,
    hint: '处理后通知员工',
    path: '/clubpoints/admin/dispute',
    queryJson: '{"status":1}'
  }
])

const getSummary = async () => {
  loading.value = true
  try {
    Object.assign(summary, await DashboardApi.getAdminDashboardSummary())
  } finally {
    loading.value = false
  }
}

const go = (path: string, queryJson?: string) => {
  router.push({ path, query: queryJson ? JSON.parse(queryJson) : undefined })
}

onMounted(getSummary)
</script>

<style lang="scss" scoped>
.dashboard-card {
  margin-bottom: 16px;
  cursor: pointer;

  &__label {
    color: var(--el-text-color-secondary);
  }

  &__value {
    margin: 10px 0;
    font-size: 28px;
    font-weight: 700;
  }

  &__hint {
    color: var(--el-text-color-placeholder);
  }
}
</style>

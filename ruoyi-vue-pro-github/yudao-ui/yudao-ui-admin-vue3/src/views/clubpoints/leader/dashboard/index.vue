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
        <span>负责人待办</span>
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
import * as DashboardApi from '@/api/clubpoints/leader/dashboard'

defineOptions({ name: 'ClubPointsLeaderDashboard' })

const router = useRouter()
const loading = ref(false)
const summary = reactive<DashboardApi.LeaderDashboardSummaryRespVO>({
  managedClubCount: 0,
  draftActivityCount: 0,
  rejectedActivityCount: 0,
  attendanceExceptionCount: 0,
  pendingContributionSubmitCount: 0,
  todoCount: 0,
  todoItems: []
})

const cards = computed(() => [
  {
    key: 'club',
    label: '负责俱乐部',
    value: summary.managedClubCount || 0,
    hint: '只显示自己负责的俱乐部',
    path: '/clubpoints/leader/club'
  },
  {
    key: 'draft',
    label: '草稿活动',
    value: summary.draftActivityCount || 0,
    hint: '待提交审核',
    path: '/clubpoints/leader/activity',
    queryJson: '{"status":1}'
  },
  {
    key: 'rejected',
    label: '被驳回活动',
    value: summary.rejectedActivityCount || 0,
    hint: '需要修改后重新提交',
    path: '/clubpoints/leader/activity',
    queryJson: '{"status":3}'
  },
  {
    key: 'contribution',
    label: '待提交材料',
    value: summary.pendingContributionSubmitCount || 0,
    hint: `签到异常 ${summary.attendanceExceptionCount || 0} 个`,
    path: '/clubpoints/leader/contribution',
    queryJson: '{"status":"editable"}'
  }
])

const getSummary = async () => {
  loading.value = true
  try {
    Object.assign(summary, await DashboardApi.getLeaderDashboardSummary())
  } catch {
  } finally {
    loading.value = false
  }
}

const go = (path: string, queryJson?: string) => {
  const query = queryJson ? JSON.parse(queryJson) : undefined
  router.push({ path, query })
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

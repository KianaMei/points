<template>
  <div class="club-points-app-dashboard">
    <ContentWrap>
      <el-row :gutter="16">
        <el-col v-for="card in cards" :key="card.key" :lg="6" :md="12" :sm="12" :xs="24">
          <el-card class="dashboard-card" shadow="hover" @click="go(card.path)">
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
          <span>我的待办</span>
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
  </div>
</template>

<script lang="ts" setup>
import * as DashboardApi from '@/api/clubpoints/app/dashboard'

defineOptions({ name: 'ClubPointsAppDashboard' })

const router = useRouter()
const loading = ref(false)
const summary = reactive<DashboardApi.AppDashboardSummaryRespVO>({
  availablePoints: 0,
  frozenPoints: 0,
  totalEarnedPoints: 0,
  joinedClubCount: 0,
  registeredActivityCount: 0,
  pendingRedemptionCount: 0,
  unreadNotifyCount: 0,
  todoCount: 0,
  todoItems: []
})

const cards = computed(() => [
  {
    key: 'points',
    label: '可用积分',
    value: `${summary.availablePoints || 0} 分`,
    hint: `冻结 ${summary.frozenPoints || 0} 分，年度累计 ${summary.totalEarnedPoints || 0} 分`,
    path: '/clubpoints/app/ledger'
  },
  {
    key: 'club',
    label: '我的俱乐部',
    value: summary.joinedClubCount || 0,
    hint: '查看已加入和可加入俱乐部',
    path: '/clubpoints/app/club'
  },
  {
    key: 'activity',
    label: '已报名活动',
    value: summary.registeredActivityCount || 0,
    hint: '报名、签到、签退入口',
    path: '/clubpoints/app/activity'
  },
  {
    key: 'notify',
    label: '未读通知',
    value: summary.unreadNotifyCount || 0,
    hint: `待审核兑换 ${summary.pendingRedemptionCount || 0} 个`,
    path: '/clubpoints/app/notify'
  }
])

const getSummary = async () => {
  loading.value = true
  try {
    Object.assign(summary, await DashboardApi.getAppDashboardSummary())
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

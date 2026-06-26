<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="已读状态" prop="readStatus">
        <el-select v-model="queryParams.readStatus" class="!w-180px" clearable placeholder="全部">
          <el-option :value="false" label="未读" />
          <el-option :value="true" label="已读" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
        <el-button :disabled="selectedIds.length === 0" type="primary" @click="markRead">
          标记已读
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="通知只支持已读 / 未读，不提供删除。"
      type="info"
    />
    <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column label="发送方" min-width="140" prop="templateNickname" />
      <el-table-column label="内容" min-width="320" prop="templateContent" show-overflow-tooltip />
      <el-table-column align="center" label="状态" prop="readStatus" width="100">
        <template #default="{ row }">
          <el-tag :type="row.readStatus ? 'info' : 'warning'">
            {{ row.readStatus ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column :formatter="dateFormatter" align="center" label="已读时间" prop="readTime" width="180" />
    </el-table>
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as NotifyApi from '@/api/clubpoints/app/notify'

defineOptions({ name: 'ClubPointsAppNotify' })

const message = useMessage()
const loading = ref(false)
const list = ref<NotifyApi.AppNotifyMessageRespVO[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  readStatus: undefined as boolean | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await NotifyApi.getMyNotifyPage(queryParams)
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

const handleSelectionChange = (rows: NotifyApi.AppNotifyMessageRespVO[]) => {
  selectedIds.value = rows.map((row) => row.id)
}

const markRead = async () => {
  await NotifyApi.updateNotifyRead(selectedIds.value)
  message.success('已标记为已读')
  selectedIds.value = []
  await getList()
}

onMounted(getList)
</script>

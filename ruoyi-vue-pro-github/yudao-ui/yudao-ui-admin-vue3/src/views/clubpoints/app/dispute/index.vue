<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="异议不会直接修改积分；管理员处理后会展示回复和关联调整 / 撤销流水。"
      type="info"
    />
    <el-button type="primary" @click="openCreateDialog">
      <Icon class="mr-5px" icon="ep:plus" />提交异议
    </el-button>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="标题" min-width="220" prop="title" />
      <el-table-column align="center" label="目标类型" prop="targetType" width="120" />
      <el-table-column align="center" label="目标ID" prop="targetId" width="120" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_DISPUTE_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="管理员回复" min-width="260" prop="replyContent" show-overflow-tooltip />
      <el-table-column :formatter="dateFormatter" align="center" label="提交时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

  <Dialog v-model="createVisible" title="提交异议" width="680">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="96px">
      <el-form-item label="目标类型" prop="targetType">
        <el-input-number v-model="formData.targetType" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="目标ID" prop="targetId">
        <el-input-number v-model="formData.targetId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" placeholder="请输入异议标题" />
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="formData.content" :rows="4" type="textarea" />
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="formData.attachments" directory="clubpoints/dispute" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitDispute">提交</el-button>
    </template>
  </Dialog>

  <Dialog v-model="detailVisible" title="异议详情" width="720">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="标题">{{ detail?.title }}</el-descriptions-item>
      <el-descriptions-item label="内容">{{ detail?.content }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <StatusTag :type="DICT_TYPE.CLUB_POINTS_DISPUTE_STATUS" :value="detail?.status" />
      </el-descriptions-item>
      <el-descriptions-item label="管理员回复">{{ detail?.replyContent || '-' }}</el-descriptions-item>
      <el-descriptions-item label="关联流水">{{ detail?.relatedTransactionId || '-' }}</el-descriptions-item>
    </el-descriptions>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as DisputeApi from '@/api/clubpoints/app/dispute'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAppDispute' })

const message = useMessage()
const loading = ref(false)
const list = ref<DisputeApi.AppDisputeRespVO[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10 })

const createVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<DisputeApi.AppDisputeCreateReqVO>({
  targetType: 1,
  targetId: undefined as unknown as number,
  title: '',
  content: '',
  attachments: []
})
const formRules = {
  targetType: [{ required: true, message: '目标类型不能为空', trigger: 'blur' }],
  targetId: [{ required: true, message: '目标ID不能为空', trigger: 'blur' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '内容不能为空', trigger: 'blur' }]
}

const detailVisible = ref(false)
const detail = ref<any>()

const getList = async () => {
  loading.value = true
  try {
    const data = await DisputeApi.getMyDisputePage(queryParams)
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  Object.assign(formData, {
    targetType: 1,
    targetId: undefined,
    title: '',
    content: '',
    attachments: []
  })
  createVisible.value = true
}

const submitDispute = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await DisputeApi.createDispute(formData)
    message.success('异议已提交')
    createVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const openDetail = async (row: DisputeApi.AppDisputeRespVO) => {
  detail.value = await DisputeApi.getDispute(row.id)
  detailVisible.value = true
}

onMounted(getList)
</script>

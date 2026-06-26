<template>
  <ContentWrap>
    <el-alert
      :closable="false"
      class="mb-16px"
      show-icon
      title="材料审核通过后锁定只读；负责人不能审核材料，只能提交或撤回材料。"
      type="info"
    />
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="负责俱乐部" prop="clubId">
        <div class="!w-240px">
          <ClubSelect v-model="queryParams.clubId" :options="clubOptions" />
        </div>
      </el-form-item>
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
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />重置
        </el-button>
        <el-button v-hasPermi="['clubpoints:contribution:submit']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />新建材料
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="标题" min-width="220" prop="title" />
      <el-table-column label="俱乐部" min-width="160" prop="clubNameSnapshot" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_MATERIAL_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column :formatter="dateFormatter" align="center" label="创建时间" prop="createTime" width="180" />
      <el-table-column align="center" fixed="right" label="操作" width="240">
        <template #default="{ row }">
          <el-button v-hasPermi="['clubpoints:contribution:submit']" link type="primary" @click="openForm('update', row)">
            编辑
          </el-button>
          <el-button v-hasPermi="['clubpoints:contribution:submit']" link type="success" @click="submitMaterial(row)">
            提交
          </el-button>
          <el-button v-hasPermi="['clubpoints:contribution:withdraw']" link type="warning" @click="withdrawMaterial(row)">
            撤回
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新建材料' : '编辑材料'" width="860">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="104px">
      <el-form-item label="负责俱乐部" prop="clubId">
        <ClubSelect v-model="formData.clubId" :options="clubOptions" />
      </el-form-item>
      <el-form-item label="材料类型">
        <el-input-number v-model="formData.type" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="formData.title" />
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="formData.description" :rows="3" type="textarea" />
      </el-form-item>
      <el-form-item label="规则版本" prop="ruleVersionId">
        <el-input-number v-model="formData.ruleVersionId" :min="1" class="!w-240px" controls-position="right" />
      </el-form-item>
      <el-form-item label="积分明细">
        <el-button link type="primary" @click="addItem">添加明细</el-button>
        <el-table :data="formData.items" class="mt-8px">
          <el-table-column label="员工" min-width="180">
            <template #default="{ row }">
              <UserPicker v-model="row.userId" />
            </template>
          </el-table-column>
          <el-table-column label="规则项编码" min-width="160">
            <template #default="{ row }">
              <el-input v-model="row.ruleItemCode" />
            </template>
          </el-table-column>
          <el-table-column label="积分" width="130">
            <template #default="{ row }">
              <el-input-number v-model="row.points" :min="1" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="原因" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.reason" />
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="90">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="formData.attachments" directory="clubpoints/contribution" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitForm">保存草稿</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as ContributionApi from '@/api/clubpoints/leader/contribution'
import * as ClubApi from '@/api/clubpoints/leader/club'
import type { ClubPointClubOption } from '@/api/clubpoints/shared/types'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import ClubSelect from '@/views/clubpoints/components/ClubSelect.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'
import UserPicker from '@/views/clubpoints/components/UserPicker.vue'

defineOptions({ name: 'ClubPointsLeaderContribution' })

const message = useMessage()
const loading = ref(false)
const list = ref<ContributionApi.LeaderContributionRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  clubId: undefined as number | undefined,
  status: undefined as number | undefined
})
const clubOptions = ref<ClubPointClubOption[]>([])

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<ContributionApi.LeaderContributionSaveReqVO>({
  clubId: undefined as unknown as number,
  type: undefined,
  ruleVersionId: undefined as unknown as number,
  title: '',
  description: '',
  reason: '',
  items: [],
  attachments: []
})
const formRules = {
  clubId: [{ required: true, message: '俱乐部不能为空', trigger: 'change' }],
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  ruleVersionId: [{ required: true, message: '规则版本不能为空', trigger: 'blur' }]
}

const loadClubOptions = async () => {
  if (clubOptions.value.length > 0) {
    return
  }
  const clubs = await ClubApi.getMyManagedClubList()
  clubOptions.value = clubs.map((club) => ({
    id: club.id,
    name: club.name,
    code: club.code,
    status: club.status
  }))
}

const ensureDefaultClub = async () => {
  await loadClubOptions()
  if (!queryParams.clubId && clubOptions.value.length > 0) {
    queryParams.clubId = clubOptions.value[0].id
  }
}

const getList = async () => {
  loading.value = true
  try {
    await ensureDefaultClub()
    if (!queryParams.clubId) {
      list.value = []
      total.value = 0
      return
    }
    const data = await ContributionApi.getLeaderContributionPage(queryParams)
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

const openForm = async (type: 'create' | 'update', row?: ContributionApi.LeaderContributionRespVO) => {
  formType.value = type
  await loadClubOptions()
  const detail = row ? await ContributionApi.getLeaderContribution(row.id) : undefined
  Object.assign(formData, {
    id: detail?.id,
    clubId: detail?.clubId,
    type: detail?.type,
    ruleVersionId: detail?.ruleVersionId,
    title: detail?.title || '',
    description: detail?.description || '',
    reason: '',
    items: detail?.items || [],
    attachments: detail?.attachments || []
  })
  if (formData.items.length === 0) {
    addItem()
  }
  formVisible.value = true
}

const addItem = () => {
  formData.items.push({
    userId: undefined as unknown as number,
    ruleItemCode: '',
    points: 1,
    reason: '',
    materialSummary: ''
  })
}

const removeItem = (index: number) => {
  formData.items.splice(index, 1)
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (formData.items.length === 0) {
    message.error('积分明细至少一条')
    return
  }
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await ContributionApi.createLeaderContribution(formData)
    } else {
      await ContributionApi.updateLeaderContribution(formData)
    }
    message.success('材料已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const submitMaterial = async (row: ContributionApi.LeaderContributionRespVO) => {
  try {
    const result = await message.prompt('请输入提交审核原因', '提交材料')
    await ContributionApi.submitLeaderContribution({ id: row.id, reason: result.value })
    message.success('已提交')
    await getList()
  } catch {}
}

const withdrawMaterial = async (row: ContributionApi.LeaderContributionRespVO) => {
  try {
    const result = await message.prompt('请输入撤回原因', '撤回材料')
    await ContributionApi.withdrawLeaderContribution({ id: row.id, reason: result.value })
    message.success('已撤回')
    await getList()
  } catch {}
}

onMounted(getList)
</script>

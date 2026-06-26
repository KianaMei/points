<template>
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
      <el-form-item label="版本名称" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          placeholder="请输入版本名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-200px" clearable placeholder="请选择状态">
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.CLUB_POINTS_RULE_VERSION_STATUS)"
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
        <el-button
          v-hasPermi="['clubpoints:rule:manage']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />新增版本
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="版本号" prop="versionNo" width="130" />
      <el-table-column label="版本名称" min-width="180" prop="name" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_RULE_VERSION_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="公示时间"
        prop="publicityTime"
        width="180"
      />
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="生效时间"
        prop="effectiveTime"
        width="180"
      />
      <el-table-column label="摘要" min-width="220" prop="summary" show-overflow-tooltip />
      <el-table-column align="center" fixed="right" label="操作" width="300">
        <template #default="{ row }">
          <el-button link type="primary" @click="openItemDialog(row)">规则项</el-button>
          <el-button
            v-hasPermi="['clubpoints:rule:manage']"
            link
            type="primary"
            @click="openForm('update', row)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:rule:manage']"
            link
            type="success"
            @click="handleRuleAction(row, 'publish')"
          >
            发布
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:rule:manage']"
            link
            type="warning"
            @click="handleRuleAction(row, 'withdraw')"
          >
            撤回
          </el-button>
          <el-button
            v-hasPermi="['clubpoints:rule:manage']"
            link
            type="danger"
            @click="handleRuleAction(row, 'disable')"
          >
            停用
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

  <Dialog v-model="formVisible" :title="formType === 'create' ? '新增规则版本' : '编辑规则版本'" width="760">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="104px">
      <el-form-item label="版本号" prop="versionNo">
        <el-input v-model="formData.versionNo" placeholder="例如 V2026.02" />
      </el-form-item>
      <el-form-item label="版本名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入版本名称" />
      </el-form-item>
      <el-form-item label="公示时间">
        <el-date-picker
          v-model="formData.publicityTime"
          class="!w-full"
          placeholder="请选择公示时间"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="生效时间">
        <el-date-picker
          v-model="formData.effectiveTime"
          class="!w-full"
          placeholder="请选择生效时间"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="formData.summary" :rows="2" type="textarea" />
      </el-form-item>
      <el-form-item label="制度内容">
        <el-input v-model="formData.content" :rows="4" type="textarea" />
      </el-form-item>
      <el-form-item label="附件">
        <AttachmentInput v-model="formData.attachments" directory="clubpoints/rule" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button :loading="submitLoading" type="primary" @click="submitForm">保存</el-button>
    </template>
  </Dialog>

  <Dialog v-model="itemDialogVisible" title="规则项" width="900">
    <el-table v-loading="itemLoading" :data="ruleItems">
      <el-table-column label="编码" min-width="180" prop="itemCode" />
      <el-table-column label="名称" min-width="180" prop="itemName" />
      <el-table-column align="center" label="默认分" prop="defaultPoints" width="100" />
      <el-table-column align="center" label="最小分" prop="minPoints" width="100" />
      <el-table-column align="center" label="最大分" prop="maxPoints" width="100" />
      <el-table-column align="center" label="状态" prop="status" width="120">
        <template #default="{ row }">
          <StatusTag :type="DICT_TYPE.CLUB_POINTS_RULE_ITEM_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip />
    </el-table>
  </Dialog>
</template>

<script lang="ts" setup>
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import * as RuleApi from '@/api/clubpoints/admin/rule'
import AttachmentInput from '@/views/clubpoints/components/AttachmentInput.vue'
import StatusTag from '@/views/clubpoints/components/StatusTag.vue'

defineOptions({ name: 'ClubPointsAdminRule' })

const message = useMessage()

const loading = ref(false)
const list = ref<RuleApi.RuleVersionRespVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined as string | undefined,
  status: undefined as number | undefined
})

const formVisible = ref(false)
const formType = ref<'create' | 'update'>('create')
const submitLoading = ref(false)
const formRef = ref()
const formData = reactive<RuleApi.RuleVersionSaveReqVO>({
  name: '',
  versionNo: '',
  summary: '',
  content: '',
  attachments: []
})
const formRules = {
  name: [{ required: true, message: '版本名称不能为空', trigger: 'blur' }]
}

const itemDialogVisible = ref(false)
const itemLoading = ref(false)
const ruleItems = ref<RuleApi.RuleItemSaveReqVO[]>([])

const getList = async () => {
  loading.value = true
  try {
    const data = await RuleApi.getRuleVersionPage(queryParams)
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

const openForm = (type: 'create' | 'update', row?: RuleApi.RuleVersionRespVO) => {
  formType.value = type
  Object.assign(formData, {
    id: row?.id,
    versionNo: row?.versionNo || '',
    name: row?.name || '',
    publicityTime: row?.publicityTime,
    effectiveTime: row?.effectiveTime,
    summary: row?.summary || '',
    content: row?.content || '',
    attachments: row?.attachments || []
  })
  formVisible.value = true
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formType.value === 'create') {
      await RuleApi.createRuleVersion(formData)
    } else {
      await RuleApi.updateRuleVersion(formData)
    }
    message.success('规则版本已保存')
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleRuleAction = async (
  row: RuleApi.RuleVersionRespVO,
  action: 'publish' | 'withdraw' | 'disable'
) => {
  const actionText = action === 'publish' ? '发布' : action === 'withdraw' ? '撤回' : '停用'
  try {
    const result = await message.prompt(`请输入${actionText}原因`, '操作确认')
    const data = { id: row.id, reason: result.value }
    if (action === 'publish') {
      await RuleApi.publishRuleVersion(data)
    } else if (action === 'withdraw') {
      await RuleApi.withdrawRuleVersion(data)
    } else {
      await RuleApi.disableRuleVersion(data)
    }
    message.success(`${actionText}成功`)
    await getList()
  } catch {}
}

const openItemDialog = async (row: RuleApi.RuleVersionRespVO) => {
  itemDialogVisible.value = true
  itemLoading.value = true
  try {
    ruleItems.value = await RuleApi.getRuleItemList(row.id)
  } finally {
    itemLoading.value = false
  }
}

onMounted(getList)
</script>

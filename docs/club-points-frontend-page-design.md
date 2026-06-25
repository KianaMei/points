# 俱乐部员工积分系统前端页面设计

## 1. 前端基线

### 1.1 工程位置

| 项 | 设计 |
| --- | --- |
| 前端工程 | `ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3` |
| 开发端口 | `8889` |
| 页面根目录 | `src/views/clubpoints` |
| API 根目录 | `src/api/clubpoints` |
| 路由来源 | 后端菜单 seed 下发，前端不写静态业务路由 |
| UI 组件基线 | 复用现有 `ContentWrap`、`Dialog`、`Pagination`、`dict-tag`、`UploadFile`、`UserSelectForm`、`DeptSelectForm`、Element Plus |
| 请求封装 | 复用 `@/config/axios`，按现有 `src/api/system/*` 风格导出函数 |

新增目录：

```text
src/api/clubpoints/
  app/
  leader/
  admin/
  shared/
src/views/clubpoints/
  app/
  leader/
  admin/
  components/
```

### 1.2 前端职责边界

| 能做 | 不能做 |
| --- | --- |
| 展示菜单、列表、表单、状态、待办和操作结果 | 不能把按钮隐藏当权限控制 |
| 生成 `requestNo` 防重复点击 | 不能只靠前端防重复提交 |
| 用 `v-hasPermi` 隐藏无权限按钮 | 不能跳过后端 `@PreAuthorize` 和数据范围校验 |
| 对必填、格式、时间顺序做即时校验 | 不能替代后端规则版本、积分区间、库存、冻结和幂等校验 |
| 展示后端返回的快照字段 | 不能重新计算历史积分事实 |
| 发起 Excel 下载 | 不能给员工和负责人提供导出入口 |

## 2. 菜单和路由

### 2.1 一级菜单

一级菜单固定为 `俱乐部积分`。三类角色可共用同一前端工程，但菜单由后端权限过滤：

| 分组 | 可见角色 | 说明 |
| --- | --- | --- |
| 我的积分 | 员工、负责人、管理员本人 | 本人工作台、积分、俱乐部、活动、兑换、异议、通知。 |
| 负责人工作台 | 负责人 | 负责俱乐部的活动、成员、签到修正、非签到材料。 |
| 积分管理 | 管理员 | 全局俱乐部、活动、规则、账本、兑换、年度、预算、报表、审计、任务。 |

### 2.2 路由命名

| 页面类型 | 路由前缀 | 组件命名 |
| --- | --- | --- |
| 员工端 | `/clubpoints/app/*` | `ClubPointsApp*` |
| 负责人端 | `/clubpoints/leader/*` | `ClubPointsLeader*` |
| 管理员端 | `/clubpoints/admin/*` | `ClubPointsAdmin*` |
| 共用组件 | 无独立路由 | `ClubPoints*` |

`defineOptions({ name })` 必须稳定，避免 `keep-alive` 和菜单缓存错乱。

## 3. 共用前端组件

| 组件 | 路径 | 用途 |
| --- | --- | --- |
| 积分数值展示 | `components/PointAmount.vue` | 正负积分颜色、冻结积分提示、年度清零提示。 |
| 状态标签 | `components/StatusTag.vue` | 俱乐部、活动、报名、材料、兑换、任务状态展示。 |
| 附件输入 | `components/AttachmentInput.vue` | 文件上传和外部链接输入，输出 `AttachmentInputVO[]`。 |
| 强确认弹窗 | `components/StrongConfirmDialog.vue` | 仅管理员物理删除俱乐部使用。 |
| 审核弹窗 | `components/ReviewDialog.vue` | 活动、材料、兑换审核，输出 `ReviewReqVO`。 |
| 积分规则选择 | `components/RuleItemSelect.vue` | 选择当前已发布规则版本和规则项，展示分值区间。 |
| 员工选择 | `components/UserPicker.vue` | 复用 system 用户数据，给材料明细、代录、调整使用。 |
| 俱乐部选择 | `components/ClubSelect.vue` | 根据角色展示负责俱乐部或全局俱乐部。 |
| 请求号生成 | `shared/requestNo.ts` | 兑换申请、管理员代录、积分调整等提交前生成 `requestNo`。 |

## 4. 员工端页面

### 4.1 员工工作台

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/dashboard` |
| 组件 | `views/clubpoints/app/dashboard/index.vue` |
| API | `GET /clubpoints/app/dashboard/summary` |
| 权限 | 登录 |

页面模块：

| 模块 | 字段 |
| --- | --- |
| 积分卡片 | `availablePoints`、`frozenPoints`、`totalEarnedPoints` |
| 俱乐部卡片 | `joinedClubCount` |
| 活动卡片 | `registeredActivityCount` |
| 兑换卡片 | `pendingRedemptionCount` |
| 通知入口 | `unreadNotifyCount` |

交互：

| 操作 | 跳转 |
| --- | --- |
| 查看积分 | `/clubpoints/app/ledger` |
| 查看已报名活动 | `/clubpoints/app/activity` |
| 查看兑换 | `/clubpoints/app/redemption` |
| 查看通知 | `/clubpoints/app/notify` |

### 4.2 我的积分

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/ledger` |
| 组件 | `views/clubpoints/app/ledger/index.vue` |
| API | `GET /clubpoints/app/ledger/summary`、`GET /clubpoints/app/ledger/page` |
| 权限 | 登录 |

页面结构：

| 区域 | 字段 |
| --- | --- |
| 概览 | 当前可用积分、冻结积分、累计增加、累计扣减、年度已清零、最近流水时间 |
| 流水筛选 | 方向、积分类型、来源类型、来源俱乐部、业务发生时间段 |
| 流水表 | 流水 ID、方向、积分、积分类型、来源类型、发放俱乐部、规则版本、证明材料、原因、业务发生时间、登记时间、是否撤销 |

页面文案必须展示：`来源统计不代表当前可用积分余额构成；当前可用积分以账户概览为准。`

### 4.3 我的俱乐部

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/club` |
| 组件 | `views/clubpoints/app/club/index.vue` |
| API | `GET /clubpoints/app/club/my-list`、`GET /clubpoints/app/club/joinable-page`、`POST /clubpoints/app/club/join`、`POST /clubpoints/app/club/exit`、`GET /clubpoints/app/club/member-page` |
| 权限 | 加入 `clubpoints:club-member:join`；退出 `clubpoints:club-member:exit`；成员 `clubpoints:club-member:query` |

页面结构：

| 区域 | 字段或动作 |
| --- | --- |
| 我的俱乐部 | 俱乐部名称、状态、成员数、负责人、加入时间、查看成员、退出 |
| 可加入俱乐部 | 名称、介绍、成员数、负责人、加入 |
| 成员名单 | 员工、部门、联系方式、当前可用积分、加入时间、是否负责人 |

退出俱乐部弹窗必须提示：退出后该俱乐部未开始或可取消的报名会自动取消，不产生缺席扣分。

### 4.4 活动报名和签到

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/activity` |
| 组件 | `views/clubpoints/app/activity/index.vue` |
| API | `GET /clubpoints/app/activity/page`、`GET /clubpoints/app/activity/get`、`GET /clubpoints/app/activity/my-history-page`、`POST /clubpoints/app/registration/create`、`POST /clubpoints/app/registration/cancel`、`GET /clubpoints/app/registration/my-page`、`POST /clubpoints/app/attendance/check-in`、`POST /clubpoints/app/attendance/check-out` |
| 权限 | 报名 `clubpoints:registration:create`；取消 `clubpoints:registration:cancel`；签到 `clubpoints:attendance:check-in`；签退 `clubpoints:attendance:check-out` |

页面结构：

| 区域 | 字段或动作 |
| --- | --- |
| 活动列表 | 俱乐部、活动标题、地点、开始结束时间、报名截止、取消截止、基础积分、全程额外积分、状态、报名状态 |
| 活动详情 | 活动说明、签到窗口、签退窗口、积分配置、附件、当前员工报名和签到签退状态 |
| 我的报名 | 报名时间、取消时间、签到状态、签退状态、结算状态 |
| 操作 | 报名、取消报名、签到、签退 |

签到签退提交必须带 `registrationId` 和 `clientTime`。页面可以展示本地时间，但必须提示：最终窗口判断以后端服务器北京时间为准。

### 4.5 兑换

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/redemption` |
| 组件 | `views/clubpoints/app/redemption/index.vue` |
| API | `GET /clubpoints/app/redemption/batch-page`、`GET /clubpoints/app/redemption/gift-page`、`POST /clubpoints/app/redemption/apply`、`POST /clubpoints/app/redemption/cancel`、`GET /clubpoints/app/redemption/my-page` |
| 权限 | 申请 `clubpoints:redemption:apply`；取消 `clubpoints:redemption:cancel-own` |

页面结构：

| 区域 | 字段或动作 |
| --- | --- |
| 批次列表 | 批次名称、开放时间、关闭时间、资格规则、当前是否可申请 |
| 礼品列表 | 礼品名称、图片、消耗积分、库存状态、说明、申请 |
| 我的兑换 | 批次、礼品、消耗积分、冻结积分、申请状态、申请时间、审核时间、审核意见、取消 |

提交兑换前端必须生成 `requestNo`。库存不足、资格不足、积分不足都按后端错误码展示，前端不能自行绕过申请按钮限制。

### 4.6 我的异议

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/dispute` |
| 组件 | `views/clubpoints/app/dispute/index.vue` |
| API | `POST /clubpoints/app/dispute/create`、`GET /clubpoints/app/dispute/my-page`、`GET /clubpoints/app/dispute/get` |
| 权限 | 登录 |

页面结构：

| 区域 | 字段 |
| --- | --- |
| 异议列表 | 目标类型、目标 ID、内容摘要、状态、回复、关联流水、提交时间、处理时间 |
| 提交弹窗 | 目标类型、目标 ID、异议内容、附件或链接 |

异议不直接改积分。页面必须展示管理员回复和关联调整/撤销流水。

### 4.7 我的通知

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/app/notify` |
| 组件 | `views/clubpoints/app/notify/index.vue` |
| API | `GET /clubpoints/app/notify/my-page`、`PUT /clubpoints/app/notify/update-read` |
| 权限 | 登录 |

通知只展示已读/未读，不提供删除。

## 5. 负责人端页面

### 5.1 负责人工作台

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/leader/dashboard` |
| 组件 | `views/clubpoints/leader/dashboard/index.vue` |
| API | `GET /clubpoints/leader/dashboard/summary` |
| 权限 | `clubpoints:leader` |

展示负责俱乐部数量、草稿活动、被驳回活动、签到异常、待提交材料、待办数。待办点击进入对应页面，并自动带筛选条件。

### 5.2 负责俱乐部和成员

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/leader/club` |
| 组件 | `views/clubpoints/leader/club/index.vue` |
| API | `GET /clubpoints/leader/club/my-managed-list`、`GET /clubpoints/leader/club/get`、`PUT /clubpoints/leader/club/update`、`GET /clubpoints/leader/member/page` |
| 权限 | `clubpoints:club-leader`、修改 `clubpoints:club:update`、成员 `clubpoints:club-member:query` |

负责人只能修改自己负责俱乐部的名称、介绍、联系方式、封面，不能创建、停用、删除俱乐部，不能设置负责人，不能移除成员。

### 5.3 活动管理

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/leader/activity` |
| 组件 | `views/clubpoints/leader/activity/index.vue` |
| API | `GET /clubpoints/leader/activity/page`、`GET /clubpoints/leader/activity/get`、`POST /clubpoints/leader/activity/create`、`PUT /clubpoints/leader/activity/update`、`POST /clubpoints/leader/activity/submit`、`POST /clubpoints/leader/activity/withdraw`、`POST /clubpoints/leader/activity/cancel`、`DELETE /clubpoints/leader/activity/delete` |
| 权限 | 查询 `clubpoints:activity:query`；创建 `clubpoints:activity:create`；修改 `clubpoints:activity:update`；提交/撤回 `clubpoints:activity:submit`；取消 `clubpoints:activity:cancel`；删除 `clubpoints:activity:delete` |

表单字段：

| 字段 | 要求 |
| --- | --- |
| 所属俱乐部 | 只能选择负责俱乐部 |
| 标题、说明、地点 | 标题和说明必填 |
| 活动时间 | 开始时间必须早于结束时间 |
| 报名截止、取消截止 | 不能晚于活动开始时间 |
| 签到签退窗口 | 按偏移分钟输入，前端只校验数字和基础时间顺序 |
| 基础积分、全程额外积分 | 来自规则版本允许范围，最终以后端校验为准 |
| 规则版本 | 必选当前可用规则版本 |
| 附件 | 可选 |

取消活动、物理删除活动只走普通二次确认，不走强确认；后端仍写审计和快照。

### 5.4 报名、签到和特殊缺席

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/leader/attendance` |
| 组件 | `views/clubpoints/leader/attendance/index.vue` |
| API | `GET /clubpoints/leader/registration/page`、`GET /clubpoints/leader/attendance/page`、`POST /clubpoints/leader/attendance/correct`、`POST /clubpoints/leader/registration/mark-special-absence` |
| 权限 | 报名查询 `clubpoints:registration:query`；签到修正 `clubpoints:attendance:correct`；特殊缺席 `clubpoints:registration:special-absence` |

页面结构：

| 区域 | 字段或动作 |
| --- | --- |
| 报名名单 | 活动、员工、部门、联系方式、报名状态、取消原因、特殊缺席标记 |
| 签到签退 | 签到状态、签退状态、来源、自助时间、修正记录 |
| 修正弹窗 | 报名 ID、目标类型、修正后时间、修正原因 |
| 特殊缺席弹窗 | 报名 ID、原因 |

结算后修正不能改原流水，只能提示管理员通过补发、撤销或调整流水处理。

### 5.5 非签到积分材料

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/leader/contribution` |
| 组件 | `views/clubpoints/leader/contribution/index.vue` |
| API | `GET /clubpoints/leader/contribution/page`、`POST /clubpoints/leader/contribution/create`、`PUT /clubpoints/leader/contribution/update`、`POST /clubpoints/leader/contribution/submit`、`POST /clubpoints/leader/contribution/withdraw`、`DELETE /clubpoints/leader/contribution/delete` |
| 权限 | 查询 `clubpoints:contribution:query`；提交 `clubpoints:contribution:submit`；撤回 `clubpoints:contribution:withdraw`；删除 `clubpoints:contribution:delete` |

材料表单：

| 字段 | 要求 |
| --- | --- |
| 俱乐部 | 只能选择负责俱乐部 |
| 类型 | 宣传、策划、特殊贡献等字典 |
| 标题、说明 | 必填 |
| 规则版本 | 必填 |
| 积分明细 | 至少一条，包含员工、积分、原因、材料摘要 |
| 附件或链接 | 按制度需要上传 |

审核通过后材料和附件锁定，负责人不能再改，只能查看。

## 6. 管理员端页面

### 6.1 管理员工作台

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/admin/dashboard` |
| 组件 | `views/clubpoints/admin/dashboard/index.vue` |
| API | 复用活动审核、材料审核、兑换审核、异议、任务分页接口聚合 |
| 权限 | 管理员菜单权限 |

展示待审核活动、待审核材料、待审核兑换、待处理异议、异常任务、异常撤销记录。

### 6.2 俱乐部和负责人

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/admin/club` |
| 组件 | `views/clubpoints/admin/club/index.vue` |
| API | `GET /clubpoints/club/page`、`GET /clubpoints/club/get`、`POST /clubpoints/club/create`、`PUT /clubpoints/club/update`、`POST /clubpoints/club/disable`、`DELETE /clubpoints/club/delete`、`POST /clubpoints/club-leader/assign`、`POST /clubpoints/club-member/add`、`POST /clubpoints/club-member/remove` |
| 权限 | `clubpoints:club:query`、`clubpoints:club:create`、`clubpoints:club:update`、`clubpoints:club:disable`、`clubpoints:club:delete`、`clubpoints:club-leader:update`、`clubpoints:club-member:add`、`clubpoints:club-member:remove` |

强确认只出现在物理删除俱乐部：

| 项 | 规则 |
| --- | --- |
| 弹窗标题 | `强确认删除俱乐部` |
| 输入文本 | `确认删除俱乐部：{俱乐部名称}` |
| 请求字段 | `strongConfirm.confirmText`、`strongConfirm.confirmedAt` |
| 禁用提交 | 输入文本不匹配时禁用 |
| 后端接口 | `DELETE /clubpoints/club/delete` |

停用俱乐部、移除成员、设置负责人都要填写原因，但不走强确认。

### 6.3 活动审核和全局活动

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/admin/activity` |
| 组件 | `views/clubpoints/admin/activity/index.vue` |
| API | `GET /clubpoints/activity/page`、`POST /clubpoints/activity/create`、`PUT /clubpoints/activity/update`、`POST /clubpoints/activity/publish`、`POST /clubpoints/activity/review`、`POST /clubpoints/activity/cancel`、`DELETE /clubpoints/activity/delete` |
| 权限 | `clubpoints:activity:query`、`clubpoints:activity:create`、`clubpoints:activity:update`、`clubpoints:activity:publish`、`clubpoints:activity:review`、`clubpoints:activity:cancel`、`clubpoints:activity:delete` |

管理员可以直接发布活动，也可以审核负责人提交的活动。审核弹窗只能通过或驳回，驳回必须填写原因。

### 6.4 结算和账本

| 页面 | 路由 | API | 权限 |
| --- | --- | --- | --- |
| 活动结算 | `/clubpoints/admin/settlement` | `POST /clubpoints/settlement/run`、`GET /clubpoints/settlement/page` | `clubpoints:settlement:run`、`clubpoints:settlement:query` |
| 积分账户 | `/clubpoints/admin/ledger/account` | `GET /clubpoints/ledger/account-page` | `clubpoints:ledger:query` |
| 积分流水 | `/clubpoints/admin/ledger/transaction` | `GET /clubpoints/ledger/transaction-page`、`POST /clubpoints/ledger/adjust`、`POST /clubpoints/ledger/reverse` | `clubpoints:ledger:query`、`clubpoints:ledger:adjust`、`clubpoints:ledger:reverse` |

积分调整表单必须包含 `requestNo`、员工、调整类型、方向、积分、发放俱乐部、规则版本、原因、附件。撤销流水必须选择原流水并填写撤销原因。

### 6.5 规则版本

| 项 | 设计 |
| --- | --- |
| 路由 | `/clubpoints/admin/rule` |
| 组件 | `views/clubpoints/admin/rule/index.vue` |
| API | `GET /clubpoints/rule/page`、`POST /clubpoints/rule/create`、`PUT /clubpoints/rule/update`、`POST /clubpoints/rule/publish`、`POST /clubpoints/rule/withdraw`、`POST /clubpoints/rule/disable` |
| 权限 | `clubpoints:rule:manage` |

页面必须展示规则版本、规则项、状态、生效时间和附件。发布、撤回、停用都要二次确认并填写原因。

### 6.6 非签到积分审核和代录

| 页面 | 路由 | API | 权限 |
| --- | --- | --- | --- |
| 材料审核 | `/clubpoints/admin/contribution-review` | `GET /clubpoints/contribution/review-page`、`GET /clubpoints/contribution/get`、`POST /clubpoints/contribution/review`、`DELETE /clubpoints/contribution/delete` | `clubpoints:contribution:review`、`clubpoints:contribution:delete` |
| 管理员代录 | `/clubpoints/admin/contribution-direct` | `POST /clubpoints/contribution/direct-create` | `clubpoints:contribution:direct-create` |

审核通过后不能改材料内容。管理员代录直接生效，必须填写 `requestNo`、员工、积分、规则版本、原因、附件。

### 6.7 兑换管理

| 页面 | 路由 | API | 权限 |
| --- | --- | --- | --- |
| 兑换批次 | `/clubpoints/admin/redemption-batch` | `GET /clubpoints/redemption-batch/page`、`POST /clubpoints/redemption-batch/create`、`PUT /clubpoints/redemption-batch/update`、`POST /clubpoints/redemption-batch/open`、`POST /clubpoints/redemption-batch/close` | `clubpoints:redemption-batch:manage` |
| 礼品维护 | `/clubpoints/admin/redemption-gift` | `GET /clubpoints/redemption-gift/page`、`POST /clubpoints/redemption-gift/create`、`PUT /clubpoints/redemption-gift/update`、`POST /clubpoints/redemption-gift/update-status` | `clubpoints:redemption-gift:manage` |
| 兑换审核 | `/clubpoints/admin/redemption-application` | `GET /clubpoints/redemption-application/page`、`POST /clubpoints/redemption-application/review` | `clubpoints:redemption:review` |

审核兑换只能通过或拒绝，不能修改申请里的礼品、数量、积分消耗。通过后展示直接发放时间；拒绝后展示冻结释放。

### 6.8 异议、年度、预算

| 页面 | 路由 | API | 权限 |
| --- | --- | --- | --- |
| 异议处理 | `/clubpoints/admin/dispute` | `GET /clubpoints/dispute/page`、`POST /clubpoints/dispute/handle` | `clubpoints:dispute:handle` |
| 年度清零 | `/clubpoints/admin/annual-clearing` | `POST /clubpoints/annual/clear` | `clubpoints:annual:clear` |
| 年度排名和激励 | `/clubpoints/admin/annual-ranking` | `GET /clubpoints/annual/ranking-page`、`POST /clubpoints/annual/incentive-suggest` | `clubpoints:annual:query`、`clubpoints:annual:manage` |
| 预算记录 | `/clubpoints/admin/budget` | `GET /clubpoints/budget/page`、`POST /clubpoints/budget/create`、`PUT /clubpoints/budget/update` | `clubpoints:budget:manage` |

年度清零页面必须提示：只清未冻结可用积分；冻结中的兑换申请后续审核拒绝时释放回账户，不追加过期清零。

### 6.9 报表、审计、任务

| 页面 | 路由 | API | 权限 |
| --- | --- | --- | --- |
| 报表中心 | `/clubpoints/admin/report` | `GET /clubpoints/report/export-excel` | `clubpoints:report:export` |
| 审计日志 | `/clubpoints/admin/audit` | `GET /clubpoints/audit/page` | `clubpoints:audit:query` |
| 任务运行 | `/clubpoints/admin/job-run` | `GET /clubpoints/job-run/page`、`POST /clubpoints/job-run/handle` | `clubpoints:job:query`、`clubpoints:job:handle` |

报表中心第一版支持：

| 报表 | 页面查询 | Excel 导出 |
| --- | --- | --- |
| 积分明细表 | 是 | 是 |
| 积分兑换记录 | 是 | 是 |
| 积分总台账 | 是 | 是 |
| 俱乐部发放积分排名 | 是 | 建议支持 |
| 预算和经费使用统计 | 是 | 建议支持 |

员工和负责人菜单不能出现导出按钮。导出动作必须写强审计，记录导出人、时间、类型和筛选条件。

## 7. API 模块拆分

| 前端 API 文件 | 后端路径前缀 | 内容 |
| --- | --- | --- |
| `api/clubpoints/app/dashboard.ts` | `/clubpoints/app/dashboard` | 员工工作台 |
| `api/clubpoints/app/ledger.ts` | `/clubpoints/app/ledger` | 我的积分概览和流水 |
| `api/clubpoints/app/club.ts` | `/clubpoints/app/club` | 我的俱乐部、加入、退出、成员 |
| `api/clubpoints/app/activity.ts` | `/clubpoints/app/activity`、`/clubpoints/app/registration`、`/clubpoints/app/attendance` | 活动、报名、签到签退 |
| `api/clubpoints/app/redemption.ts` | `/clubpoints/app/redemption` | 员工兑换 |
| `api/clubpoints/app/dispute.ts` | `/clubpoints/app/dispute` | 我的异议 |
| `api/clubpoints/app/notify.ts` | `/clubpoints/app/notify` | 我的通知 |
| `api/clubpoints/leader/club.ts` | `/clubpoints/leader/club`、`/clubpoints/leader/member` | 负责人俱乐部和成员 |
| `api/clubpoints/leader/activity.ts` | `/clubpoints/leader/activity` | 负责人活动 |
| `api/clubpoints/leader/attendance.ts` | `/clubpoints/leader/registration`、`/clubpoints/leader/attendance` | 报名和签到修正 |
| `api/clubpoints/leader/contribution.ts` | `/clubpoints/leader/contribution` | 非签到材料 |
| `api/clubpoints/admin/club.ts` | `/clubpoints/club`、`/clubpoints/club-leader`、`/clubpoints/club-member` | 管理员俱乐部 |
| `api/clubpoints/admin/activity.ts` | `/clubpoints/activity` | 管理员活动 |
| `api/clubpoints/admin/settlement.ts` | `/clubpoints/settlement` | 活动结算 |
| `api/clubpoints/admin/ledger.ts` | `/clubpoints/ledger` | 账本和调整 |
| `api/clubpoints/admin/rule.ts` | `/clubpoints/rule` | 规则版本 |
| `api/clubpoints/admin/contribution.ts` | `/clubpoints/contribution` | 材料审核和代录 |
| `api/clubpoints/admin/redemption.ts` | `/clubpoints/redemption-batch`、`/clubpoints/redemption-gift`、`/clubpoints/redemption-application` | 兑换管理 |
| `api/clubpoints/admin/operation.ts` | `/clubpoints/dispute`、`/clubpoints/annual`、`/clubpoints/budget`、`/clubpoints/report`、`/clubpoints/audit`、`/clubpoints/job-run` | 运营、报表、审计、任务 |

## 8. 权限和按钮

按钮必须按接口权限配置 `v-hasPermi`。隐藏按钮只改善体验，不代表安全。

| 动作 | 前端按钮 | 权限码 |
| --- | --- | --- |
| 加入俱乐部 | 加入 | `clubpoints:club-member:join` |
| 退出俱乐部 | 退出 | `clubpoints:club-member:exit` |
| 报名活动 | 报名 | `clubpoints:registration:create` |
| 取消报名 | 取消报名 | `clubpoints:registration:cancel` |
| 签到 | 签到 | `clubpoints:attendance:check-in` |
| 签退 | 签退 | `clubpoints:attendance:check-out` |
| 提交兑换 | 立即兑换 | `clubpoints:redemption:apply` |
| 取消兑换 | 取消申请 | `clubpoints:redemption:cancel-own` |
| 修改负责俱乐部 | 保存 | `clubpoints:club:update` |
| 创建活动 | 新建活动 | `clubpoints:activity:create` |
| 修改活动 | 编辑 | `clubpoints:activity:update` |
| 提交活动审核 | 提交审核 | `clubpoints:activity:submit` |
| 取消活动 | 取消活动 | `clubpoints:activity:cancel` |
| 删除活动 | 删除 | `clubpoints:activity:delete` |
| 修正签到签退 | 修正 | `clubpoints:attendance:correct` |
| 标记特殊缺席 | 特殊缺席 | `clubpoints:registration:special-absence` |
| 提交材料 | 提交材料 | `clubpoints:contribution:submit` |
| 审核材料 | 审核 | `clubpoints:contribution:review` |
| 管理员代录 | 代录积分 | `clubpoints:contribution:direct-create` |
| 积分调整 | 调整积分 | `clubpoints:ledger:adjust` |
| 撤销流水 | 撤销 | `clubpoints:ledger:reverse` |
| 兑换审核 | 审核 | `clubpoints:redemption:review` |
| 报表导出 | 导出 | `clubpoints:report:export` |

## 9. 表单和交互规则

### 9.1 时间

| 场景 | 前端规则 |
| --- | --- |
| 业务时间展示 | 统一展示北京时间语义，不让用户选择时区 |
| 活动开始结束 | 开始时间必须早于结束时间 |
| 报名截止、取消截止 | 不晚于活动开始时间 |
| 签到签退窗口 | 前端校验偏移分钟为整数，最终窗口以后端计算为准 |
| 流水和报表筛选 | 使用 `startTime`、`endTime`，按 `occurred_at` 语义提示 |

### 9.2 附件

| 场景 | 前端规则 |
| --- | --- |
| 文件上传 | 先走 infra 文件上传，拿到 `fileId` 后写入 `AttachmentInputVO` |
| 外部链接 | `type=2` 时必须填写 `url` 和 `name` |
| 审核后附件 | 已锁定附件只读展示，管理员追加用单独入口 |
| 材料、异议、预算、规则 | 均复用 `AttachmentInput.vue` |

### 9.3 请求号和防重复提交

| 场景 | 生成规则 |
| --- | --- |
| 提交兑换 | `REDEMPTION_APPLY` 前端请求号，后端拼业务幂等键 |
| 管理员代录 | `DIRECT_CONTRIBUTION` 前端请求号，后端落唯一幂等键 |
| 积分调整 | `LEDGER_ADJUST` 前端请求号，后端落唯一幂等键 |

提交按钮在请求中必须 loading 禁用。请求失败后允许用户重试，但不能重新生成同一次业务的 `requestNo`；关闭弹窗重新发起新业务时再生成新号。

## 10. 开发顺序

| 顺序 | 内容 | 原因 |
| --- | --- | --- |
| 1 | 建 `api/clubpoints/shared`、共用状态字典、附件输入、强确认弹窗、审核弹窗 | 后续所有页面复用 |
| 2 | 管理员规则版本、俱乐部、账本账户/流水基础页面 | 给活动、材料、兑换提供基础数据 |
| 3 | 员工工作台、我的积分、我的俱乐部 | 最小员工闭环 |
| 4 | 负责人俱乐部、活动、报名签到修正、材料 | 活动和非签到积分入口 |
| 5 | 管理员活动审核、结算、材料审核、代录 | 形成发分闭环 |
| 6 | 兑换批次、礼品、员工兑换、管理员审核 | 形成消耗积分闭环 |
| 7 | 异议、年度、预算、报表、审计、任务 | 运营收口 |

## 11. 前端验收

| 场景 | 验收点 |
| --- | --- |
| 权限 | 员工看不到导出、审计、全局兑换；负责人看不到其他俱乐部数据；管理员能看到全局菜单 |
| 强确认 | 只有管理员物理删除俱乐部出现强确认，其他删除只有普通确认 |
| 积分文案 | 来源统计页面明确不代表当前余额构成 |
| 活动签到 | 签到签退按钮按报名和窗口状态展示，提交仍以后端返回为准 |
| 兑换 | 资格不足、库存不足、积分不足能按后端错误提示；重复点击不能生成重复申请 |
| 附件锁定 | 材料审核通过后附件只读，管理员可追加 |
| 报表 | 只有管理员有导出按钮，导出带筛选条件并触发审计 |
| 任务 | 失败任务能查看原因、重试或人工处理，人工处理必须填原因 |

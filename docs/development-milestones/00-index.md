# 开发里程碑执行索引

## Superpowers 入口

本目录按 Superpowers 执行方式维护：

- 共享执行规则：`01-superpowers-execution-rules.md`
- 每个 M 文件都是一个 Implementation Plan 入口。
- 执行具体 M 文件前，必须先读共享执行规则。
- 实际写代码时必须使用 `test-driven-development` 的 RED/GREEN/REFACTOR。
- 执行计划时优先使用 `subagent-driven-development`；不能用子任务时使用 `executing-plans`。
- Superpowers 默认的 commit 步骤在本项目改为 Checkpoint，因为项目禁止未授权提交。

## 用法

每次准备开发时先读本索引，再只读当前里程碑文件。不要一次性读完 M0-M12。

执行规则：

- 一个里程碑没有通过验收，不进入下一个里程碑。
- 一个任务没有测试或验证证据，不算完成。
- 数据库约束、权限、审计、幂等属于硬门禁，不能用前端逻辑或口头约定替代。
- 不跑 full build，除非明确要求。
- 不提交 git，除非明确要求。
- 不添加 co-author 或 AI 元数据。

## 状态标记

| 标记 | 含义 |
| --- | --- |
| `[ ]` | 未开始 |
| `[~]` | 进行中或部分完成 |
| `[x]` | 已完成并有验证证据 |
| `[!]` | 阻塞，必须处理后继续 |

## 里程碑文件

| 状态 | 里程碑 | 文件 | 放行目标 |
| --- | --- | --- | --- |
| `[x]` | 规则 | `01-superpowers-execution-rules.md` | 所有里程碑执行前必读 |
| `[x]` | M0 | `M0-engineering-foundation.md` | `clubpoints` 空模块被 `yudao-server` 扫描，底座能轻量验证 |
| `[x]` | M1 | `M1-database-and-seed.md` | 正式 schema、seed、测试 DDL 一致 |
| `[x]` | M2 | `M2-permission-crosscutting.md` | 权限、范围、审计、附件、通知横切能力可复用 |
| `[x]` | M3 | `M3-rule-config.md` | 规则版本和规则项可发布、停用、读取 |
| `[x]` | M4 | `M4-ledger.md` | 积分流水、冻结、余额缓存形成账本脊柱 |
| `[x]` | M5 | `M5-club-member-leader.md` | 俱乐部、成员、负责人闭环可用 |
| `[x]` | M6 | `M6-activity-registration-attendance.md` | 活动、报名、签到签退、特殊缺席闭环可用 |
| `[x]` | M7 | `M7-activity-settlement.md` | 活动积分和缺席扣分可幂等结算 |
| `[x]` | M8 | `M8-contribution-violation.md` | 非签到积分、违规扣分、弄虚作假闭环可用 |
| `[x]` | M9 | `M9-redemption.md` | 兑换资格、冻结、库存、审核闭环可用 |
| `[x]` | M10 | `M10-annual-dispute-budget.md` | 异议、年度清零、排名、激励、预算闭环可用 |
| `[x]` | M11 | `M11-report-job-frontend.md` | 报表、任务监控、通知、前端页面收口 |
| `[x]` | M12 | `M12-hardening-acceptance.md` | 并发、权限、回归、演示验收完成 |
| `[x]` | M13 | `M13-usability-contract-hardening.md` | 可用性、前后端契约、用户语言与契约硬化 |

## 依赖链

```text
M0 空模块
-> M1 表和 seed
-> M2 横切能力
-> M3 规则配置
-> M4 账本
-> M5 俱乐部成员
-> M6 活动报名签到
-> M7 活动结算
-> M8 非签到和扣分
-> M9 兑换
-> M10 年度运营
-> M11 前端和报表收口
-> M12 硬化验收
-> M13 可用性和契约硬化
```

## 每个任务的最低完成标准

一个任务必须同时满足：

- 代码或 SQL 落到指定文件。
- 单元测试、集成测试、接口验证或命令验证至少一种通过。
- 影响文档同步更新。
- 失败路径有明确处理。
- 没有绕过数据库唯一键、后端权限、强审计、事务边界。

## 当前最近入口

当前最近入口：M13 已放行。M0-M13 全部里程碑均已完成并有验证证据。

- M0 已创建 `ruoyi-vue-pro-github/yudao-module-clubpoints` 空模块。
- M0 已在根 POM 和 `yudao-server/pom.xml` 接入 `yudao-module-clubpoints`。
- M0 已通过 `mvn -pl yudao-server -am -DskipTests "-Dflatten.skip=true" compile` 轻量编译验证。
- M1 已创建 `sql/mysql/club-points-schema.sql` 和 `sql/mysql/club-points-seed.sql`。
- M1 已完成正式 schema 的 34 张 `club_points_*` 表、默认 seed、测试 DDL、clean SQL。
- M1 已通过临时 MySQL 库导入、关键唯一键、权限集合、seed 幂等、H2 测试 DDL、正式/测试字段与唯一键比对验证。
- M2.1 已完成权限码、三端菜单、按钮权限和三类角色建议授权，并通过临时库导入、权限集合、角色边界和幂等验证。
- M2.2 已完成 `DictTypeConstants`、seed 字典补齐、常量/seed 一致性测试、MySQL 导入和幂等验证。
- M2.3 已完成 `ClubScopeService`、成员/负责人 DO 与 Mapper、本人/已加入俱乐部/负责俱乐部/全局范围校验，并通过数据范围单测和 M2 组合验证。
- M2.4 已完成 `ClubAuditService`、审计日志 DO 与 Mapper、审计创建 BO、强审计动作常量和审计失败回滚业务测试，并通过 M2 组合验证。
- M2.5 已完成 `ClubAttachmentService`、附件绑定 DO 与 Mapper、infra 文件复用、附件锁定、删除前校验和软删除，并通过 M2 组合验证。
- M2.6 已完成 `ClubNotifyService`、通知模板常量、活动审核/积分变动/兑换审核/异议回复通知封装、通知失败不回滚业务测试，并通过 M2 组合验证。
- M2.7 已完成员工越权、负责人越权、管理员全局访问、强审计失败回滚、通知失败不回滚、附件锁定不可删除横切测试，并通过 M2 放行组合验证。
- M3.1 已完成规则版本、规则项、发布记录 DO 与 Mapper，并通过 RED/GREEN 映射测试和规则包质量门禁。
- M3.2 已完成规则版本状态枚举、规则项值类型枚举、规则项编码枚举和 5 个规则错误码，并通过 seed 编码一致性、固定分值区间表达和 M3 当前组合测试验证。
- M3.3 已完成规则版本 Service、规则项 Service、发布/停用状态机、当前已发布版本读取、按编码读取规则项、发布和停用强审计，并通过 M3.3 RED/GREEN、替代记录回归 RED/GREEN 和 M3 当前组合测试验证。
- M3.4 已完成管理员规则版本分页、详情、创建、更新、复制、发布、撤回、停用、规则项列表和规则项保存接口，并通过 M3.4 RED/GREEN、值类型校验补强 RED/GREEN、M3 当前组合测试和质量门禁。
- M3.5 已完成 `ClubPointRuleResolveService`、规则快照 BO、按发生时间读取已发布版本、按版本和编码读取规则项、固定分值读取、区间校验和规则快照构建，并通过 M3.5 RED/GREEN 单测验证。
- M3.6 已完成规则状态机、业务读取、分值边界和无发布版本失败测试收口，并通过 M3 组合测试验证。
- M3 已放行；下一步入口是 M4 积分账本脊柱。
- M4.1 已完成积分流水、账户缓存、冻结、年度状态四张表 DO 与 Mapper，并通过 RED/GREEN 映射测试验证。
- M4.2 已完成流水方向、流水状态、流水来源类型、冻结状态、积分分类、年度清零状态枚举和 6 个账本错误码，并通过 RED/GREEN 枚举测试和 M4 当前组合验证。
- M4.3 已完成 `ClubPointLedgerService` 正/负流水追加、幂等处理、余额不足拒绝、规则快照、来源快照和账户缓存同事务更新，并通过 RED/GREEN 服务测试和 M4 当前组合验证。
- M4.4 已完成 `ClubPointFreezeService` 冻结积分、释放冻结、冻结转扣减、冻结来源枚举、冻结错误码和账户缓存同事务更新，并通过 RED/GREEN 服务测试和 M4 当前组合验证。
- M4.5 已完成 `ClubPointLedgerService` 撤销反向流水、管理员调整、撤销幂等、调整附件快照、强审计失败回滚和账户缓存同事务更新，并通过 RED/GREEN 服务测试、M4 当前组合验证和质量门禁。
- M4.6 已完成按用户和全量账户缓存重算、冻结中积分重算、重算任务运行记录、流水事实源不变验证，并通过 RED/GREEN 服务测试、M4 当前组合验证和质量门禁。
- M4.7 已完成员工本人账本、负责人负责俱乐部发放来源账本、管理员全局账本查询 API；负责人范围按 `issuing_club_id` 限定为自己负责俱乐部发放来源，并通过 RED/GREEN 查询接口测试、M4 收口组合验证和质量门禁。
- M4.8 已完成账本核心测试收口，覆盖正向发分、负向扣分、重复幂等键、并发重复幂等、余额不足、冻结/释放/转扣减、撤销、调整强审计失败回滚和余额重算。
- M4 已放行；下一步入口是 M5 俱乐部、成员、负责人闭环。
- M5.1 已完成 `club_points_club` 主表 DO/Mapper，复用 M2 已落地的成员/负责人 DO/Mapper，并通过 RED/GREEN Mapper 测试和 M5 当前组合验证。
- M5.2 已完成俱乐部状态、成员状态、负责人状态枚举，补齐俱乐部/成员/负责人业务错误码，并通过 RED/GREEN 枚举测试和 M5 当前组合验证。
- M5.3 已完成 `ClubPointClubService` 创建、修改、停用、启用、物理删除、强确认、强审计和引用阻断，并通过 RED/GREEN 服务测试。
- M5.4 已完成 `ClubPointMemberService` 员工加入、管理员添加、员工退出、管理员移除、成员状态审计、自动取消有效报名和范围收缩，并通过 RED/GREEN 服务测试。
- M5.5 已完成 `ClubPointLeaderService` 管理员设置负责人、移除负责人、有效用户校验、强审计和负责人范围立即生效，并通过 RED/GREEN 服务测试。
- M5.6 已完成员工本人俱乐部、可加入俱乐部、成员分页、负责人负责俱乐部、管理员俱乐部/成员/负责人查询 API，并通过 RED/GREEN 查询接口测试。
- M5.7 已完成测试收口，覆盖创建俱乐部、停用后拒绝新增成员业务、物理删除强确认、成员加入退出、重复加入失败、负责人任免、负责人越权失败和历史快照仍可读，并通过 M5 收口组合验证。
- M5 已放行；下一步入口是 M6 活动、报名、签到签退、特殊缺席闭环。
- M6.1 已完成活动、活动审核记录、活动积分配置版本、活动报名、签到签退有效事实、签到签退补录修正 6 张表 DO/Mapper，并通过 RED/GREEN Mapper 测试和 M6 当前组合验证。
- M6.2 已完成活动状态枚举、状态跳转 guard、报名/签到/关键字段修改状态 guard 和状态错误码，并通过 RED/GREEN 枚举测试和 M6 当前组合验证。
- M6.3 已完成活动 Service，覆盖负责人创建草稿、提交审核、管理员审核通过/驳回、未发布修改、已发布关键字段修改强审计、取消强审计、活动附件绑定和锁定，并通过 RED/GREEN 服务测试和 M6 当前组合验证。
- M6.4 已完成报名 Service，覆盖员工报名、员工取消报名、负责人负责俱乐部报名分页、管理员报名分页、成员范围、报名截止和重复报名唯一键兜底，并通过 RED/GREEN 服务测试和 M6 当前组合验证。
- M6.5 已完成签到签退 Service，覆盖员工自助签到、签退、本人有效报名范围、活动状态、签到/签退窗口、重复签到签退唯一键兜底、签退必须先签到和不生成积分流水，并通过 RED/GREEN 服务测试和 M6 当前组合验证。
- M6.6 已完成管理员补录签到签退、管理员修正签到签退、管理员标记特殊缺席、强审计失败回滚和特殊缺席报名事实保存，并通过 RED/GREEN 服务测试和 M6 当前组合验证。
- M6.7 已完成员工活动查询、员工报名取消、员工签到签退、负责人活动管理、管理员活动审核、管理员签到补录修正和特殊缺席 API，并通过 RED/GREEN Controller 测试验证。
- M6.8 已完成活动状态跳转、负责人越权创建失败、报名重复失败、报名时间窗口、签到时间窗口、补录强审计、特殊缺席保存和不生成积分流水测试收口，并通过 M6 收口组合验证。
- M6 已放行；下一步入口是 M7 活动结算、无故缺席和月度累计缺席扣分。
- M7.1 已完成活动结算状态、结算运行状态、结算触发来源、活动基础/全程额外/单次缺席/月度缺席结算项模型和结算状态 seed 字典，并通过 RED/GREEN 枚举测试和 M7 当前组合验证。
- M7.2 已完成活动 SettlementService、结算运行 DO/Mapper、活动报名/签到签退/特殊缺席读取、活动积分和单次无故缺席扣分流水创建、结算运行记录写入和活动状态更新，并通过 RED/GREEN 服务测试、M7 当前组合验证和质量门禁。
- M7.3 已完成结算 `runKey` 前置幂等、已结算活动重跑、运行记录唯一键冲突兜底、流水稳定幂等复用和并发重跑测试，并通过 RED/GREEN 服务测试、M7 当前组合验证和质量门禁。
- M7.4 已完成月度累计缺席统计、阈值读取、达到阈值后扣分、同用户同年月只扣一次和统计快照写入，并通过 RED/GREEN 服务测试、M7 当前组合验证和质量门禁。
- M7.5 已完成活动结算 Job Handler、任务运行记录、失败可重试留痕、重试幂等不重复发分和结算运行关联 `job_run_id`，并通过 RED/GREEN Job 测试、M7 当前组合验证和质量门禁。
- M7.6 已完成管理员待结算活动分页、手动触发结算、结算运行记录分页、结算明细查询、手动结算强审计和手动重跑不重复流水，并通过 RED/GREEN 接口测试、M7 当前组合验证和质量门禁。
- M7.7 已完成测试收口，覆盖正常签到发分、缺席扣分、特殊缺席、重复结算、并发结算、月度累计缺席、余额不足扣分失败和 Job 失败记录，并通过 M7 收口组合验证和质量门禁。
- M7 已放行；下一步入口是 M8 非签到积分、违规扣分、弄虚作假闭环。
- M8.1 已完成非签到材料、材料明细、材料审核记录 DO 与 Mapper，并通过 RED/GREEN Mapper 测试验证；下一步入口是 M8.2 状态机和错误码。
- M8.2 已完成材料状态枚举、状态跳转 guard、审核/撤回/编辑 guard 和 6 个 contribution 错误码，并通过 RED/GREEN 枚举测试和 M8 当前组合验证。
- M8.3 已完成负责人提交材料 Service，覆盖创建草稿、多明细、附件绑定、提交审核、负责人范围、规则项存在、分值区间、附件缺失和提交后不可编辑，并通过 RED/GREEN 服务测试和 M8 当前组合验证。
- M8.4 已完成管理员审核材料 Service，覆盖待审核列表全局范围、审核通过发流水、审核驳回、附件锁定、强审计、审核记录、重复审核拦截和审计失败回滚，并通过 RED/GREEN 服务测试和 M8 当前组合验证。
- M8.5 已完成管理员代录 Service，覆盖全局范围校验、规则区间、原因和附件必填、requestNo 幂等、附件绑定锁定、强审计、账本发分和审计失败回滚，并通过 RED/GREEN 服务测试和 M8 当前组合验证。
- M8.6 已完成管理员违规扣分 Service，覆盖全局范围校验、违规规则区间、原因和附件必填、requestNo 幂等、附件绑定锁定、强审计、LedgerService 负向流水、余额不足回滚和审计失败回滚，并通过 RED/GREEN 服务测试和 M8 当前组合验证。
- M8.7 已完成管理员弄虚作假处理 Service，覆盖全局范围校验、原材料已发正向流水撤销、当前可用积分清零扣分、附件锁定、强审计、年度评优资格取消、requestNo 幂等、通知失败不回滚和审计失败回滚，并通过 RED/GREEN 服务测试和 M8 当前组合验证。
- M8.8 已完成负责人非签到材料列表、详情、创建、修改、提交、撤回 API，以及管理员待审核列表、详情、审核、代录、违规扣分、弄虚作假处理 API；权限码、API 文档和 seed 已同步，通过 RED/GREEN Controller 测试和 M8 当前组合验证。
- M8.9 已完成测试收口，覆盖负责人提交材料、负责人越权、分值越界、审核通过发分、重复审核不重复发分、管理员代录幂等、违规扣分、弄虚作假撤销加扣分，并通过 M8 收口组合验证。
- M8 已放行；下一步入口是 M9 兑换资格、冻结、库存、审核闭环。
- M9.1 已完成兑换批次、礼品、资格快照、兑换申请、库存锁、审核记录 DO 与 Mapper，并通过 RED/GREEN 映射测试验证；下一步入口是 M9.2 批次 Service。
- M9.2 已完成兑换批次 Service，覆盖创建、修改未开启批次、开启生成资格快照、关闭批次、资格规则修改强审计、开启后禁止修改和关闭后申请校验，并通过 RED/GREEN 服务测试和 M9 当前组合验证；下一步入口是 M9.3 礼品 Service。
- M9.3 已完成兑换礼品 Service，覆盖新增、修改、上架、下架、积分价格、库存总量校验、数据库条件锁库存、释放已锁库存、已锁转已兑和并发不超兑，并通过 RED/GREEN 服务测试、M9 当前组合验证和质量门禁；下一步入口是 M9.4 资格快照。
- M9.4 已完成兑换资格快照 Service，覆盖管理员按批次查看和按资格结果筛选、员工申请前读取本人快照、未生成快照失败、不合格失败、申请资格不读取实时账户，并通过 RED/GREEN 服务测试、M9 当前组合验证和质量门禁；下一步入口是 M9.5 申请 Service。
- M9.5 已完成兑换申请 Service，覆盖员工可兑换礼品列表、提交申请、批次开放校验、资格快照校验、礼品上架校验、冻结积分、数据库条件锁库存、创建申请、库存锁记录、申请幂等、库存不足回滚和可用积分不足回滚，并通过 RED/GREEN 服务测试、M9 当前组合验证和质量门禁。
- M9.6 已完成兑换审核 Service，覆盖管理员待审核列表、审核通过冻结转扣减、兑换负向流水、库存锁转已兑、审核拒绝释放冻结和库存、强审计、通知、重复审核幂等、审计失败回滚和通知失败不回滚，并通过 RED/GREEN 服务测试、M9 当前组合验证和质量门禁。
- M9.7 已完成兑换取消和超时处理 Service，覆盖员工审核前取消、本人范围、重复取消幂等、已审核禁止取消、取消释放冻结和库存、取消不生成扣减流水、待审核超时自动取消、超时只处理过期待审核申请，并通过 RED/GREEN 服务测试、M9 当前组合验证和质量门禁；下一步入口是 M9.8 API。
- M9.8 已完成兑换 API，覆盖管理员批次、礼品、资格快照、兑换申请分页和审核，员工开放批次、可兑换礼品、提交申请、我的兑换和取消本人申请；路径和权限已同步 API 文档，负责人没有兑换审核入口，并通过 RED/GREEN Controller 测试、M9 当前组合验证和质量门禁；下一步入口是 M9.9 测试收口。
- M9.9 已完成测试收口，覆盖批次状态机、资格快照生成、库存不足、积分不足、并发申请不超兑、重复申请幂等、审核通过扣分、审核拒绝释放、取消释放和跨年冻结释放口径，并通过 M9 收口组合验证。
- M9 已放行；下一步入口是 M10 异议、年度清零、排名、激励、预算闭环。
- M10.1 已完成异议 DO、Mapper、状态机枚举、提交/受理/驳回/处理 Service、附件绑定、强审计、通知和账本调整/撤销集成，并通过 RED/GREEN、M10.1 组合验证和质量门禁；下一步入口是 M10.2 年度清零模型。
- M10.2 已完成年度清零记录 DO、Mapper、状态/来源复用、幂等键、北京时间触发口径和跨年冻结释放口径固定，并通过 RED/GREEN、M10.2 组合验证和质量门禁；下一步入口是 M10.3 年度清零 Service。
- M10.3 已完成年度清零 Service，覆盖单用户清零、全量扫描、同年同用户幂等、冻结积分不清零、无可用分跳过、账本负向流水和账户缓存更新，并通过 RED/GREEN、M10.3 组合验证和质量门禁；下一步入口是 M10.4 年度清零 Job。
- M10.4 已完成年度清零 Job Handler、Job Run 记录、人工/重试强审计、失败用户定位和指定失败用户重跑，并通过 RED/GREEN、M10.4 组合验证和质量门禁；下一步入口是 M10.5 年度排名。
- M10.5 已完成俱乐部年度排名 DO、Mapper、生成 Service、年度流水读取和重生成能力；排名按年度正向发放流水扣除对应撤销流水，兑换扣分、年度清零和普通扣分不影响俱乐部发放量，并通过 RED/GREEN、M10.5 组合验证和质量门禁。
- M10.6 已完成激励记录 DO、Mapper、激励建议生成、管理员确认、管理员取消和确认 / 取消强审计；激励建议不自动登记预算、不生成积分流水、不更新账户，并通过 RED/GREEN、M10.6 组合验证和质量门禁；下一步入口是 M10.7 预算记录。
- M10.7 已完成预算记录 DO、Mapper、预算新增、修改、停用、查询、激励来源回链和预算创建 / 修改 / 停用强审计；预算年度按 `occur_date` 统计，俱乐部来源通过激励 `source_type/source_id` 追溯，停用使用逻辑删除，并通过 RED/GREEN、M10.7 组合验证和质量门禁；下一步入口是 M10.8 API。
- M10.8 已完成员工异议、管理员异议处理、年度清零和清零记录、排名生成和查询、激励建议 / 确认 / 取消、预算分页 / 创建 / 修改 / 停用 API；路径和 `club-points-api-design.md` 已同步，并通过 RED/GREEN Controller 测试、M10.8 组合验证和质量门禁；下一步入口是 M10.9 测试收口。
- M10.9 已完成年度运营测试收口，覆盖异议提交处理、异议调整走账本、年度清零、重复清零幂等、冻结积分不清零、跨年冻结释放、年度排名不受兑换影响、激励确认和预算修改强审计，并通过 M10 收口组合验证。
- M10 已放行；下一步入口是 M11 报表、任务监控、通知、前端页面收口。
- M11.1 已完成前端基建和共用组件，创建 `api/clubpoints/{app,leader,admin,shared}`、`views/clubpoints/{app,leader,admin,components}`、请求号工具、共享类型、积分数值、状态标签、附件输入、强确认、审核、规则项选择、员工选择和俱乐部选择组件；已通过请求号行为验证、字典 seed 覆盖验证、`8889` 入口验证和质量门禁。下一步入口是 M11.2 前端 API 模块。
- M11.2 已完成前端 API 模块，创建 app 7 个、leader 4 个、admin 8 个按角色和领域分组的请求封装；所有业务 API 文件复用 `@/config/axios`，兑换申请、管理员代录和积分调整暴露请求号复用 helper，并通过新增范围类型检查过滤、页面散写 URL 扫描和质量门禁。下一步入口是 M11.3 报表查询。
- M11.3 已完成管理员报表查询接口，支持积分明细、总台账、兑换记录、俱乐部排名和预算统计五类分页查询；查询权限为 `clubpoints:report:query`，导出权限保留 `clubpoints:report:export` 给 M11.4；已通过 RED/GREEN Controller / Service 测试和权限 seed 同步。下一步入口是 M11.4 报表导出。
- M11.4 已完成管理员报表导出接口，支持五类报表 Excel 导出、`clubpoints:report:export` 权限、`REPORT_EXPORT` 强审计、审计失败阻断导出和前端下载封装；已通过 RED/GREEN Controller / Service 测试。下一步入口是 M11.5 任务监控。
- M11.5 已完成管理员任务运行列表、详情和失败任务人工重试接口；`clubpoints:job:query` 查询、`clubpoints:job:handle` 处理，重试写 `JOB_RUN_RETRY` 强审计，并按原 `runKey + retryCount+1` 派发活动结算或年度清零 Job 保持幂等；已通过 RED/GREEN Controller / Service 测试。下一步入口是 M11.6 通知和待办。
- M11.6 已完成员工通知分页和标记已读、员工 / 负责人 / 管理员工作台待办汇总；员工通知复用 system 站内信，负责人只统计有效负责俱乐部内待办，管理员工作台权限为 `clubpoints:dashboard:query`；已通过 RED/GREEN Controller 测试、通知失败不回滚测试、前端类型过滤验证和质量门禁。下一步入口是 M11.7 管理员基础数据页。
- M11.7 已完成管理员规则版本、俱乐部和负责人、积分账户、积分流水、活动结算 5 个基础页面；强确认只用于物理删除俱乐部，积分调整带请求号，撤销从原流水发起，结算重跑幂等继续由后端兜底；已通过 RED/GREEN 文件验证、前端类型过滤验证和页面权限 / URL 扫描。下一步入口是 M11.8 员工前端页面。
- M11.8 已完成员工工作台、我的积分、我的俱乐部、活动报名签到、兑换、我的异议、我的通知 7 个页面；页面只调用本人接口，兑换申请失败重试保留同一 `requestNo`，通知不提供删除；已通过 RED/GREEN 文件验证、前端类型过滤验证和页面权限 / URL 扫描。下一步入口是 M11.9 负责人前端页面。
- M11.9 已完成负责人工作台、负责俱乐部和成员、活动管理、报名签到和特殊缺席、非签到材料 5 个页面；页面只调用负责人范围 API，不提供兑换审核、报表导出、规则配置、负责人任免或移除成员入口；已通过 RED/GREEN 文件验证、前端类型过滤验证和页面权限 / URL 扫描。下一步入口是 M11.10 管理员活动和材料审核页。
- M11.10 已完成管理员活动审核和全局活动、材料审核、管理员代录 3 个页面；活动审核和材料审核复用审核弹窗，代录带 `requestNo` 且失败重试保留同号；已通过 RED/GREEN 文件验证、前端类型过滤验证、活动 / 材料 Controller 接口测试和页面权限 / URL 扫描。下一步入口是 M11.11 管理员兑换管理页。
- M11.11 已完成管理员兑换批次、礼品维护、兑换审核 3 个页面；批次和礼品状态可维护，兑换审核页只读展示申请礼品、数量和积分消耗，只提交通过或拒绝；已通过 RED/GREEN 文件验证、前端类型过滤验证、兑换 Controller 接口测试和页面权限 / URL 扫描。下一步入口是 M11.12 管理员运营页面。
- M11.12 已完成管理员异议处理、年度清零、年度排名和激励、预算记录、管理员工作台 5 个页面；年度清零提示明确只清未冻结可用积分，工作台待办按后端聚合跳转对应列表；已通过 RED/GREEN 文件验证、前端类型过滤验证、年度运营 Controller 接口测试和页面权限 / URL 扫描。下一步入口是 M11.13 管理员报表、审计、任务页。
- M11.13 已完成管理员报表中心、强审计日志和任务运行 3 个页面，并补齐后端审计分页接口；报表导出只在管理员页出现并带筛选条件调用后端强审计导出，任务失败处理必须填写原因；已通过 RED/GREEN 文件验证、前端类型过滤验证、报表 / 任务 / 审计接口测试和页面权限 / URL 扫描。下一步入口是 M11.14 前端验证。
- M11.14 已完成前端验证，覆盖 `pnpm install --frozen-lockfile`、8889 路由入口、三类角色工作台、管理员报表 / 审计 / 任务页、后端 seed 菜单、按钮权限隐藏、接口失败错误提示和类型过滤；旧 live 后端进程未加载新接口时返回 404，页面保持默认值或空表并不再抛 mounted hook 未处理异常；后端对应 Controller / Service 组合测试 20 个通过。
- M11 已放行；下一步入口是 M12 硬化验收。
- M12.1 已完成数据库约束复查，新增 `ClubPointSchemaHardeningTest` 复核正式 schema 与测试 DDL 的 `club_points_*` 表、主键、字段、`deleted` 字段和唯一键。
- M12.1 已通过 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointSchemaHardeningTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`；`ClubPointSchemaHardeningTest` 运行 2 个测试，失败 0，错误 0。
- M12.2 已完成权限矩阵复查，新增 `ClubPointPermissionMatrixHardeningTest` 验证员工本人、员工他人隔离、负责人负责俱乐部、负责人其他俱乐部拒绝、负责人禁止兑换审核和报表导出、管理员全局账本可见。
- M12.2 已通过 `mvn -pl yudao-module-clubpoints -am -Dtest=ClubPointPermissionMatrixHardeningTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`；`ClubPointPermissionMatrixHardeningTest` 运行 2 个测试，失败 0，错误 0。
- M12.3 已完成幂等和并发测试复查，组合验证覆盖活动重复/并发结算、月度缺席、非签到重复审核、管理员代录幂等、兑换重复提交、兑换并发不超兑、兑换重复审核、年度清零重复执行和账本唯一键冲突处理。
- M12.3 已通过 `mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointActivitySettlementServiceImplTest,ClubPointContributionServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionGiftServiceImplTest,ClubPointRedemptionReviewServiceImplTest,ClubPointAnnualClearingServiceImplTest,ClubPointLedgerServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`；7 个服务测试类合计运行 54 个测试，失败 0，错误 0。
- M12.4 已完成事务边界复查，组合验证覆盖强审计失败回滚、通知失败不回滚、流水与账户缓存同事务、兑换申请冻结 / 库存 / 申请事务回滚、兑换审核冻结 / 库存 / 流水一致性。
- M12.4 已通过 `mvn -pl yudao-module-clubpoints -am "-Dtest=ClubAuditServiceImplTest,ClubNotifyServiceImplTest,ClubPointLedgerServiceImplTest,ClubPointLedgerAdjustmentServiceImplTest,ClubPointFreezeServiceImplTest,ClubPointContributionServiceImplTest,ClubPointRedemptionApplicationServiceImplTest,ClubPointRedemptionReviewServiceImplTest,ClubPointRedemptionBatchServiceImplTest,ClubPointAnnualClearingServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`；10 个服务测试类合计运行 70 个测试，失败 0，错误 0。
- M12.5 已完成年度和跨年测试，新增 `ClubPointAnnualCrossYearHardeningTest`，覆盖北京时间 1 月 1 日清零、只清可用分、冻结不清、跨年兑换拒绝释放回账户、不追加过期清零流水、排名不受兑换扣分和年度清零影响、历史流水仍可查。
- M12.5 已通过 `mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointAnnualCrossYearHardeningTest,ClubPointAnnualClearingModelTest,ClubPointAnnualClearingServiceImplTest,ClubPointRedemptionReviewServiceImplTest,ClubPointRedemptionCancelServiceImplTest,ClubPointAnnualRankingServiceImplTest,ClubPointAnnualOperationControllerTest,ClubPointRedemptionControllerTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`；8 个测试类合计运行 34 个测试，失败 0，错误 0。
- M12.6 已完成前端回归，修复 `/admin-api` 前缀覆盖、员工报名分页、管理员活动管理接口、负责人页面默认负责俱乐部、前端 query/body 契约和无后端实现的删除 / 撤回死入口。
- M12.6 已通过活动 Controller 测试、前端 API 前缀 hardening 测试、7 类接口组合测试、`yudao-server` 轻量编译、live API 回归、8889 前端入口、Playwright 10 页面回归和管理员报表导出验证；`vue-tsc` 全量仍有非 clubpoints 存量债，clubpoints 路径过滤无命中。
- M12.7 已完成 MVP 演示脚本，新增 `ClubPointMvpDemoHardeningTest` 通过 API 串起规则发布、俱乐部和负责人、活动审核、报名签到签退、`force=true` 手动结算、员工查账、兑换、年度排名、年度清零、审计和积分明细报表。
- M12.7 已补齐管理员俱乐部写接口供演示数据可重复准备，并固定 `force=true` 管理员手动结算语义：已发布活动可先收口到已结束，再走原 Job、结算 Service、账本和幂等链路。
- M12.7 已通过 `mvn -pl yudao-module-clubpoints -am "-Dtest=ClubPointMvpDemoHardeningTest,ClubPointClubQueryControllerTest,ClubPointActivityControllerTest,ClubPointSettlementAdminControllerTest,ClubPointRedemptionControllerTest,ClubPointAnnualOperationControllerTest,ClubPointReportControllerTest,ClubPointReportServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test`；8 个测试类合计运行 36 个测试，失败 0，错误 0。
- M12.7 已通过 `mvn -pl yudao-server -am -DskipTests "-Dflatten.skip=true" compile`；clubpoints 前端路径类型过滤无输出。
- M12.8 已完成文档收口，同步 API、数据库、前端页面、权限规格、PRD、架构、流程和开发计划；当前 MVP 明确仅开放俱乐部物理删除，活动物理删除、材料物理删除和负责人活动撤回不进入 MVP。
- M12.8 已移除前端活动 / 材料死删除 API wrapper，并把活动基础积分和全程额外分改为页面显式输入，不再由前端 API 默认硬编码制度分值。
- M12.8 已通过文档过期表述扫描、死接口扫描、硬编码分值扫描、数据库设计 / 正式 SQL 34 张表一致性验证、clubpoints 前端路径类型过滤和 M12 关键回归组合测试。
- M12 已放行；M0-M12 全部里程碑均已完成并有验证证据。
- M13 已完成 API wrapper 到后端 mapping、页面不得散写请求、用户语言白名单、活动积分发放命名、菜单路由、权限一致性和静默失败契约硬化。
- M13 已重放 `club-points-seed.sql` 到 live `club_points` 数据库，运行态菜单已从旧口径 `员工工作台 / 活动报名` 校准为 `员工积分中心 / 活动报名签到`。
- M13 已定位并处理 live 后端 `ClubPointClubAppController.joinClub(AppClubOperationReqVO)` 运行态反射异常；根因是旧后端进程加载 stale classpath，源码和当前 `target/classes` 均正确，轻量编译后重启 PID 61960 生效。
- M13 当前回归以 API wrapper、页面散写、权限端点、用户语言、静默失败和菜单路由门禁为准。
- M13 前端类型过滤已通过：`pnpm --dir ruoyi-vue-pro-github\yudao-ui\yudao-ui-admin-vue3 exec vue-tsc --noEmit --pretty false` 全量仍因非 clubpoints 存量债退出 1，但过滤 `src/api/clubpoints`、`src/views/clubpoints` 和 `clubpoints` 命中 0。

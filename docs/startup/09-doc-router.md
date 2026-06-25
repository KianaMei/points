# 文档路由

## 每次启动必读

- `AGENTS.md`
- `docs/startup/00-readme.md`
- `docs/startup/09-doc-router.md`

## 小文档按任务读

- 项目范围：`docs/startup/01-project-scope.md`
- 源码和环境：`docs/startup/02-source-structure.md`
- 后端实现：`docs/startup/03-backend-rules.md`
- 前端实现：`docs/startup/04-frontend-rules.md`
- 数据流：`docs/startup/05-data-flow.md`
- 数据库：`docs/startup/06-database-rules.md`
- 权限审计：`docs/startup/07-permission-audit.md`
- 实现顺序：`docs/startup/08-implementation-order.md`

## 长文档什么时候读

- 产品需求、第一版边界：`docs/club-points-prd.md`
- 功能权限矩阵：`docs/club-points-functions-and-permissions.md`
- 业务流程、状态机、异常流：`docs/club-points-flow-design.md`
- 架构边界、事务、能力依赖：`docs/club-points-architecture-design.md`
- API 路径、VO 字段、错误码：`docs/club-points-api-design.md`
- 表、字段、索引、唯一键：`docs/club-points-database-design.md`
- 页面、按钮、接口映射：`docs/club-points-frontend-page-design.md`
- 里程碑和落地步骤：`docs/club-points-development-plan.md`
- 细粒度执行清单：`docs/development-milestones/00-index.md`
- Superpowers 执行规则：`docs/development-milestones/01-superpowers-execution-rules.md`

## 读取策略

- 不要默认读所有长文档。
- 先读当前任务相关的 1-2 个小文档。
- 小文档不足以判断时，再读对应长文档。
- 发生文档冲突时，以更具体的设计文档为准。
- 数据库冲突以 `club-points-database-design.md` 为准。
- 前端页面冲突以 `club-points-frontend-page-design.md` 为准。
- 权限冲突以 `club-points-functions-and-permissions.md` 为准。
- 开发顺序冲突以 `club-points-development-plan.md` 为准。
- 具体执行步骤冲突以 `docs/development-milestones/` 下对应里程碑文件为准，但不得违反长设计文档的事实源约束。

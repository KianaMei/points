# 启动规则

## 每次先读

1. `AGENTS.md`
2. `docs/startup/00-readme.md`
3. `docs/startup/09-doc-router.md`

## 再按任务读

- 需求范围：读 `01-project-scope.md`
- 目录和环境：读 `02-source-structure.md`
- 后端代码：读 `03-backend-rules.md`
- 前端代码：读 `04-frontend-rules.md`
- 积分、兑换、结算、年度：读 `05-data-flow.md`
- SQL、DO、Mapper：读 `06-database-rules.md`
- 权限、按钮、审计、强确认：读 `07-permission-audit.md`
- 排期、阶段、先后顺序：读 `08-implementation-order.md`

## 不要这样做

- 不要默认一次性读取所有长设计文档。
- 不要只凭聊天记忆写代码。
- 不要把 PRD、API、数据库、前端页面设计互相覆盖。
- 不要恢复已删除的芋道业务模块。
- 不要引入新框架、新平台、新迁移工具。
- 不要提交 git，除非用户明确要求。
- 不要跑 full build，除非用户明确要求。
- 不要添加 co-author 或 AI 元数据。

## 当前项目一句话

基于裁剪后的芋道 `ruoyi-vue-pro`，开发俱乐部员工积分系统。

MVP 闭环：

```text
活动发分 -> 员工查账 -> 兑换扣分 -> 俱乐部排名 -> 年度清零 -> 审计追溯
```

## 当前默认节奏

先模块和 SQL，再权限、规则、账本，然后才是业务页面和完整闭环。

如果一个任务会改变事实源、权限边界、数据库表或流程状态，先查对应长文档。

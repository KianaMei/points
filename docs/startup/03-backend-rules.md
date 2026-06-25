# 后端规则

## 基线

- 基于芋道 `Spring Boot 2.7.x` / Java 8 口径。
- 不升级到 Spring Boot 3。
- 不引入新大框架。
- 不恢复已删除的 ai/bpm/crm/erp/im/iot/mall/member/pay 等模块。

## 模块

- 业务代码只进 `yudao-module-clubpoints`。
- `yudao-server` 只负责启动和依赖业务模块。
- 组织、用户、角色、菜单、站内信复用 `yudao-module-system`。
- 文件、定时任务、代码生成、Excel 能力复用 `yudao-module-infra`。

## DO 和 Mapper

- 普通业务 DO 继承 `BaseDO`。
- 不继承 `TenantBaseDO`。
- 不添加 `tenant_id`。
- Mapper 继承 `BaseMapperX<T>`。
- 查询优先用 `LambdaQueryWrapperX<T>`。

## Controller

- 员工入口：`/clubpoints/app/**`
- 负责人入口：`/clubpoints/leader/**`
- 管理员入口：`/clubpoints/**`
- 写接口和敏感读接口必须有 `@PreAuthorize`。
- 当前登录人从安全框架取，不让前端传当前用户 ID。

## Service

- 事务放应用服务层。
- 关键业务写和强审计在同一事务。
- 审计失败必须回滚业务。
- 通知失败不回滚业务。
- 积分、冻结、库存、年度清零必须有数据库兜底约束。

## 错误码

- `clubpoints` 使用独立错误码段。
- 错误码统一放模块内 `enums/ErrorCodeConstants.java`。

## 禁止

- 不能直接改积分余额。
- 不能用 Redis 当事实源。
- 不能只靠前端隐藏按钮做权限。
- 不能把业务表加进租户忽略列表糊弄租户问题。

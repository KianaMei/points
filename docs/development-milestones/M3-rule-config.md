# M3 规则版本和配置后台 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现规则版本、规则项、发布、停用和业务读取接口，确保所有分值、阈值、区间来自已发布规则。

**Architecture:** 规则版本控制生效周期，规则项控制可执行参数。业务服务只读取已发布版本并保存规则快照，不允许把制度分值硬编码进 Java。

**Tech Stack:** Spring Boot Service、MyBatis Mapper、RuoYi VO/Controller、强审计、JUnit。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 规则默认值来自 seed，不写死进业务代码。
- 发布和停用必须写强审计。
- 已发布版本不可直接修改规则项。
- Java 行为必须 TDD。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/dataobject/rule/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/dal/mysql/rule/`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/rule/ClubPointRuleService.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/controller/admin/rule/`
- Modify: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Test: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/java/cn/iocoder/yudao/module/clubpoints/service/rule/`

**Interfaces:**

- Consumes: M1 规则表、M2 强审计、默认规则 seed。
- Produces: `getCurrentRuleVersion()`、`getRuleItemByCode(String code)`、`validatePointInRange(String code, Integer points)`、`buildRuleSnapshot(...)`。

**Verification:**

- Run: 规则版本状态机单测。
- Expected: 草稿、发布、停用、已发布不可改全部通过。
- Run: 分值区间校验单测。
- Expected: 越界失败，固定分值规则通过。

## 目标

实现规则版本、规则项、发布、停用、业务读取接口。后续所有分值、阈值、上限都从已发布规则读取。

## 前置条件

- M2 已放行。
- 默认规则 seed 已存在。
- 强审计可用。
- 权限码和菜单已存在。

## 任务 M3.1 DO 和 Mapper

- [ ] 创建 `dal/dataobject/rule/ClubPointRuleVersionDO.java`。
- [ ] 创建 `dal/dataobject/rule/ClubPointRuleItemDO.java`。
- [ ] 创建 `dal/dataobject/rule/ClubPointRulePublishRecordDO.java`。
- [ ] 创建 `dal/mysql/rule/ClubPointRuleVersionMapper.java`。
- [ ] 创建 `dal/mysql/rule/ClubPointRuleItemMapper.java`。
- [ ] 创建 `dal/mysql/rule/ClubPointRulePublishRecordMapper.java`。
- [ ] DO 继承 `BaseDO`。
- [ ] Mapper 继承 `BaseMapperX<T>`。

验收：

- [ ] DO 字段和 M1 DDL 一致。
- [ ] 不出现 `TenantBaseDO`。

## 任务 M3.2 枚举和错误码

- [ ] 创建规则版本状态枚举。
- [ ] 创建规则项值类型枚举。
- [ ] 创建规则项编码枚举。
- [ ] 补充错误码：版本不存在、版本状态错误、规则项不存在、规则项编码重复、规则值越界。
- [ ] 固定分值也用区间表达。

验收：

- [ ] 不在 service 写死制度分值。
- [ ] 规则项编码和 seed 一致。

## 任务 M3.3 Service

- [ ] 创建 `service/rule/ClubPointRuleService.java`。
- [ ] 创建 `service/rule/ClubPointRuleServiceImpl.java`。
- [ ] 实现创建草稿版本。
- [ ] 实现复制已有版本。
- [ ] 实现新增规则项。
- [ ] 实现修改草稿规则项。
- [ ] 实现发布版本。
- [ ] 实现停用版本。
- [ ] 实现读取当前已发布版本。
- [ ] 实现按编码读取规则项。
- [ ] 发布和停用必须写强审计。

验收：

- [ ] 同一时间只有一个当前有效版本。
- [ ] 已发布版本不可直接改规则项。
- [ ] 业务读取只读已发布版本。

## 任务 M3.4 Admin API

- [ ] 创建规则版本分页接口。
- [ ] 创建规则版本详情接口。
- [ ] 创建规则版本草稿接口。
- [ ] 创建规则版本复制接口。
- [ ] 创建规则版本发布接口。
- [ ] 创建规则版本停用接口。
- [ ] 创建规则项列表接口。
- [ ] 创建规则项保存接口。
- [ ] Controller 使用 `@PreAuthorize`。
- [ ] 写 VO 校验规则值类型、区间、默认值。

验收：

- [ ] API 路径和 `club-points-api-design.md` 一致。
- [ ] 管理员权限才能发布和停用。
- [ ] 发布接口不接受前端传当前用户 ID。

## 任务 M3.5 业务读取封装

- [ ] 提供 `getCurrentRuleVersion()`。
- [ ] 提供 `getRuleItemByCode(String code)`。
- [ ] 提供 `validatePointInRange(String code, Integer points)`。
- [ ] 提供 `buildRuleSnapshot(...)`。
- [ ] 对不存在规则项抛业务错误。

验收：

- [ ] M4-M10 可以统一读取规则。
- [ ] 业务记录能保存规则快照 JSON。

## 任务 M3.6 测试

- [ ] 测试草稿创建。
- [ ] 测试发布成功。
- [ ] 测试已发布版本不可修改。
- [ ] 测试停用成功。
- [ ] 测试分值越界失败。
- [ ] 测试固定分值规则。
- [ ] 测试无已发布版本时业务读取失败。

验收：

- [ ] 规则状态机测试通过。
- [ ] 规则值边界测试通过。

## M3 放行标准

- [ ] 规则版本可发布。
- [ ] 规则项可配置。
- [ ] 已发布规则可被业务读取。
- [ ] 发布和停用写强审计。
- [ ] 没有制度分值硬编码。

## M3 不通过时禁止

- [ ] 禁止写账本发分逻辑。
- [ ] 禁止写活动结算。
- [ ] 禁止写非签到积分和兑换资格规则。

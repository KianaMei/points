# M0 工程地基 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建并接入 `yudao-module-clubpoints` 空模块，证明裁剪后的芋道底座仍可轻量编译、启动和扫描业务模块。

**Architecture:** `clubpoints` 采用和 `system/infra` 一致的单 Maven 模块形态。`yudao-server` 只增加模块依赖和扫描，不放业务逻辑；M0 只做工程接入，不做业务 Controller、Service、Mapper。

**Tech Stack:** Java 8 口径、Spring Boot 2.7.x、Maven、RuoYi-Vue-Pro、MySQL Docker、Redis Docker、Vue3/Vite 端口 `8889`。

## Global Constraints

- 先读 `docs/development-milestones/01-superpowers-execution-rules.md`。
- 不恢复已删除的芋道业务模块。
- 不引入新框架。
- 不跑 full build，除非用户明确要求。
- 不提交 git，Superpowers 的 commit 步骤在本项目改为 Checkpoint。
- 不添加 co-author 或 AI 元数据。
- M0 的 POM、模块扫描和端口验证必须有命令证据。

---

## Superpowers 文件与接口索引

**Files:**

- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/pom.xml`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/ClubPointsModule.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/resources/sql/create_tables.sql`
- Create: `ruoyi-vue-pro-github/yudao-module-clubpoints/src/test/resources/sql/clean.sql`
- Modify: `ruoyi-vue-pro-github/pom.xml`
- Modify: `ruoyi-vue-pro-github/yudao-server/pom.xml`

**Interfaces:**

- Consumes: `yudao-module-system`、`yudao-module-infra`、`yudao-server` Spring Boot 扫描。
- Produces: `cn.iocoder.yudao.module.clubpoints.ClubPointsModule` 最小 Bean、`ErrorCodeConstants` 错误码入口、测试 SQL 文件占位。

**Verification:**

- Run: `mvn -pl yudao-server -am -DskipTests -Dflatten.skip=true compile`
- Expected: 编译通过，`yudao-module-clubpoints` 被 Maven reactor 识别。
- Run: `Invoke-WebRequest http://127.0.0.1:48080/admin-api/system/auth/get-permission-info`
- Expected: HTTP `200`，业务码 `401`。
- Run: `Invoke-WebRequest http://127.0.0.1:8889`
- Expected: HTTP `200`。

## 目标

把裁剪后的芋道底座接入 `clubpoints` 空模块，证明后端能扫描模块，前端和基础后端仍可响应。

## 前置条件

- Docker MySQL 端口 `3306` 可用。
- Docker Redis 端口 `6379` 可用。
- 后端端口 `48080` 可用。
- 前端端口 `8889` 可用。
- 不恢复已删除的芋道业务模块。

## 任务 M0.1 底座事实复核

- [ ] 检查根 POM 只包含 `yudao-dependencies`、`yudao-framework`、`yudao-server`、`yudao-module-system`、`yudao-module-infra`。
- [ ] 检查 `yudao-server/pom.xml` 只依赖 `system` 和 `infra`，尚未依赖 `clubpoints`。
- [ ] 检查 `sql/mysql` 当前只有 `ruoyi-vue-pro.sql` 和 `quartz.sql`。
- [ ] 检查 `yudao-module-clubpoints` 不存在，避免误以为模块已经开始。
- [ ] 记录验证命令和输出摘要。

验收：

- [ ] 事实记录写入当前开发记录或最终回复。
- [ ] 没有修改源码。

## 任务 M0.2 创建空模块

- [ ] 创建 `ruoyi-vue-pro-github/yudao-module-clubpoints/pom.xml`。
- [ ] 依赖 `yudao-module-system`。
- [ ] 依赖 `yudao-module-infra`。
- [ ] 依赖安全、MyBatis、Redis、Protection、Job、Excel、Test starter。
- [ ] 创建 `src/main/java/cn/iocoder/yudao/module/clubpoints/ClubPointsModule.java`。
- [ ] 创建 `src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`。
- [ ] 创建 `src/test/resources/sql/create_tables.sql` 空文件。
- [ ] 创建 `src/test/resources/sql/clean.sql` 空文件。

验收：

- [ ] 模块目录结构存在。
- [ ] `ErrorCodeConstants.java` 使用 `1_300_xxx_xxx` 段。
- [ ] 没有业务 Controller、Service、Mapper。

## 任务 M0.3 接入 Maven

- [ ] 根 `ruoyi-vue-pro-github/pom.xml` 增加 `<module>yudao-module-clubpoints</module>`。
- [ ] `ruoyi-vue-pro-github/yudao-server/pom.xml` 增加 `yudao-module-clubpoints` 依赖。
- [ ] 不修改 `system` 和 `infra` 反向依赖。
- [ ] 不恢复 member、bpm、pay、mall 等已删模块。

验收：

- [ ] Maven reactor 能识别 `yudao-module-clubpoints`。
- [ ] `system`、`infra` 没有依赖 `clubpoints`。

## 任务 M0.4 模块扫描验证

- [ ] 创建最小 Spring Bean 或配置类，用来证明 `clubpoints` 被 `yudao-server` 扫描。
- [ ] 不创建真实业务接口。
- [ ] 启动或轻量编译时确认 Bean 无依赖缺失。

验收：

- [ ] `mvn -pl yudao-server -am -DskipTests -Dflatten.skip=true compile` 通过，除非用户明确禁止本次验证。
- [ ] 若后端已运行，重启后 `48080` 仍返回未登录正常响应。
- [ ] 最小 Bean 被扫描的证据明确。

## 任务 M0.5 前后端底座回归

- [ ] 验证 `http://127.0.0.1:48080/admin-api/system/auth/get-permission-info` 返回 HTTP `200` 且业务码 `401`。
- [ ] 验证 `http://127.0.0.1:8889` 返回 HTTP `200`。
- [ ] 验证 Docker 容器 `yudao-mysql` 和 `yudao-redis` 仍在运行。
- [ ] 不跑 full build。

验收：

- [ ] 后端未登录响应正常。
- [ ] 前端 Vite 入口正常。
- [ ] Docker 依赖正常。

## M0 放行标准

- [ ] `clubpoints` 空模块存在。
- [ ] 根 POM 已接入模块。
- [ ] `yudao-server` 已依赖模块。
- [ ] `ErrorCodeConstants.java` 存在。
- [ ] 测试 SQL 空文件存在。
- [ ] 轻量编译或启动验证通过。

## M0 不通过时禁止

- [ ] 禁止进入 M1 写 SQL。
- [ ] 禁止写任何业务 Controller。
- [ ] 禁止写前端页面。

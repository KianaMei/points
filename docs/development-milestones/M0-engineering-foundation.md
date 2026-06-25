# M0 工程地基 Implementation Plan

**Status:** `[x]` M0 已放行。工程接入、Maven reactor、轻量编译、当前前后端端口回归均有证据；后端重启后回归未执行，保留为未勾选记录。

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

- [x] Docker MySQL 端口 `3306` 可用。
- [x] Docker Redis 端口 `6379` 可用。
- [x] 后端端口 `48080` 可用。
- [x] 前端端口 `8889` 可用。
- [x] 不恢复已删除的芋道业务模块。

## 任务 M0.1 底座事实复核

- [x] 检查根 POM 只包含 `yudao-dependencies`、`yudao-framework`、`yudao-server`、`yudao-module-system`、`yudao-module-infra`。
- [x] 检查 `yudao-server/pom.xml` 只依赖 `system` 和 `infra`，尚未依赖 `clubpoints`。
- [x] 检查 `sql/mysql` 当前只有 `ruoyi-vue-pro.sql` 和 `quartz.sql`。
- [x] 检查 `yudao-module-clubpoints` 不存在，避免误以为模块已经开始。
- [x] 记录验证命令和输出摘要。

验收：

- [x] 事实记录写入当前开发记录或最终回复。
- [x] 没有修改源码。

证据：

- `Select-String ruoyi-vue-pro-github/pom.xml -Pattern '<module>'`：当时仅有 `yudao-dependencies`、`yudao-framework`、`yudao-server`、`yudao-module-system`、`yudao-module-infra`。
- `Select-String ruoyi-vue-pro-github/yudao-server/pom.xml -Pattern '<artifactId>yudao-module-'`：当时仅依赖 `yudao-module-system`、`yudao-module-infra`。
- `Get-ChildItem ruoyi-vue-pro-github/sql/mysql`：当时只有 `quartz.sql`、`ruoyi-vue-pro.sql`。
- `Test-Path ruoyi-vue-pro-github/yudao-module-clubpoints`：当时返回 `False`。

## 任务 M0.2 创建空模块

- [x] 创建 `ruoyi-vue-pro-github/yudao-module-clubpoints/pom.xml`。
- [x] 依赖 `yudao-module-system`。
- [x] 依赖 `yudao-module-infra`。
- [x] 依赖安全、MyBatis、Redis、Protection、Job、Excel、Test starter。
- [x] 创建 `src/main/java/cn/iocoder/yudao/module/clubpoints/ClubPointsModule.java`。
- [x] 创建 `src/main/java/cn/iocoder/yudao/module/clubpoints/enums/ErrorCodeConstants.java`。
- [x] 创建 `src/test/resources/sql/create_tables.sql` 空文件。
- [x] 创建 `src/test/resources/sql/clean.sql` 空文件。

验收：

- [x] 模块目录结构存在。
- [x] `ErrorCodeConstants.java` 使用 `1_300_xxx_xxx` 段。
- [x] 没有业务 Controller、Service、Mapper。

证据：

- `Get-Item` 确认 `pom.xml`、`ClubPointsModule.java`、`ErrorCodeConstants.java`、`create_tables.sql`、`clean.sql` 均存在。
- `Select-String ErrorCodeConstants.java -Pattern '1_300_000_000'` 命中 `CLUB_POINTS_MODULE_NOT_INITIALIZED`。
- `Get-ChildItem yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints -Directory` 仅显示 `enums`，未创建 `controller`、`service`、`dal`、`mapper` 等业务目录。

## 任务 M0.3 接入 Maven

- [x] 根 `ruoyi-vue-pro-github/pom.xml` 增加 `<module>yudao-module-clubpoints</module>`。
- [x] `ruoyi-vue-pro-github/yudao-server/pom.xml` 增加 `yudao-module-clubpoints` 依赖。
- [x] 不修改 `system` 和 `infra` 反向依赖。
- [x] 不恢复 member、bpm、pay、mall 等已删模块。

验收：

- [x] Maven reactor 能识别 `yudao-module-clubpoints`。
- [x] `system`、`infra` 没有依赖 `clubpoints`。

证据：

- RED：`mvn -pl yudao-module-clubpoints -DskipTests -Dflatten.skip=true validate` 失败，Maven 报 `Could not find the selected project in the reactor: yudao-module-clubpoints`。
- GREEN：PowerShell 下运行 `mvn -pl yudao-module-clubpoints -DskipTests "-Dflatten.skip=true" validate`，结果 `BUILD SUCCESS`。
- `rg "yudao-module-clubpoints" yudao-module-system yudao-module-infra -n` 无命中，未形成反向依赖。

## 任务 M0.4 模块扫描验证

- [x] 创建最小 Spring Bean 或配置类，用来证明 `clubpoints` 被 `yudao-server` 扫描。
- [x] 不创建真实业务接口。
- [x] 启动或轻量编译时确认 Bean 无依赖缺失。

验收：

- [x] `mvn -pl yudao-server -am -DskipTests -Dflatten.skip=true compile` 通过，除非用户明确禁止本次验证。
- [ ] 若后端已运行，重启后 `48080` 仍返回未登录正常响应。
- [x] 最小 Bean 被扫描的证据明确。

证据：

- PowerShell 下运行 `mvn -pl yudao-server -am -DskipTests "-Dflatten.skip=true" compile`，结果 `BUILD SUCCESS`。
- Reactor 顺序包含 `yudao-module-clubpoints`，并显示 `yudao-module-clubpoints SUCCESS`、`yudao-server SUCCESS`。
- `YudaoServerApplication` 的 `scanBasePackages` 覆盖 `${yudao.info.base-package}.module`，`ClubPointsModule` 位于 `cn.iocoder.yudao.module.clubpoints` 并标注 `@Component`。
- 未执行后端重启；因此“重启后 48080”不标完成。

## 任务 M0.5 前后端底座回归

- [x] 验证 `http://127.0.0.1:48080/admin-api/system/auth/get-permission-info` 返回 HTTP `200` 且业务码 `401`。
- [x] 验证 `http://127.0.0.1:8889` 返回 HTTP `200`。
- [x] 验证 Docker 容器 `yudao-mysql` 和 `yudao-redis` 仍在运行。
- [x] 不跑 full build。

验收：

- [x] 后端未登录响应正常。
- [x] 前端 Vite 入口正常。
- [x] Docker 依赖正常。

证据：

- `Invoke-WebRequest http://127.0.0.1:48080/admin-api/system/auth/get-permission-info` 返回 HTTP `200`，内容 `{"code":401,"msg":"账号未登录","data":null}`。
- `Invoke-WebRequest http://127.0.0.1:8889` 返回 HTTP `200`。
- `docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"` 显示 `yudao-mysql`、`yudao-redis` 均 `Up`。

## M0 放行标准

- [x] `clubpoints` 空模块存在。
- [x] 根 POM 已接入模块。
- [x] `yudao-server` 已依赖模块。
- [x] `ErrorCodeConstants.java` 存在。
- [x] 测试 SQL 空文件存在。
- [x] 轻量编译或启动验证通过。

## M0 不通过时禁止

- 禁止进入 M1 写 SQL。
- 禁止写任何业务 Controller。
- 禁止写前端页面。

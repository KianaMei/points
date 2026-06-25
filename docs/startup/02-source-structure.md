# 源码结构和环境

## 仓库根

```text
C:\jobs\pointsmall
```

## 芋道源码根

```text
C:\jobs\pointsmall\ruoyi-vue-pro-github
```

## 当前保留后端模块

- `yudao-dependencies`
- `yudao-framework`
- `yudao-server`
- `yudao-module-system`
- `yudao-module-infra`

## 业务模块目标

- 新增 `yudao-module-clubpoints`
- 根 `pom.xml` 增加 module。
- `yudao-server/pom.xml` 增加依赖。
- 模块形态照 system/infra：单 Maven 工程，不拆 api/biz 子模块。

## 后端包结构

```text
api/ controller/ convert/ dal/{dataobject,mysql,redis}/
enums/ framework/ job/ mq/ service/
```

## 前端工程

```text
ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3
```

前端新增目录：`src/api/clubpoints/`、`src/views/clubpoints/`

## SQL 位置

- 正式 schema：`ruoyi-vue-pro-github/sql/mysql/club-points-schema.sql`
- 正式 seed：`ruoyi-vue-pro-github/sql/mysql/club-points-seed.sql`
- 测试建表：`yudao-module-clubpoints/src/test/resources/sql/create_tables.sql`
- 测试清表：`yudao-module-clubpoints/src/test/resources/sql/clean.sql`

## 本地运行事实

- MySQL：Docker，端口 `3306`
- Redis：Docker，端口 `6379`
- 后端：本地 JDK / IDE，端口 `48080`
- 前端：本地 pnpm/npm，端口 `8889`
- 项目 POM 标称 Java：`1.8`

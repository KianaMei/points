# 前端规则

## 工程

```text
ruoyi-vue-pro-github/yudao-ui/yudao-ui-admin-vue3
```

开发入口：

```text
http://localhost:8889
```

## 新增目录

```text
src/api/clubpoints/{app,leader,admin,shared}/
src/views/clubpoints/{app,leader,admin,components}/
```

## 组件基线

- 复用现有 `ContentWrap`、`Dialog`、`Pagination`。
- 复用 `dict-tag`、`UploadFile`、`UserSelectForm`、`DeptSelectForm`。
- 继续用 Element Plus。
- 请求封装复用 `@/config/axios`。

## 路由和菜单

- 路由来自后端菜单 seed。
- 前端不写静态业务路由作为事实源。
- 员工路由前缀：`/clubpoints/app/*`
- 负责人路由前缀：`/clubpoints/leader/*`
- 管理员路由前缀：`/clubpoints/admin/*`

## 权限按钮

- 按钮用 `v-hasPermi` 隐藏。
- `v-hasPermi` 只改善体验，不是安全边界。
- 后端仍必须校验功能权限和数据范围。

## 强确认

- 只有管理员物理删除俱乐部用强确认。
- 活动删除、材料删除、停用、取消只用普通确认。
- 强确认字段：`strongConfirm.confirmText`、`strongConfirm.confirmedAt`。

## 请求号

- 兑换申请、管理员代录、积分调整需要前端生成 `requestNo`。
- 同一次提交失败后重试，不要重新生成 `requestNo`。

详情查 `docs/club-points-frontend-page-design.md`。

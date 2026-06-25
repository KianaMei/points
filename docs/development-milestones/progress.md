# 开发过程记录

## M0 工程地基

- 2026-06-25：读取 `AGENTS.md`、启动文档、里程碑索引、共享执行规则和 `M0-engineering-foundation.md`。
- 2026-06-25：事实复核：当前源码根 `ruoyi-vue-pro-github` 只有 `yudao-dependencies`、`yudao-framework`、`yudao-server`、`yudao-module-system`、`yudao-module-infra` 五个后端模块；`yudao-server` 只依赖 system/infra；`sql/mysql` 只有 `ruoyi-vue-pro.sql` 和 `quartz.sql`；`yudao-module-clubpoints` 不存在。
- 2026-06-25：RED 验证：`mvn -pl yudao-module-clubpoints -DskipTests -Dflatten.skip=true validate` 失败，原因是 Maven reactor 找不到 `yudao-module-clubpoints`。
- 2026-06-25：GREEN 实现：创建 `yudao-module-clubpoints` 空模块、`ClubPointsModule` marker bean、`ErrorCodeConstants` 错误码入口、测试 SQL 占位文件；根 POM 接入 module；`yudao-server` 接入模块依赖。
- 2026-06-25：模块识别验证：PowerShell 下使用 `mvn -pl yudao-module-clubpoints -DskipTests "-Dflatten.skip=true" validate`，结果 `BUILD SUCCESS`。
- 2026-06-25：后端轻量编译验证：`mvn -pl yudao-server -am -DskipTests "-Dflatten.skip=true" compile`，reactor 包含 `yudao-module-clubpoints`，20 个 reactor 项全部 `SUCCESS`。
- 2026-06-25：运行态底座验证：`docker ps` 显示 `yudao-mysql`、`yudao-redis` 均 `Up`；`Invoke-WebRequest http://127.0.0.1:48080/admin-api/system/auth/get-permission-info` 返回 HTTP `200`，业务响应 `{"code":401,"msg":"账号未登录","data":null}`；`Invoke-WebRequest http://127.0.0.1:8889` 返回 HTTP `200`。

## M1 数据库和 Seed

- 2026-06-25：M1 RED 基线：`club-points-schema.sql` 和 `club-points-seed.sql` 不存在；`yudao-module-clubpoints/src/test/resources/sql/create_tables.sql` 与 `clean.sql` 均为 1 字节占位。
- 2026-06-25：读取数据库设计表清单、通用字段、状态枚举、规则表、俱乐部表、关键唯一约束和推荐落库顺序；确认数据库设计是 M1 单一输入。
- 2026-06-25：完成 M1.1-M1.3 第一批 GREEN：创建正式 schema/seed 骨架，落地 `club_points_rule_version`、`club_points_rule_item`、`club_points_rule_publish_record`、`club_points_club`、`club_points_club_member`、`club_points_club_leader`。
- 2026-06-25：MySQL 验证：创建临时库 `club_points_m1_partial` 并导入 `club-points-schema.sql` 成功；`SHOW TABLES LIKE 'club_points_%'` 返回当前 6 张表。
- 2026-06-25：唯一键验证：重复插入规则版本号、规则项 `(rule_version_id,item_code)`、俱乐部编号、俱乐部名称、成员 `active_unique_key`、负责人 `active_unique_key` 均被 MySQL `ERROR 1062` 拒绝。
- 2026-06-25：完成 M1.4：落地活动、活动审核、活动积分配置版本、报名、签到签退、签到修正、活动结算运行 7 张表；特殊缺席保留在报名表字段中，没有拆独立表。
- 2026-06-25：M1.4 MySQL 验证：重新创建临时库 `club_points_m1_partial` 并导入当前 schema 成功；当前 `club_points_*` 表数为 13。
- 2026-06-25：M1.4 唯一键验证：活动配置 `(activity_id,version_no)`、报名 `active_unique_key`、签到 `(registration_id,target_type)`、结算 `run_key` 的重复插入均被 MySQL `ERROR 1062` 拒绝。
- 2026-06-25：完成 M1.5：落地积分流水、账户缓存、冻结、员工年度状态 4 张表；账户缓存按数据库设计使用 `user_id` 唯一，未添加计划文本中不存在的 `year` 字段。
- 2026-06-25：M1.5 MySQL 验证：重新创建临时库 `club_points_m1_partial` 并导入当前 schema 成功；当前 `club_points_*` 表数为 17。
- 2026-06-25：M1.5 唯一键验证：流水号、流水幂等键、撤销原流水 ID、账户 `user_id`、冻结号、冻结幂等键、冻结 `(source_type,source_id)`、年度状态 `(user_id,year)` 的重复插入均被 MySQL `ERROR 1062` 拒绝。
- 2026-06-25：完成 M1.6：落地非签到材料、材料明细、材料审核记录 3 张表。
- 2026-06-25：M1.6 MySQL 验证：重新创建临时库 `club_points_m1_partial` 并导入当前 schema 成功；当前 `club_points_*` 表数为 20。
- 2026-06-25：M1.6 唯一键验证：材料 `request_no`、明细 `idempotency_key`、明细 `effective_unique_key` 的重复插入均被 MySQL `ERROR 1062` 拒绝。
- 2026-06-25：完成 M1.7：落地兑换批次、礼品、资格快照、兑换申请、库存锁、兑换审核记录 6 张表；保留冻结、库存锁、申请分表。
- 2026-06-25：M1.7 MySQL 验证：重新创建临时库 `club_points_m1_partial` 并导入当前 schema 成功；当前 `club_points_*` 表数为 26。
- 2026-06-25：M1.7 唯一键验证：资格快照 `(batch_id,user_id)`、申请号、申请幂等键、库存锁 `application_id`、库存锁幂等键的重复插入均被 MySQL `ERROR 1062` 拒绝。
- 2026-06-25：完成 M1.8：落地异议、年度清零、年度排名、激励、预算、附件绑定、强审计、业务任务运行 8 张表；未新增导出主表。
- 2026-06-25：M1.8 MySQL 验证：重新创建临时库 `club_points_m1_partial` 并导入当前 schema 成功；当前 `club_points_*` 表数为 34。
- 2026-06-25：M1.8 唯一键验证：年度清零 `(year,user_id)`、年度清零幂等键、年度排名 `(year,club_code_snapshot)`、任务运行幂等键的重复插入均被 MySQL `ERROR 1062` 拒绝。
- 2026-06-25：完成 M1.9：写入角色、菜单、按钮权限、角色授权、字典类型和值、默认规则版本、默认规则项、通知模板、定时任务 seed。
- 2026-06-25：M1.9 MySQL 验证：创建临时库 `club_points_m1_seed`，依次导入芋道基线 SQL、clubpoints schema、clubpoints seed 成功。
- 2026-06-25：M1.9 覆盖性验证：API 文档权限码 53 个，seed 按钮权限 53 个，集合一致；本模块角色 3 个、角色授权 105 条、字典类型 17 个、字典值 66 个、默认规则项 22 个、通知模板 4 个、定时任务 3 个。
- 2026-06-25：M1.9 幂等验证：重复导入 seed 后核心计数不变；精确搜索租户、云密钥、邮件/短信账号类 seed 无命中。
- 2026-06-25：完成 M1.10：从正式 schema 机械转换测试 DDL，`json` 降级为 H2 兼容 `longtext`，并写入 34 张表的清表 SQL。
- 2026-06-25：M1.10 对齐验证：正式 schema 与测试 DDL 均为 34 张表；字段集合差异 `0`；唯一键集合为 `30` 对 `30`，差异 `0`。
- 2026-06-25：M1.10 H2 验证：使用 H2 2.1.214 file 库和 `MODE=MYSQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=value` 加载 `create_tables.sql` 返回 `h2_file_create_EXIT=0`，加载 `clean.sql` 返回 `h2_file_clean_EXIT=0`。
- 2026-06-25：M1 放行：正式 schema、seed、测试 DDL、clean SQL 均存在并有验证证据；`00-index.md` 当前入口切换到 M2。

## M2 权限范围和横切能力

- 2026-06-25：读取 M2 计划、权限审计启动文档、权限矩阵、API 权限映射和前端页面路由；确认 M2.1 先处理权限码、菜单路径和角色授权。
- 2026-06-25：M2.1 RED 验证：当前 seed 导入临时库 `club_points_m2_perm_red` 后，`clubpoints:registration:special-absence` 权限数为 `0`；三端组件路径 `clubpoints/app/%`、`clubpoints/leader/%`、`clubpoints/admin/%` 均只有 `1`；员工角色误拿 `clubpoints:redemption:review`。
- 2026-06-25：完成 M2.1 GREEN：重写 `club-points-seed.sql` 菜单为员工端、负责人端、管理员端三段结构；补 `clubpoints:registration:special-absence`；重写员工、负责人、管理员角色建议授权。
- 2026-06-25：M2.1 导入验证：临时库 `club_points_m2_perm_green` 依次导入 `ruoyi-vue-pro.sql`、`club-points-schema.sql`、`club-points-seed.sql` 成功。
- 2026-06-25：M2.1 权限和菜单验证：本模块菜单和按钮共 `89` 条，按钮权限 `54` 个，特殊缺席权限 `1` 个；组件路径 `clubpoints/app/%` 为 `7` 条、`clubpoints/leader/%` 为 `5` 条、`clubpoints/admin/%` 为 `19` 条。
- 2026-06-25：M2.1 权限集合验证：API 设计权限码 `53` 个，前端页面设计权限码 `53` 个，并集 `54` 个；seed 权限码 `54` 个；缺失 `0`，多余 `0`。
- 2026-06-25：M2.1 角色边界验证：负责人禁止权限计数 `0`，员工管理权限计数 `0`；重复导入 seed 后菜单 `89`、员工授权 `18`、负责人授权 `42`、管理员授权 `89`，计数不膨胀。
- 2026-06-25：M2.1 冲突记录：前端页面设计和功能清单都包含特殊缺席，API 文档未列独立权限码；M2.1 已按功能存在和前端按钮权限补 `clubpoints:registration:special-absence`，后续 API 实现必须补齐 Controller 权限。
- 2026-06-25：M2.2 RED：新增 `DictTypeConstantsTest`，用反射比对 `DictTypeConstants` 常量集合和 seed 字典类型集合；目标失败确认为 `DictTypeConstants` 不存在。早先两次 Maven 命令分别失败在依赖解析和 PowerShell 属性转义，不计入 RED。
- 2026-06-25：M2.2 GREEN：创建 `DictTypeConstants.java`，补齐 `23` 个本模块字典类型常量；seed 新增负责人状态、报名取消原因、规则版本状态、规则项状态、流水来源类型、年度清零状态字典类型和值。
- 2026-06-25：M2.2 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=DictTypeConstantsTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`DictTypeConstantsTest` 运行 `2` 个测试，失败 `0`，错误 `0`。
- 2026-06-25：M2.2 MySQL 验证：临时库 `club_points_m2_dict_green` 导入基线 SQL、clubpoints schema、clubpoints seed 成功；本模块字典类型 `23` 个，字典值 `89` 个，M2 必需字典类型命中 `11` 个。
- 2026-06-25：M2.2 幂等验证：重复导入 `club-points-seed.sql` 后，本模块字典类型仍为 `23` 个，字典值仍为 `89` 个。
- 2026-06-25：M2.2 前端硬编码验证：当前 `yudao-ui-admin-vue3/src/views/clubpoints` 不存在，`rg --files` 只发现后端模块和 SQL 中的 `clubpoints` 文件；现阶段没有前端中文状态硬编码。
- 2026-06-25：M2.3 RED：新增 `ClubScopeServiceImplTest`，覆盖本人、已加入俱乐部、负责俱乐部、全局范围判断和负责人越权访问其他俱乐部；目标失败确认为 `ClubScopeService`、`ClubScopeServiceImpl`、成员/负责人 DO 与 Mapper、`CLUB_SCOPE_DENIED` 不存在。
- 2026-06-25：M2.3 GREEN：新增 `ClubMemberDO`、`ClubLeaderDO`、`ClubMemberMapper`、`ClubLeaderMapper`、`ClubScopeService`、`ClubScopeServiceImpl`，补 `CLUB_SCOPE_DENIED` 错误码；范围服务统一以有效状态 `1` 判断成员和负责人关系。
- 2026-06-25：M2.3 测试基础设施：新增 `yudao-module-clubpoints/src/test/resources/application-unit-test.yaml`，让本模块 DB 单测可解析 `yudao.info.base-package` 并加载 H2 `create_tables.sql`。
- 2026-06-25：M2.3 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubScopeServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubScopeServiceImplTest` 运行 `10` 个测试，失败 `0`，错误 `0`。
- 2026-06-25：M2.3 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=DictTypeConstantsTest,ClubScopeServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `12` 个测试，失败 `0`，错误 `0`。第一次组合命令因 PowerShell 逗号未加引号被解析失败，不计入测试失败。
- 2026-06-25：M2.3 质量验证：`git diff --check` 无空白错误；`rg -n "tenant_id|TenantBaseDO|Co-authored|co-author|AI" yudao-module-clubpoints` 无命中。
- 2026-06-25：M2.4 RED：新增 `ClubAuditServiceImplTest`，覆盖审计字段完整落库、失败结果落库、高风险动作常量、缺失必填字段抛统一错误、审计失败回滚业务插入；目标失败确认为审计 Service/DO/Mapper/BO/动作常量/错误码不存在。
- 2026-06-25：M2.4 GREEN：新增 `ClubAuditLogDO`、`ClubAuditLogMapper`、`ClubAuditCreateReqBO`、`ClubAuditService`、`ClubAuditServiceImpl`、`ClubAuditActionTypeConstants`，补 `CLUB_AUDIT_WRITE_FAILED` 错误码；审计写入使用默认事务传播，参与业务事务。
- 2026-06-25：M2.4 冲突处理：M2.4 清单要求“请求参数摘要”，但 `club_points_audit_log` 无独立 `request_params_summary` 字段；按数据库设计将该摘要落到 `target_snapshot_json`，未擅自改表。
- 2026-06-25：M2.4 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubAuditServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubAuditServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- 2026-06-25：M2.4 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=DictTypeConstantsTest,ClubScopeServiceImplTest,ClubAuditServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `17` 个测试，失败 `0`，错误 `0`。
- 2026-06-25：M2.4 质量验证：`git diff --check` 无空白错误；`rg -n "tenant_id|TenantBaseDO" yudao-module-clubpoints` 无命中；裸 `AI` 搜索会误命中 `FAILED`，改用 `rg -n "Co-authored|co-author|AI-generated|Generated by AI|ChatGPT" yudao-module-clubpoints` 检查元数据，无命中。
- 2026-06-25：M2.5 RED：新增 `ClubAttachmentServiceImplTest`，覆盖文件附件绑定复用 infra 文件、外部链接附件不调用文件服务、审核通过后锁定业务附件、锁定附件禁止删除、未锁定附件软删除；目标失败确认为附件 Service/DO/Mapper/BO/常量/错误码不存在。
- 2026-06-25：M2.5 GREEN：新增 `ClubAttachmentConstants`、`ClubAttachmentRefDO`、`ClubAttachmentRefMapper`、`ClubAttachmentBindReqBO`、`ClubAttachmentService`、`ClubAttachmentServiceImpl`，补附件参数无效、不存在、已锁定错误码。
- 2026-06-25：M2.5 实现边界：文件附件只调用 `FileService.getFile(fileId)` 校验文件存在并写 `club_points_attachment_ref`，不复制 `infra_file`；URL 附件不调用 `FileService`；普通删除只置 `status = 3`，不提供绕过强审计的物理删除材料入口。
- 2026-06-25：M2.5 测试适配：`BaseMapperX` 不存在 `insertAndGetId`，测试改为 `insert` 后读取 DO 回填 ID，未为测试在生产 Mapper 增加辅助方法。
- 2026-06-25：M2.5 单测验证：`mvn -pl yudao-module-clubpoints -am -Dtest=ClubAttachmentServiceImplTest "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；`ClubAttachmentServiceImplTest` 运行 `5` 个测试，失败 `0`，错误 `0`。
- 2026-06-25：M2.5 组合验证：`mvn -pl yudao-module-clubpoints -am "-Dtest=DictTypeConstantsTest,ClubScopeServiceImplTest,ClubAuditServiceImplTest,ClubAttachmentServiceImplTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dflatten.skip=true" test` 返回 `BUILD SUCCESS`；合计 `22` 个测试，失败 `0`，错误 `0`。
- 2026-06-25：M2.5 质量验证：`git diff --check` 无空白错误；`rg -n "tenant_id|TenantBaseDO" yudao-module-clubpoints` 无命中；`rg -n "Co-authored|co-author|AI-generated|Generated by AI|ChatGPT" yudao-module-clubpoints` 无命中；`rg -n "createFile\(" yudao-module-clubpoints/src/main/java/cn/iocoder/yudao/module/clubpoints/service/attachment` 无命中。
- 2026-06-25：M2.5 文档同步：`M2-permission-crosscutting.md` 勾选 M2.5 并补证据，`00-index.md` 当前入口切换到 M2.6 `ClubNotifyService`。

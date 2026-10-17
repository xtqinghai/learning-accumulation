# h_boot

#### 介绍
**1、h-boot**
是SpringBoot+Vue前后端分离的快速开发平台后端程序，基于SpringBoot3.5+，MyBatisPlus3.5+，MySQL8+/PostgreSQL17+，JDK17+

**2、h-boot**
是主要是用来学习

**3、h-boot**
参考和运用到了很多优秀的开源的项目和组件(您在参考本项目时，需要注意原作者的许可，使用时请务必遵守和获取原作者的许可)

#### 软件架构
软件架构说明


#### 安装教程

1. 使用jdk 17+
2. idea maven 引入

#### 使用说明

1.  sql目录为数据库脚本，需要创建数据库并导入
2.  修改application.yml文件的数据库配置
3.  idea运行项目入口主程序[HBootApplication.java](src%2Fmain%2Fjava%2Fcom%2Fquinn%2Fhboot%2FHBootApplication.java)

#### 本地依赖启动

以下命令适用于通过 Homebrew 安装的 PostgreSQL 17 和 Redis，均为手动启动方式，不会新增开机启动项。

**PostgreSQL**

```bash
# 启动
pg_ctl -D /opt/homebrew/var/postgresql@17 -l /opt/homebrew/var/log/postgresql@17.log start

# 检查状态和数据库连接
pg_ctl -D /opt/homebrew/var/postgresql@17 status
pg_isready -h localhost -p 5432
psql -h localhost -U pingguo -d h_boot

# 停止
pg_ctl -D /opt/homebrew/var/postgresql@17 stop
```

开发环境默认连接 `jdbc:postgresql://localhost:5432/h_boot`，用户名为 `pingguo`。可通过 `HBOOT_DB_URL`、`HBOOT_DB_USERNAME` 和 `HBOOT_DB_PASSWORD` 覆盖。

**Redis**

```bash
# 前台启动
redis-server /opt/homebrew/etc/redis.conf

# 或手动后台启动
redis-server /opt/homebrew/etc/redis.conf --daemonize yes

# 检查状态，正常返回 PONG
redis-cli ping

# 停止后台 Redis
redis-cli shutdown
```

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

#### warm-flow参考
https://gitee.com/dromara/RuoYi-Vue-Plus



阶段一（现在 – 6 个月）

目标：AI + 现有系统融合

Spring AI

Ollama

向量数据库

SSE / 流式输出

RAG 实战

📌 成果：

一个“AI 能用起来的管理系统”

阶段二（6 – 18 个月）

目标：平台化 & 架构能力

插件化设计

事件驱动

规则引擎

分布式任务

OpenTelemetry

📌 成果：

一个可扩展、可插拔的平台

阶段三（18 个月以上）

目标：架构师 / 技术负责人

AI Agent 编排

企业知识系统

多团队协作架构

技术选型能力


# RBAC权限 ER图
![mermaid-diagram.png](mermaid-diagram.png)

# h-boot 环境变量与启动说明

## 1. 环境基线

- `dev`
  - 本地开发环境
  - 默认开启 Swagger、Druid、Warm-Flow UI
  - 允许使用本地默认地址作为示例，但敏感账号密码建议通过 `.env` 显式提供
- `test`
  - 测试 / 联调环境
  - 默认关闭 Swagger、Druid、Warm-Flow UI
  - 所有数据库、Redis、文件存储地址必须显式配置
- `prod`
  - 生产环境
  - 默认关闭 Swagger、Druid、Warm-Flow UI
  - 不提供弱口令、默认账号、默认上传目录
  - 必须通过环境变量显式提供数据库、Redis、文件存储等配置
- `debug`
  - 基于 `dev` 的调试环境
  - 继承 `dev` 配置，并增加更详细的日志

## 2. 配置文件职责

- [application.yaml](../src/main/resources/application.yaml)
  - 放跨环境通用基础配置
  - 包含认证、上传安全、缓存、通用开关默认值
- [application-dev.yaml](../src/main/resources/application-dev.yaml)
  - 开发环境专用
  - 默认开启 Swagger、Druid、Warm-Flow UI
- [application-test.yaml](../src/main/resources/application-test.yaml)
  - 测试环境专用
  - 默认关闭 Swagger、Druid、Warm-Flow UI
- [application-prod.yaml](../src/main/resources/application-prod.yaml)
  - 生产环境专用
  - 默认关闭 Swagger、Druid、Warm-Flow UI
  - 不再提供默认上传目录

## 3. `.env.example` 使用说明

- 模板文件位置：
  - [.env.example](../.env.example)
- 建议做法：
  - 本地开发复制为 `.env`
  - 测试 / 生产环境通过系统环境变量、容器平台、CI/CD Secret 注入
- 注意：
  - `.env.example` 只是示例模板
  - 不要把真实密码、生产地址、访问密钥提交到仓库

## 4. 生产环境必填变量

生产环境至少应显式提供以下变量：

```text
SPRING_PROFILES_ACTIVE=prod
HBOOT_DB_URL=
HBOOT_DB_USERNAME=
HBOOT_DB_PASSWORD=
ENGINE_DB_URL=
ENGINE_DB_USERNAME=
ENGINE_DB_PASSWORD=
REDIS_HOST=
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0
FILE_LOCAL_BASE_PATH=
FILE_PUBLIC_URL=
```

建议同时显式提供以下安全相关变量：

```text
APP_API_DOCS_ENABLED=false
APP_DRUID_MONITOR_ENABLED=false
WARM_FLOW_UI_ENABLED=false
WARM_FLOW_BANNER=false
APP_DRUID_MONITOR_USERNAME=
APP_DRUID_MONITOR_PASSWORD=
```

## 5. 启动方式

### 5.1 开发环境

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:HBOOT_DB_URL="jdbc:mysql://localhost:13306/h_boot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:HBOOT_DB_USERNAME="root"
$env:HBOOT_DB_PASSWORD="replace-me"
$env:ENGINE_DB_URL=$env:HBOOT_DB_URL
$env:ENGINE_DB_USERNAME="root"
$env:ENGINE_DB_PASSWORD="replace-me"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:FILE_LOCAL_BASE_PATH="$env:USERPROFILE\\upload-dev"
$env:FILE_PUBLIC_URL="http://localhost:8080/api/v1/file/preview"
$env:APP_API_DOCS_ENABLED="true"
$env:APP_DRUID_MONITOR_ENABLED="true"
$env:APP_DRUID_MONITOR_USERNAME="devadmin"
$env:APP_DRUID_MONITOR_PASSWORD="replace-me"
.\mvnw spring-boot:run
```

### 5.2 测试环境

```powershell
$env:SPRING_PROFILES_ACTIVE="test"
$env:HBOOT_DB_URL="jdbc:mysql://test-db:3306/h_boot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:HBOOT_DB_USERNAME="hboot_test"
$env:HBOOT_DB_PASSWORD="replace-me"
$env:ENGINE_DB_URL=$env:HBOOT_DB_URL
$env:ENGINE_DB_USERNAME="hboot_test"
$env:ENGINE_DB_PASSWORD="replace-me"
$env:REDIS_HOST="test-redis"
$env:REDIS_PORT="6379"
$env:FILE_LOCAL_BASE_PATH="D:\\hboot\\upload-test"
$env:FILE_PUBLIC_URL="https://test.example.com/api/v1/file/preview"
.\mvnw spring-boot:run
```

### 5.3 生产环境

先打包：

```powershell
.\mvnw -DskipTests package
```

再启动：

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:HBOOT_DB_URL="jdbc:mysql://prod-db:3306/h_boot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:HBOOT_DB_USERNAME="hboot"
$env:HBOOT_DB_PASSWORD="replace-me"
$env:ENGINE_DB_URL=$env:HBOOT_DB_URL
$env:ENGINE_DB_USERNAME="hboot"
$env:ENGINE_DB_PASSWORD="replace-me"
$env:REDIS_HOST="prod-redis"
$env:REDIS_PORT="6379"
$env:REDIS_PASSWORD="replace-me"
$env:FILE_LOCAL_BASE_PATH="/data/hboot/upload"
$env:FILE_PUBLIC_URL="https://your-domain/api/v1/file/preview"
$env:APP_API_DOCS_ENABLED="false"
$env:APP_DRUID_MONITOR_ENABLED="false"
$env:WARM_FLOW_UI_ENABLED="false"
$env:WARM_FLOW_BANNER="false"
java -jar .\target\h-boot-0.0.1-SNAPSHOT.jar
```

Linux 示例：

```bash
export SPRING_PROFILES_ACTIVE=prod
export HBOOT_DB_URL='jdbc:mysql://prod-db:3306/h_boot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'
export HBOOT_DB_USERNAME='hboot'
export HBOOT_DB_PASSWORD='replace-me'
export ENGINE_DB_URL="$HBOOT_DB_URL"
export ENGINE_DB_USERNAME='hboot'
export ENGINE_DB_PASSWORD='replace-me'
export REDIS_HOST='prod-redis'
export REDIS_PORT='6379'
export REDIS_PASSWORD='replace-me'
export FILE_LOCAL_BASE_PATH='/data/hboot/upload'
export FILE_PUBLIC_URL='https://your-domain/api/v1/file/preview'
export APP_API_DOCS_ENABLED='false'
export APP_DRUID_MONITOR_ENABLED='false'
export WARM_FLOW_UI_ENABLED='false'
export WARM_FLOW_BANNER='false'
java -jar target/h-boot-0.0.1-SNAPSHOT.jar
```

## 6. 当前健康检查现状

- 当前已可基于 Spring Boot `health` 端点暴露基础健康状态
- 已有明确配置接入的健康项：
  - 数据库
  - Redis
- 对象存储、短信、邮件：
  - 当前项目还没有稳定的正式接入配置
  - 本次先完成环境基线和配置梳理
  - 后续在实际接入对应服务后，再补充专用 `HealthIndicator`

## 7. 生产环境建议

- 使用 `JDK 17`
- 不要在 `prod` 中开启 Swagger、Druid、Warm-Flow UI
- 不要依赖默认路径、默认口令、默认账号
- 上传目录应由运维明确指定，并保证权限最小化
- 环境变量建议由部署平台统一托管，不要写死在启动脚本中

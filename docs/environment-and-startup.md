# h-boot 环境变量与启动方式

## 1. Profile 约定

- `dev`: 本地开发默认环境，开启 Swagger、Druid、Warm-Flow UI。
- `debug`: 在 `dev` 基础上追加更详细的 SQL/ORM 日志。
- `prod`: 生产环境，默认关闭 Swagger、Druid、Warm-Flow UI。

`debug` 已通过 profile group 继承 `dev`，因此只指定 `debug` 也会带上开发数据源与 Redis 配置。

## 2. 必填环境变量

生产环境至少需要配置以下变量：

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
FILE_PUBLIC_URL=
FILE_LOCAL_BASE_PATH=/data/hboot/upload
```

以下变量默认关闭或可选：

```text
APP_API_DOCS_ENABLED=false
APP_DRUID_MONITOR_ENABLED=false
APP_DRUID_MONITOR_USERNAME=
APP_DRUID_MONITOR_PASSWORD=
WARM_FLOW_ENABLED=true
WARM_FLOW_UI_ENABLED=false
WARM_FLOW_BANNER=false
```

## 3. PowerShell 启动示例

### 开发环境

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:HBOOT_DB_URL="jdbc:mysql://localhost:13306/h_boot?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:HBOOT_DB_USERNAME="root"
$env:HBOOT_DB_PASSWORD="123456"
$env:ENGINE_DB_URL=$env:HBOOT_DB_URL
$env:ENGINE_DB_USERNAME="root"
$env:ENGINE_DB_PASSWORD="123456"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:FILE_LOCAL_BASE_PATH="c:/upload"
$env:FILE_PUBLIC_URL="http://localhost:8080/api/v1/file/preview"
$env:APP_API_DOCS_ENABLED="true"
$env:APP_DRUID_MONITOR_ENABLED="true"
$env:APP_DRUID_MONITOR_USERNAME="devadmin"
$env:APP_DRUID_MONITOR_PASSWORD="change-me"
.\mvnw spring-boot:run
```

### 调试环境

```powershell
$env:SPRING_PROFILES_ACTIVE="debug"
.\mvnw spring-boot:run
```

### 生产环境

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
$env:FILE_LOCAL_BASE_PATH="/data/hboot/upload"
$env:FILE_PUBLIC_URL="https://your-domain/api/v1/file/preview"
.\mvnw -DskipTests package
java -jar .\target\h-boot-0.0.1-SNAPSHOT.jar
```

## 4. Linux 启动示例

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
export FILE_LOCAL_BASE_PATH='/data/hboot/upload'
export FILE_PUBLIC_URL='https://your-domain/api/v1/file/preview'
java -jar target/h-boot-0.0.1-SNAPSHOT.jar
```

## 5. 当前约束

- 运行环境需要 `JDK 17`。
- `prod` 默认不会暴露 Swagger、Druid 和 Warm-Flow UI。
- 如果开启 Druid 监控，必须显式配置 `APP_DRUID_MONITOR_USERNAME` 和 `APP_DRUID_MONITOR_PASSWORD`。

# 会议管理系统 - AI 编码助手说明

## 项目概述

这是一个基于 Spring Boot 3.5.6 + Java 21 的会议管理系统，使用 Netty 实现 WebSocket 实时通信。支持会议创建/加入、好友管理、文件上传（腾讯云 COS）、用户信息管理等功能，并提供 Redis/RabbitMQ 灵活的消息处理方案。

## 技术架构

### 核心技术栈

- **后端框架**: Spring Boot 3.5.6, Java 21, MyBatis-Plus
- **数据库**: MySQL 8.0+
- **缓存/消息**: Redis, Redisson (分布式锁), RabbitMQ
- **WebSocket**: Netty 4.2.6（独立端口 8082）
- **对象存储**: 腾讯云 COS
- **API 文档**: Knife4j (Swagger)
- **工具库**: Hutool, Lombok

### 应用架构

```
REST API (端口 8081, 上下文路径: /api)
    ↓
Controllers → Services → Mappers → MySQL
    ↓                ↓           ↓
  Redis          RabbitMQ/Redis  腾讯云COS
    ↓              (消息处理)    (文件存储)
WebSocket (端口 8082, Netty)
```

## 核心设计模式

### 1. 认证与鉴权

**基于 Token 的认证模式**:

- Token 存储在 Redis，key 格式: `UserConstant.TOKEN + token` 或 `UserConstant.TOKEN + userId`
- 使用 `@GlobalInterceptor` 注解进行鉴权检查:
  ```java
  @GlobalInterceptor(checkLogin = true)  // 需要登录
  @GlobalInterceptor(checkAdmin = true)  // 需要管理员权限
  ```
- `GlobalOperationAspect` 拦截并通过 `RedisComponent.getTokenUserInfo()` 验证 Token
- 控制器继承 `BaseController`，使用 `getTokenUserInfo()` 获取当前用户信息

### 2. 统一响应处理

**标准化响应结构**:

- 所有接口返回 `BaseResponse<T>`，包含 `code`、`message`、`data`
- 成功响应使用 `ResultUtils.success(data)` (code=0)
- 失败响应使用 `ResultUtils.error(ErrorCode)`
- `GlobalExceptionHandler` 全局捕获 `BusinessException` 和 `RuntimeException`

### 3. WebSocket 通信 (Netty)

**双通道架构**:

- **REST API**: 业务逻辑（端口 8081）
- **WebSocket**: 实时通信（端口 8082）
- `InitRun` (ApplicationRunner) 启动时启动 Netty 服务器
- Handler 链: `TokenHandler` → `WebSocketServerProtocolHandler` → `WebSocketHandler`
- 消息类型定义在 `MessageTypeEnum` (INIT, ADD_MEETING_ROOM, PEER, CHAT 等)

### 4. 灵活的消息处理

**基于配置的策略** (`application.yml: message.handle.channel`):

- `redis`: 使用 Redis 发布/订阅 (`MsgHandler4Redis`)
- `rabbitmq`: 使用 RabbitMQ 扇形交换机 (`MsgHandler4Rabbitmq`)
- `@ConditionalOnProperty` 确保只加载一个实现
- 两者都实现 `MsgHandler` 接口的 `sendMessage()` 和 `listener()` 方法

### 5. 文件上传（腾讯云 COS）

**文件管理架构**:

- `CosConfig`: 配置腾讯云 COS 连接参数（secretId、secretKey、bucket、region）
- `CosComponent`: 封装文件上传、删除操作
- `FileService`: 文件业务逻辑，包括文件记录保存
- 文件存储路径: `folder/yyyy-MM-dd/uuid.ext`
- 支持文件类型: 任意类型（头像限制为图片，最大 5MB）

### 6. 会议生命周期

**关键操作**:

1. **快速会议**: `MeetingController.quickMeeting()` 创建即时会议，更新用户的 `TokenUserInfo.meetingId`
2. **加入会议**: `preJoinMeeting()` → `joinMeeting()` (验证后通过 WebSocket 广播)
3. **退出会议**: `exitMeeting()` 更新状态为 `MeetingMemberStatusEnum.EXIT_MEETING`
4. **会议成员**: 在 `meeting_member` 表中追踪，包含状态/类型

### 7. 用户信息管理

**用户模块功能**:

- **个人信息修改**: 支持修改用户名、邮箱
- **密码修改**: 需要验证旧密码，新密码长度 8-20 字符
- **头像上传**: 限制图片格式，最大 5MB，自动删除旧头像
- Redis 缓存同步: 修改用户名后自动更新 Redis 中的 TokenUserInfo

## 开发指南

### 添加新接口

1. 在继承 `BaseController` 的控制器中添加方法
2. 如需鉴权，使用 `@GlobalInterceptor` 注解
3. 通过 `ResultUtils` 返回 `BaseResponse<T>`
4. Service 层使用 `@Service`，Mapper 使用 `@Resource` 注入
5. MyBatis XML 映射文件放在 `src/main/resources/mapper/`

### 处理 WebSocket 消息

1. 在 `MessageTypeEnum` 中添加新的消息类型
2. 在 `WebSocketHandler` 或对应的 `MsgHandler` 实现中处理
3. 使用 `ChannelContextUtils` 管理用户通道
4. 通过 `MsgHandler.sendMessage(SendMsgDTO)` 发送消息

### 文件上传操作

1. **上传文件**:
   ```java
   String fileUrl = cosComponent.uploadFile(file, "folder");
   ```
2. **删除文件**:
   ```java
   cosComponent.deleteFile(fileUrl);
   ```
3. **批量删除**:
   ```java
   cosComponent.deleteFiles(url1, url2, url3);
   ```
4. **保存文件记录**: 通过 `FileService.uploadFile()` 自动保存到数据库

### 数据库操作

- MyBatis-Plus 实体类在 `model.entity`，使用 `@TableName` 注解
- DTO 在 `model.dto`，VO 在 `model.vo`
- 使用 `@MapperScan("org.lzg.meeting.mapper")` (已配置)
- 分页功能通过 `PaginationInnerInterceptor` 启用

### Redis 操作

- 业务逻辑使用 `RedisComponent` (tokens、验证码、会议成员等)
- 底层操作使用 `RedisUtil`
- Token 过期时间: `UserConstant.TOKEN_EXPIRE_TIME` (天)

### 用户信息操作

- **获取当前用户**: `getTokenUserInfo()` (在 BaseController 中)
- **更新用户信息**: `UserService.updateUserInfo(userId, userUpdateDTO)`
- **修改密码**: `UserService.updatePassword(userId, userPasswordDTO)`
- **上传头像**: `UserService.uploadAvatar(userId, file)`

## 构建与运行

```powershell
# 构建
./mvnw clean package

# 运行 (需要 MySQL、Redis，RabbitMQ 可选)
./mvnw spring-boot:run

# 环境配置: 默认 (dev) 或生产环境 (prod)
# -Dspring.profiles.active=prod
```

**配置要求**: 运行前需在 `application.yml` 中更新数据库/Redis/RabbitMQ/COS 凭据。

## 关键文件参考

### 核心组件

- **认证**: `GlobalOperationAspect.java`, `GlobalInterceptor.java`
- **WebSocket**: `NettyWebsocketStarter.java`, `InitRun.java`, `WebSocketHandler.java`
- **消息处理**: `MsgHandler.java`, `MsgHandler4Redis.java`, `MsgHandler4Rabbitmq.java`
- **响应处理**: `BaseResponse.java`, `ResultUtils.java`, `GlobalExceptionHandler.java`
- **文件管理**: `CosConfig.java`, `CosComponent.java`, `FileService.java`, `FileController.java`
- **用户管理**: `UserController.java`, `UserService.java`, `UserUpdateDTO.java`, `UserPasswordDTO.java`
- **配置**: `application.yml`, `RabbitMQConfig.java`, `RedissonConfig.java`, `CosConfig.java`

### 数据库表

- `user`: 用户表
- `meeting`: 会议表
- `meeting_member`: 会议成员表
- `friendship`: 好友关系表
- `friend_request`: 好友请求表
- `file`: 文件表 (新增)

## 常用枚举

- `MessageTypeEnum`: WebSocket 消息类型
- `MeetingStatusEnum`: 会议状态 (进行中、已结束、已取消)
- `MeetingMemberStatusEnum`: 成员状态 (在会、退出、被踢、黑名单)
- `MemberTypeEnum`: 主持人 vs 普通成员
- `JoinTypeEnum`: 直接加入 vs 需要密码

## API 接口概览

### 用户模块 (`/api/user`)

- `GET /captcha`: 获取验证码
- `POST /login`: 用户登录
- `POST /register`: 用户注册
- `GET /current`: 获取当前用户信息
- `PUT /update`: 更新用户信息 (需登录)
- `PUT /password`: 修改密码 (需登录)
- `POST /avatar`: 上传头像 (需登录)

### 文件模块 (`/api/file`)

- `POST /upload`: 上传文件 (需登录)
- `DELETE /{fileId}`: 删除文件 (需登录)
- `GET /list`: 获取文件列表 (需登录)

### 会议模块 (`/api/meeting`)

- 创建会议、加入会议、退出会议等

### 好友模块 (`/api/friend`)

- 添加好友、删除好友、好友列表等

## 配置示例

### application.yml 关键配置

```yaml
# 服务器配置
server:
  port: 8081
  servlet:
    context-path: /api

# 数据源配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting
    username: root
    password: your-password

  # Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password

# 消息处理通道 (redis 或 rabbitmq)
message:
  handle:
    channel: redis

# 腾讯云 COS 配置
cos:
  client:
    host: https://your-bucket.cos.ap-beijing.myqcloud.com
    secretId: your-secret-id
    secretKey: your-secret-key
    region: ap-beijing
    bucket: your-bucket-name

# WebSocket 端口
ws:
  port: 8082
```

## 注意事项

### 文件上传

- 头像文件限制: 图片格式，最大 5MB
- 其他文件: 建议在配置中设置 `spring.servlet.multipart.max-file-size`
- 权限控制: 用户只能删除自己上传的文件

### 安全建议

- 生产环境使用 HTTPS
- COS 存储桶设置合理的访问权限
- 定期清理过期文件
- 密码使用 MD5+盐值 加密

### 性能优化

- 文件删除可改为异步操作
- 大文件上传使用分片上传
- 合理设置 Redis 缓存过期时间
- COS 配置 CDN 加速

## 文档参考

- **文件和用户模块文档**: `docs/file-and-user-module-guide.md`
- **聊天消息功能文档**: `docs/chat-message-feature-guide.md`
- **API 文档**: http://localhost:8081/api/doc.html (Knife4j)
- **Swagger UI**: http://localhost:8081/api/swagger-ui.html

## 聊天消息功能

### 核心特性

- **数据库分片**: 使用 Apache ShardingSphere 根据会议 ID 分表（默认 4 个分表，可配置）
- **多种消息类型**: 文本、图片（自动缩略图）、视频、文件、语音
- **消息范围**: 私聊（仅双方可见）、群聊（会议所有成员可见）
- **游标分页**: 基于消息 ID 的高性能分页查询
- **实时推送**: 通过 WebSocket 实时推送消息

### 分表策略

```
逻辑表: chat_message
物理表: chat_message_0, chat_message_1, chat_message_2, chat_message_3
分片键: meetingId
分片算法: meetingId % ${sharding.table.count}
```

### 关键接口

- `POST /api/chat/send/text`: 发送文本消息
- `POST /api/chat/send/image`: 发送图片消息（支持缩略图）
- `POST /api/chat/send/video`: 发送视频消息
- `POST /api/chat/send/file`: 发送文件消息
- `POST /api/chat/history`: 查询历史消息（游标分页）
- `DELETE /api/chat/recall/{messageId}`: 撤回消息

### 配置说明

在 `application.yml` 中配置分表数量：

```yaml
sharding:
  table:
    count: 4 # 聊天消息表分表数量，可根据业务量调整
```

### 使用示例

```java
// 发送群聊消息
ChatMessageSendDTO sendDTO = new ChatMessageSendDTO();
sendDTO.setMeetingId(1001L);
sendDTO.setMessageScope(2); // 群聊
sendDTO.setMessageType(1); // 文本
sendDTO.setContent("大家好！");
chatMessageService.sendMessage(sendDTO, userId);

// 查询历史消息（游标分页）
ChatMessageQueryDTO queryDTO = new ChatMessageQueryDTO();
queryDTO.setMeetingId(1001L);
queryDTO.setMessageScope(2);
queryDTO.setCursor(null); // 首次查询
queryDTO.setPageSize(20);
ChatMessagePageVO result = chatMessageService.queryHistoryMessages(queryDTO, userId);
```

详细文档请参考 `docs/chat-message-feature-guide.md`

# Meeting Application - AI Coding Agent Instructions

## Project Overview

This is a Spring Boot 3.5.6 + Java 21 meeting management system with real-time WebSocket communication using Netty. It supports meeting creation/joining, friend management, and offers flexible message handling via Redis or RabbitMQ.

## Architecture

### Core Technology Stack

- **Backend**: Spring Boot 3.5.6, Java 21, MyBatis-Plus
- **Database**: MySQL 8.0+
- **Caching/Messaging**: Redis, Redisson (distributed locks), RabbitMQ
- **WebSocket**: Netty 4.2.6 on separate port (8082)
- **API Docs**: Knife4j (Swagger)
- **Utilities**: Hutool, Lombok

### Application Structure

```
REST API (Port 8081, context-path: /api)
    ↓
Controllers → Services → Mappers → MySQL
    ↓                ↓
  Redis          RabbitMQ/Redis (message handling)
    ↓
WebSocket (Port 8082, Netty)
```

## Critical Patterns

### 1. Authentication & Authorization

**Token-Based Auth Pattern**:

- Token stored in Redis with key: `UserConstant.TOKEN + token` or `UserConstant.TOKEN + userId`
- Use `@GlobalInterceptor` annotation for auth checks:
  ```java
  @GlobalInterceptor(checkLogin = true)  // Requires login
  @GlobalInterceptor(checkAdmin = true)  // Requires admin role
  ```
- `GlobalOperationAspect` intercepts and validates tokens via `RedisComponent.getTokenUserInfo()`
- Controllers extending `BaseController` use `getTokenUserInfo()` to get current user

### 2. Response Handling

**Standardized Response Structure**:

- All endpoints return `BaseResponse<T>` with `code`, `message`, `data`
- Use `ResultUtils.success(data)` for successful responses (code=0)
- Use `ResultUtils.error(ErrorCode)` for errors
- `GlobalExceptionHandler` catches `BusinessException` and `RuntimeException` globally

### 3. WebSocket Communication (Netty)

**Dual-Channel Architecture**:

- **REST API**: Business logic (port 8081)
- **WebSocket**: Real-time communication (port 8082)
- `InitRun` (ApplicationRunner) starts Netty server on startup
- Handler chain: `TokenHandler` → `WebSocketServerProtocolHandler` → `WebSocketHandler`
- Message types defined in `MessageTypeEnum` (INIT, ADD_MEETING_ROOM, PEER, CHAT, etc.)

### 4. Flexible Message Handling

**Configuration-Based Strategy** (`application.yml: message.handle.channel`):

- `redis`: Uses Redis pub/sub (`MsgHandler4Redis`)
- `rabbitmq`: Uses RabbitMQ fanout exchange (`MsgHandler4Rabbitmq`)
- `@ConditionalOnProperty` ensures only one implementation loads
- Both implement `MsgHandler` interface for `sendMessage()` and `listener()`

### 5. Meeting Lifecycle

**Key Operations**:

1. **Quick Meeting**: `MeetingController.quickMeeting()` creates instant meeting, updates user's `TokenUserInfo.meetingId`
2. **Join Meeting**: `preJoinMeeting()` → `joinMeeting()` (validates, broadcasts to WebSocket)
3. **Exit**: `exitMeeting()` updates status to `MeetingMemberStatusEnum.EXIT_MEETING`
4. **Meeting Members**: Tracked in `meeting_member` table with status/type

## Development Guidelines

### Adding New Endpoints

1. Add method in controller extending `BaseController`
2. Use `@GlobalInterceptor` if auth required
3. Return `BaseResponse<T>` via `ResultUtils`
4. Update service layer with `@Service`, inject mapper with `@Resource`
5. MyBatis XML mappers in `src/main/resources/mapper/`

### Working with WebSocket Messages

1. Add new message type to `MessageTypeEnum`
2. Handle in `WebSocketHandler` or corresponding `MsgHandler` implementation
3. Use `ChannelContextUtils` to manage user channels
4. Send via `MsgHandler.sendMessage(SendMsgDTO)`

### Database Operations

- MyBatis-Plus entities in `model.entity` with `@TableName`
- DTOs in `model.dto`, VOs in `model.vo`
- Use `@MapperScan("org.lzg.meeting.mapper")` (already configured)
- Pagination enabled via `PaginationInnerInterceptor`

### Redis Operations

- Use `RedisComponent` for business logic (tokens, captchas, meeting members)
- Use `RedisUtil` for low-level operations
- Token expiration: `UserConstant.TOKEN_EXPIRE_TIME` (days)

## Build & Run

```powershell
# Build
./mvnw clean package

# Run (requires MySQL, Redis, optional RabbitMQ)
./mvnw spring-boot:run

# Profiles: default (dev) or prod
# -Dspring.profiles.active=prod
```

**Configuration**: Update `application.yml` with database/Redis/RabbitMQ credentials before running.

## Key Files Reference

- **Auth**: `GlobalOperationAspect.java`, `GlobalInterceptor.java`
- **WebSocket**: `NettyWebsocketStarter.java`, `InitRun.java`, `WebSocketHandler.java`
- **Message Handling**: `MsgHandler.java`, `MsgHandler4Redis.java`, `MsgHandler4Rabbitmq.java`
- **Response**: `BaseResponse.java`, `ResultUtils.java`, `GlobalExceptionHandler.java`
- **Config**: `application.yml`, `RabbitMQConfig.java`, `RedissonConfig.java`

## Common Enums

- `MessageTypeEnum`: WebSocket message types
- `MeetingStatusEnum`: Meeting states (in progress, ended, cancelled)
- `MeetingMemberStatusEnum`: Member states (in meeting, exit, kicked, blacklisted)
- `MemberTypeEnum`: Host vs normal member
- `JoinTypeEnum`: Direct join vs password required

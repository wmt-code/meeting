# Meeting Client - 会议管理桌面客户端

基于 **Vue3 + Element Plus + WebRTC + Electron + Pinia** 技术栈开发的跨平台会议管理桌面应用。

## 📋 项目特性

- ✅ **Vue3 Composition API** - 使用 `<script setup>` 语法糖
- ✅ **Element Plus** - 统一的 UI 组件库
- ✅ **Pinia** - 轻量级状态管理 + 持久化
- ✅ **WebRTC** - P2P 音视频通话
- ✅ **Electron** - 跨平台桌面应用
- ✅ **TypeScript** - 类型安全
- ✅ **WebSocket** - 实时消息推送
- ✅ **响应式布局** - 适配不同屏幕尺寸

## 🏗️ 项目结构

```
meeting-client/
├── electron/                 # Electron 主进程
│   ├── main.js              # 主进程入口
│   └── preload.js           # 预加载脚本
├── src/
│   ├── api/                 # API 接口封装
│   │   ├── user.ts          # 用户相关
│   │   ├── meeting.ts       # 会议相关
│   │   └── chat.ts          # 聊天相关
│   ├── components/          # 公共组件
│   │   └── ChatPanel.vue    # 聊天面板
│   ├── router/              # 路由配置
│   │   └── index.ts
│   ├── stores/              # Pinia 状态管理
│   │   ├── user.ts          # 用户状态
│   │   ├── meeting.ts       # 会议状态
│   │   └── chat.ts          # 聊天状态
│   ├── utils/               # 工具函数
│   │   ├── request.ts       # Axios 封装
│   │   ├── websocket.ts     # WebSocket 封装
│   │   └── webrtc.ts        # WebRTC 封装
│   ├── views/               # 页面组件
│   │   ├── Login.vue        # 登录页
│   │   ├── Register.vue     # 注册页
│   │   ├── Home.vue         # 主页
│   │   ├── Meeting.vue      # 会议室
│   │   └── Settings.vue     # 设置页
│   ├── types/               # TypeScript 类型定义
│   │   └── env.d.ts
│   ├── App.vue              # 根组件
│   └── main.ts              # 应用入口
├── .env                     # 开发环境变量
├── .env.production          # 生产环境变量
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 🚀 快速开始

### 环境要求

- **Node.js**: >= 18.x
- **npm** / **yarn** / **pnpm**: 任选其一

### 安装依赖

```bash
cd meeting-client
npm install
```

### 开发模式

#### 1. 仅 Web 端开发（推荐先调试界面）

```bash
npm run dev
```

浏览器访问: `http://localhost:5173`

#### 2. Electron 开发模式

```bash
npm run electron:dev
```

同时启动 Vite 开发服务器和 Electron 窗口。

### 构建打包

#### Web 端构建

```bash
npm run build
```

#### Electron 打包

```bash
npm run electron:build
```

生成的安装包位于 `dist-electron` 目录：
- **Windows**: `.exe` 安装程序 / 便携版
- **macOS**: `.dmg` 镜像 / `.zip` 压缩包
- **Linux**: `.AppImage` / `.deb` 安装包

## ⚙️ 配置说明

### 环境变量

#### `.env` (开发环境)

```env
# API 基础地址
VITE_API_BASE_URL=http://localhost:8081/api

# WebSocket 地址
VITE_WS_URL=ws://localhost:8082

# 应用标题
VITE_APP_TITLE=Meeting Client
```

#### `.env.production` (生产环境)

```env
VITE_API_BASE_URL=http://182.254.211.108:8081/api
VITE_WS_URL=ws://182.254.211.108:8082
VITE_APP_TITLE=Meeting Client
```

### 后端接口地址

确保后端服务已启动：
- **REST API**: `http://localhost:8081/api`
- **WebSocket**: `ws://localhost:8082`

## 📚 核心功能

### 1. 用户认证

- 用户登录/注册
- 图形验证码
- Token 认证
- 自动重连

### 2. 会议管理

- **快速会议**: 一键创建并加入
- **加入会议**: 通过会议号+密码加入
- **会议控制**: 音视频开关、屏幕共享、退出/结束

### 3. 音视频通话 (WebRTC)

- P2P 音视频通话
- 本地/远程视频流管理
- 麦克风/摄像头控制
- 屏幕共享（规划中）

### 4. 实时聊天

- 群聊/私聊
- 文本、图片、视频、文件消息
- 消息历史记录（游标分页）
- 消息撤回

### 5. WebSocket 消息

支持的消息类型：
- `INIT` - 连接初始化
- `ADD_MEETING_ROOM` - 加入会议
- `PEER` - WebRTC 信令
- `EXIT_MEETING_ROOM` - 退出会议
- `CHAT_MEDIA_MESSAGE` - 聊天消息
- `INVITE_MEMBER_MEETING` - 邀请成员

## 🔧 技术栈详解

### 前端框架

- **Vue 3.4+**: Composition API + `<script setup>`
- **Vue Router 4**: 客户端路由
- **Pinia 2**: 状态管理 + 持久化插件

### UI 组件

- **Element Plus 2.5+**: 完整 UI 组件库
- **@element-plus/icons-vue**: 图标库

### 桌面端

- **Electron 28+**: 跨平台桌面应用框架
- **IPC 通信**: 主进程与渲染进程通信
- **electron-builder**: 打包工具

### 工具链

- **Vite 5**: 快速构建工具
- **TypeScript**: 类型检查
- **ESLint + Prettier**: 代码规范
- **Sass**: CSS 预处理器

### 通信

- **Axios**: HTTP 请求库
- **WebSocket**: 实时双向通信
- **WebRTC**: 音视频通话

## 🎨 代码规范

### 自动格式化

```bash
# ESLint 检查并修复
npm run lint

# Prettier 格式化
npm run format
```

### 编码约定

1. **组件**: 使用 PascalCase 命名，如 `ChatPanel.vue`
2. **文件**: TypeScript 文件使用 `.ts` / `.vue` 后缀
3. **样式**: 使用 `scoped` + Sass，避免全局污染
4. **类型**: 导出的接口/类型使用 PascalCase
5. **常量**: 使用 UPPER_SNAKE_CASE

## 🛠️ 开发指南

### 添加新页面

1. 在 `src/views/` 创建 `.vue` 文件
2. 在 `src/router/index.ts` 添加路由
3. 设置 `meta.requiresAuth` 控制权限

### 添加新 API

1. 在 `src/api/` 创建或扩展模块
2. 使用 `get/post/put/del` 封装方法
3. 定义 TypeScript 接口

### 状态管理

```typescript
// 定义 Store
export const useMyStore = defineStore('my-store', () => {
  const state = ref(initialValue)
  
  function action() {
    // 业务逻辑
  }
  
  return { state, action }
}, {
  persist: true // 可选：持久化
})
```

### WebRTC 集成

```typescript
import { getWebRTCManager } from '@/utils/webrtc'

const webrtcManager = getWebRTCManager()

// 获取本地流
const localStream = await webrtcManager.getLocalStream()

// 创建对等连接
const pc = webrtcManager.createPeerConnection(remoteUserId)

// 创建 Offer
const offer = await pc.createOffer()
```

## 🐛 常见问题

### 1. WebSocket 连接失败

- 检查后端 WebSocket 服务是否启动（端口 8082）
- 确认 `.env` 中的 `VITE_WS_URL` 配置正确

### 2. 摄像头/麦克风权限

- Electron 默认允许媒体权限
- 浏览器需要用户手动授权（HTTPS 或 localhost）

### 3. 依赖安装失败

```bash
# 清理缓存重新安装
rm -rf node_modules package-lock.json
npm install
```

### 4. Electron 打包失败

- 检查网络连接（需下载 Electron 二进制文件）
- 配置镜像加速:
  ```bash
  npm config set electron_mirror https://npmmirror.com/mirrors/electron/
  ```

## 📦 依赖版本

### 核心依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| vue | ^3.4.15 | Vue 框架 |
| element-plus | ^2.5.4 | UI 组件库 |
| pinia | ^2.1.7 | 状态管理 |
| vue-router | ^4.2.5 | 路由 |
| axios | ^1.6.5 | HTTP 客户端 |

### 开发依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| electron | ^28.2.0 | 桌面框架 |
| vite | ^5.0.12 | 构建工具 |
| typescript | ^5.3.3 | 类型系统 |
| electron-builder | ^24.9.1 | 打包工具 |

## 📄 License

MIT License

## 👥 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📞 联系方式

- 作者: lzg
- 邮箱: [your-email@example.com]
- 项目地址: [https://github.com/wmt-code/meeting]

---

**注意**: 本项目为教学/演示用途，生产环境部署前请进行充分测试和安全加固。

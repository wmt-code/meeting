# Meeting Client - 快速启动指南

## 📦 安装步骤

### 1. 进入项目目录

```powershell
cd h:\JavaProject\meeting\meeting-client
```

### 2. 安装依赖

使用 npm（推荐）：

```powershell
npm install
```

或使用 yarn：

```powershell
yarn install
```

或使用 pnpm（更快）：

```powershell
pnpm install
```

**注意**: 首次安装可能需要 5-10 分钟，请耐心等待。

### 3. 配置后端地址

编辑 `.env` 文件，确认后端服务地址：

```env
VITE_API_BASE_URL=http://localhost:8081/api
VITE_WS_URL=ws://localhost:8082
```

如果后端部署在其他地址，请相应修改。

## 🚀 运行方式

### 方式一：Web 浏览器模式（推荐先用此方式调试）

```powershell
npm run dev
```

然后在浏览器访问: `http://localhost:5173`

**优点**:
- 热重载，修改代码立即生效
- 可使用浏览器开发者工具调试
- 启动快速

### 方式二：Electron 桌面应用模式

```powershell
npm run electron:dev
```

**特点**:
- 真实桌面应用体验
- 完整的 Electron API 支持
- 自动打开桌面窗口

## 🔧 开发调试

### 查看编译错误

当前 TypeScript 提示找不到模块是正常的，因为还未安装依赖。执行 `npm install` 后即可解决。

### 热重载

修改代码后自动刷新，无需手动重启。

### 开发者工具

- **Web 模式**: 浏览器 F12
- **Electron 模式**: 窗口已默认打开 DevTools

## 📋 测试流程

### 1. 确保后端服务运行

```powershell
# 在 meeting 目录下
cd h:\JavaProject\meeting
mvn spring-boot:run
```

后端启动后会监听：
- REST API: `http://localhost:8081/api`
- WebSocket: `ws://localhost:8082`

### 2. 启动前端

```powershell
cd h:\JavaProject\meeting\meeting-client
npm run dev
```

### 3. 测试功能

1. **注册账号**: 访问注册页面，填写信息
2. **登录系统**: 使用注册的账号登录
3. **快速会议**: 点击"快速会议"创建并加入
4. **加入会议**: 在另一个浏览器窗口登录其他账号，通过会议号加入
5. **音视频**: 测试麦克风、摄像头控制
6. **聊天**: 发送文本、图片消息

## 🔍 常见问题排查

### 问题 1: 依赖安装失败

```powershell
# 清理并重新安装
Remove-Item node_modules -Recurse -Force
Remove-Item package-lock.json -Force
npm install
```

### 问题 2: 端口被占用

```powershell
# 查看端口占用
netstat -ano | findstr "5173"

# 修改端口: 编辑 vite.config.ts
server: {
  port: 5174  # 改为其他端口
}
```

### 问题 3: WebSocket 连接失败

检查：
1. 后端 WebSocket 服务是否启动（端口 8082）
2. 防火墙是否阻止连接
3. `.env` 配置的地址是否正确

### 问题 4: 摄像头/麦克风无权限

**Chrome/Edge**: 点击地址栏左侧的锁图标，允许摄像头和麦克风权限

**Electron**: 默认已允许，无需额外设置

## 📦 打包发布

### 打包 Windows 应用

```powershell
npm run electron:build
```

生成文件位于 `dist-electron` 目录：
- `Meeting Client Setup.exe` - 安装程序
- `Meeting Client Portable.exe` - 便携版

### 仅构建 Web 静态文件

```powershell
npm run build
```

生成文件位于 `dist` 目录，可部署到任何静态服务器。

## 🎯 下一步

1. **自定义配置**: 修改 `.env` 适应你的环境
2. **扩展功能**: 在现有代码基础上添加新特性
3. **样式调整**: 修改 `.vue` 文件中的 `<style>` 部分
4. **添加页面**: 在 `src/views` 创建新页面并添加路由

## 💡 开发技巧

### 代码片段推荐

**Vue 组件模板**:

```vue
<template>
  <div class="my-component">
    {{ message }}
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const message = ref('Hello')
</script>

<style scoped lang="scss">
.my-component {
  padding: 20px;
}
</style>
```

**Pinia Store 模板**:

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMyStore = defineStore('my-store', () => {
  const data = ref<any>(null)
  
  function fetchData() {
    // 逻辑
  }
  
  return { data, fetchData }
})
```

### 推荐 VS Code 插件

- **Vue Language Features (Volar)** - Vue 3 支持
- **TypeScript Vue Plugin (Volar)** - TS 支持
- **ESLint** - 代码检查
- **Prettier** - 代码格式化
- **Element Plus Snippets** - Element Plus 代码片段

## 📞 获取帮助

遇到问题？

1. 查看 `README.md` 完整文档
2. 检查浏览器控制台错误信息
3. 查看后端日志输出
4. 提交 Issue 到 GitHub 仓库

---

祝开发愉快！ 🎉

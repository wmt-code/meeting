import { ElMessage } from 'element-plus'

/**
 * WebSocket 消息类型枚举
 */
export enum MessageType {
  INIT = 0, // 初始化连接
  ADD_MEETING_ROOM = 1, // 加入会议
  PEER = 2, // WebRTC 信令
  EXIT_MEETING_ROOM = 3, // 退出会议
  FINISH_MEETING = 4, // 结束会议
  CHAT_TEXT_MESSAGE = 5, // 文本消息
  CHAT_MEDIA_MESSAGE = 6, // 媒体消息
  CHAT_MEDIA_MESSAGE_UPDATE = 7, // 媒体消息更新
  USER_CONTACT_APPLY = 8, // 好友申请
  INVITE_MEMBER_MEETING = 9, // 邀请入会
  FORCE_OFF_LINE = 10, // 强制下线
  MEETING_USER_VIDEO_CHANGE = 11 // 用户视频状态变化
}

/**
 * WebSocket 连接状态
 */
export enum WebSocketState {
  CONNECTING = 'CONNECTING',
  OPEN = 'OPEN',
  CLOSING = 'CLOSING',
  CLOSED = 'CLOSED'
}

/**
 * WebSocket 管理类
 */
export class WebSocketManager {
  private ws: WebSocket | null = null
  private url: string
  private token: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000
  private heartbeatInterval: number | null = null
  private messageHandlers: Map<MessageType, Function[]> = new Map()
  private state: WebSocketState = WebSocketState.CLOSED

  constructor(url: string, token: string) {
    this.url = url
    this.token = token
  }

  /**
   * 连接 WebSocket
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.state = WebSocketState.CONNECTING
        this.ws = new WebSocket(this.url)

        this.ws.onopen = () => {
          console.log('WebSocket 连接成功')
          this.state = WebSocketState.OPEN
          this.reconnectAttempts = 0

          // 发送初始化消息
          this.sendMessage({
            msgType: MessageType.INIT,
            token: this.token
          })

          // 启动心跳
          this.startHeartbeat()

          resolve()
        }

        this.ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data)
            this.handleMessage(data)
          } catch (error) {
            console.error('消息解析失败:', error)
          }
        }

        this.ws.onerror = (error) => {
          console.error('WebSocket 错误:', error)
          this.state = WebSocketState.CLOSED
          reject(error)
        }

        this.ws.onclose = () => {
          console.log('WebSocket 连接关闭')
          this.state = WebSocketState.CLOSED
          this.stopHeartbeat()

          // 自动重连
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++
            console.log(`尝试重连 (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`)
            setTimeout(() => {
              this.connect()
            }, this.reconnectDelay)
          } else {
            ElMessage.error('WebSocket 连接失败，请刷新页面重试')
          }
        }
      } catch (error) {
        console.error('WebSocket 创建失败:', error)
        reject(error)
      }
    })
  }

  /**
   * 发送消息
   */
  sendMessage(message: any): void {
    if (this.ws && this.state === WebSocketState.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket 未连接，无法发送消息')
    }
  }

  /**
   * 注册消息处理器
   */
  on(type: MessageType, handler: Function): void {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, [])
    }
    this.messageHandlers.get(type)?.push(handler)
  }

  /**
   * 取消消息处理器
   */
  off(type: MessageType, handler: Function): void {
    const handlers = this.messageHandlers.get(type)
    if (handlers) {
      const index = handlers.indexOf(handler)
      if (index > -1) {
        handlers.splice(index, 1)
      }
    }
  }

  /**
   * 处理收到的消息
   */
  private handleMessage(data: any): void {
    const type = data.msgType as MessageType
    const handlers = this.messageHandlers.get(type)

    if (handlers && handlers.length > 0) {
      handlers.forEach((handler) => {
        try {
          handler(data)
        } catch (error) {
          console.error('消息处理器执行错误:', error)
        }
      })
    }
  }

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.heartbeatInterval = window.setInterval(() => {
      if (this.state === WebSocketState.OPEN) {
        this.sendMessage({ type: 'ping' })
      }
    }, 30000) // 30秒一次心跳
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval)
      this.heartbeatInterval = null
    }
  }

  /**
   * 关闭连接
   */
  close(): void {
    this.state = WebSocketState.CLOSING
    this.stopHeartbeat()
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.messageHandlers.clear()
  }

  /**
   * 获取连接状态
   */
  getState(): WebSocketState {
    return this.state
  }
}

// 单例实例
let wsInstance: WebSocketManager | null = null

/**
 * 获取 WebSocket 实例
 */
export function getWebSocket(): WebSocketManager | null {
  return wsInstance
}

/**
 * 初始化 WebSocket
 */
export function initWebSocket(token: string): Promise<void> {
  if (wsInstance) {
    wsInstance.close()
  }
  wsInstance = new WebSocketManager(import.meta.env.VITE_WS_URL, token)
  return wsInstance.connect()
}

/**
 * 关闭 WebSocket
 */
export function closeWebSocket(): void {
  if (wsInstance) {
    wsInstance.close()
    wsInstance = null
  }
}

import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 聊天消息类型枚举
 */
export enum ChatMessageType {
  TEXT = 1,
  IMAGE = 2,
  VIDEO = 3,
  FILE = 4,
  AUDIO = 5
}

/**
 * 聊天消息范围
 */
export enum ChatMessageScope {
  PRIVATE = 1, // 私聊
  GROUP = 2 // 群聊
}

/**
 * 聊天消息
 */
export interface ChatMessage {
  id?: number
  meetingId: number
  senderId: number
  senderName: string
  receiverId?: number
  receiverName?: string
  messageType: ChatMessageType
  messageScope: ChatMessageScope
  content?: string
  fileUrl?: string
  fileName?: string
  fileSize?: number
  thumbnailUrl?: string
  createTime: string
  status: number
}

/**
 * 聊天状态管理
 */
export const useChatStore = defineStore('chat', () => {
  // 消息列表
  const messages = ref<ChatMessage[]>([])

  // 未读消息数
  const unreadCount = ref<number>(0)

  // 当前私聊对象
  const currentChatUser = ref<{ userId: number; userName: string } | null>(null)

  /**
   * 添加消息
   */
  function addMessage(message: ChatMessage) {
    messages.value.push(message)

    // 如果不是自己发的消息，增加未读数
    // 这里需要根据实际业务逻辑判断
  }

  /**
   * 批量添加消息（历史记录）
   */
  function addMessages(newMessages: ChatMessage[]) {
    messages.value.push(...newMessages)
  }

  /**
   * 清空消息
   */
  function clearMessages() {
    messages.value = []
    unreadCount.value = 0
  }

  /**
   * 标记已读
   */
  function markAsRead() {
    unreadCount.value = 0
  }

  /**
   * 设置当前聊天对象
   */
  function setCurrentChatUser(user: { userId: number; userName: string } | null) {
    currentChatUser.value = user
  }

  /**
   * 撤回消息
   */
  function recallMessage(messageId: number) {
    const message = messages.value.find((m) => m.id === messageId)
    if (message) {
      message.status = 2 // 已撤回
    }
  }

  return {
    messages,
    unreadCount,
    currentChatUser,
    addMessage,
    addMessages,
    clearMessages,
    markAsRead,
    setCurrentChatUser,
    recallMessage
  }
})

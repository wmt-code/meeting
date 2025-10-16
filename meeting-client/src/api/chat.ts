import { post, upload } from '@/utils/request'
import type { ChatMessage } from '@/stores/chat'

/**
 * 发送文本消息参数
 */
export interface SendTextMessageParams {
  meetingId: number
  messageScope: number
  receiverId?: number
  content: string
}

/**
 * 查询历史消息参数
 */
export interface QueryHistoryParams {
  meetingId: number
  messageScope: number
  otherUserId?: number
  cursor?: number
  pageSize?: number
}

/**
 * 聊天 API
 */
export const chatApi = {
  /**
   * 发送文本消息
   */
  sendTextMessage: (data: SendTextMessageParams) => post<ChatMessage>('/chat/send/text', data),

  /**
   * 发送图片消息
   */
  sendImageMessage: (formData: FormData) => upload<ChatMessage>('/chat/send/image', formData),

  /**
   * 发送视频消息
   */
  sendVideoMessage: (formData: FormData) => upload<ChatMessage>('/chat/send/video', formData),

  /**
   * 发送文件消息
   */
  sendFileMessage: (formData: FormData) => upload<ChatMessage>('/chat/send/file', formData),

  /**
   * 查询历史消息
   */
  queryHistory: (data: QueryHistoryParams) => post('/chat/history', data),

  /**
   * 撤回消息
   */
  recallMessage: (messageId: number) => post(`/chat/recall/${messageId}`)
}

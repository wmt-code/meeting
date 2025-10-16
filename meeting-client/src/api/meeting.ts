import { get, post } from '@/utils/request'
import type { MeetingInfo } from '@/stores/meeting'

/**
 * 快速会议参数
 */
export interface QuickMeetingParams {
  meetingName: string
}

/**
 * 加入会议参数
 */
export interface JoinMeetingParams {
  meetingCode: string
  password?: string
}

/**
 * 会议 API
 */
export const meetingApi = {
  /**
   * 创建快速会议
   */
  quickMeeting: (data: QuickMeetingParams) => post<MeetingInfo>('/meeting/quickMeeting', data),

  /**
   * 预加入会议
   */
  preJoinMeeting: (params: { meetingCode: string }) => get('/meeting/preJoinMeeting', params),

  /**
   * 加入会议
   */
  joinMeeting: (data: JoinMeetingParams) => post<MeetingInfo>('/meeting/joinMeeting', data),

  /**
   * 退出会议
   */
  exitMeeting: (params: { meetingId: number }) => post('/meeting/exitMeeting', params),

  /**
   * 结束会议
   */
  finishMeeting: (params: { meetingId: number }) => post('/meeting/finishMeeting', params),

  /**
   * 获取会议成员列表
   */
  getMeetingMembers: (params: { meetingId: number }) => get('/meeting/members', params)
}

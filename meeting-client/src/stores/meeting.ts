import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 会议成员信息
 */
export interface MeetingMember {
  userId: number
  userName: string
  avatar?: string
  isMuted: boolean
  isVideoOn: boolean
  isHost: boolean
  isScreenSharing: boolean
}

/**
 * 会议信息
 */
export interface MeetingInfo {
  id: number
  meetingName: string
  meetingCode: string
  status: number
  hostId: number
  startTime?: string
  members: MeetingMember[]
}

/**
 * 会议状态管理
 */
export const useMeetingStore = defineStore('meeting', () => {
  // 当前会议信息
  const currentMeeting = ref<MeetingInfo | null>(null)

  // 本地媒体状态
  const isMuted = ref<boolean>(false)
  const isVideoOn = ref<boolean>(true)
  const isScreenSharing = ref<boolean>(false)

  /**
   * 设置会议信息
   */
  function setMeetingInfo(info: MeetingInfo) {
    currentMeeting.value = info
  }

  /**
   * 清除会议信息
   */
  function clearMeeting() {
    currentMeeting.value = null
    isMuted.value = false
    isVideoOn.value = true
    isScreenSharing.value = false
  }

  /**
   * 更新成员列表
   */
  function updateMembers(members: MeetingMember[]) {
    if (currentMeeting.value) {
      currentMeeting.value.members = members
    }
  }

  /**
   * 添加成员
   */
  function addMember(member: MeetingMember) {
    if (currentMeeting.value) {
      const exists = currentMeeting.value.members.find((m) => m.userId === member.userId)
      if (!exists) {
        currentMeeting.value.members.push(member)
      }
    }
  }

  /**
   * 移除成员
   */
  function removeMember(userId: number) {
    if (currentMeeting.value) {
      currentMeeting.value.members = currentMeeting.value.members.filter((m) => m.userId !== userId)
    }
  }

  /**
   * 更新成员状态
   */
  function updateMemberStatus(userId: number, status: Partial<MeetingMember>) {
    if (currentMeeting.value) {
      const member = currentMeeting.value.members.find((m) => m.userId === userId)
      if (member) {
        Object.assign(member, status)
      }
    }
  }

  /**
   * 切换静音
   */
  function toggleMute() {
    isMuted.value = !isMuted.value
  }

  /**
   * 切换视频
   */
  function toggleVideo() {
    isVideoOn.value = !isVideoOn.value
  }

  /**
   * 切换屏幕共享
   */
  function toggleScreenShare() {
    isScreenSharing.value = !isScreenSharing.value
  }

  return {
    currentMeeting,
    isMuted,
    isVideoOn,
    isScreenSharing,
    setMeetingInfo,
    clearMeeting,
    updateMembers,
    addMember,
    removeMember,
    updateMemberStatus,
    toggleMute,
    toggleVideo,
    toggleScreenShare
  }
})

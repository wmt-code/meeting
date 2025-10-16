<template>
  <div class="meeting-container">
    <!-- 视频区域 -->
    <div class="video-area">
      <div class="main-video">
        <video ref="localVideoRef" autoplay playsinline muted></video>
        <div class="video-info">
          <span>{{ userStore.userInfo?.userName }} (我)</span>
        </div>
      </div>

      <!-- 远程视频列表 -->
      <div class="remote-videos">
        <div v-for="member in remoteMembers" :key="member.userId" class="remote-video-item">
          <video :ref="(el) => setRemoteVideo(member.userId, el)" autoplay playsinline></video>
          <div class="video-info">
            <span>{{ member.userName }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 控制栏 -->
    <div class="control-bar">
      <div class="control-buttons">
        <el-button :type="meetingStore.isMuted ? 'danger' : 'primary'" circle @click="toggleMute">
          <el-icon><Microphone v-if="!meetingStore.isMuted" /><MicrophoneSlash v-else /></el-icon>
        </el-button>

        <el-button :type="meetingStore.isVideoOn ? 'primary' : 'danger'" circle @click="toggleVideo">
          <el-icon><VideoCamera v-if="meetingStore.isVideoOn" /><VideoCameraFilled v-else /></el-icon>
        </el-button>

        <el-button type="warning" circle @click="toggleChat">
          <el-icon><ChatDotRound /></el-icon>
        </el-button>

        <el-button type="danger" circle @click="handleExitMeeting">
          <el-icon><PhoneFilled /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 聊天侧边栏 -->
    <el-drawer v-model="showChat" title="聊天" direction="rtl" :size="360">
      <ChatPanel />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMeetingStore } from '@/stores/meeting'
import { getWebRTCManager } from '@/utils/webrtc'
import { getWebSocket, MessageType } from '@/utils/websocket'
import { meetingApi } from '@/api/meeting'
import ChatPanel from '@/components/ChatPanel.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const meetingStore = useMeetingStore()

const localVideoRef = ref<HTMLVideoElement>()
const remoteVideoElements = new Map<number, HTMLVideoElement>()
const showChat = ref(false)

const remoteMembers = computed(() => {
  return meetingStore.currentMeeting?.members.filter(m => m.userId !== userStore.userInfo?.id) || []
})

let webrtcManager: any = null

/**
 * 设置远程视频元素引用
 */
const setRemoteVideo = (userId: number, el: any) => {
  if (el) {
    remoteVideoElements.set(userId, el)
  }
}

/**
 * 初始化媒体流
 */
const initMediaStream = async () => {
  try {
    webrtcManager = getWebRTCManager()
    const stream = await webrtcManager.getLocalStream()
    
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream
    }
  } catch (error) {
    ElMessage.error('无法获取摄像头/麦克风权限')
    console.error(error)
  }
}

/**
 * 切换静音
 */
const toggleMute = () => {
  meetingStore.toggleMute()
  webrtcManager?.toggleAudio(!meetingStore.isMuted)
}

/**
 * 切换视频
 */
const toggleVideo = () => {
  meetingStore.toggleVideo()
  webrtcManager?.toggleVideo(meetingStore.isVideoOn)
}

/**
 * 切换聊天
 */
const toggleChat = () => {
  showChat.value = !showChat.value
}

/**
 * 退出会议
 */
const handleExitMeeting = async () => {
  try {
    await ElMessageBox.confirm('确定要退出会议吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const meetingId = meetingStore.currentMeeting?.id
    if (meetingId) {
      await meetingApi.exitMeeting({ meetingId })
    }

    webrtcManager?.destroy()
    meetingStore.clearMeeting()
    router.push('/home')
  } catch (error) {
    // 用户取消
  }
}

onMounted(async () => {
  await initMediaStream()
  
  // 监听 WebSocket 消息
  const ws = getWebSocket()
  ws?.on(MessageType.ADD_MEETING_ROOM, (data: any) => {
    console.log('新成员加入:', data)
    // 处理新成员加入逻辑
  })
})

onUnmounted(() => {
  webrtcManager?.destroy()
})
</script>

<style scoped lang="scss">
.meeting-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1a1a1a;
}

.video-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 16px;
}

.main-video {
  flex: 1;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;

  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-info {
    position: absolute;
    bottom: 12px;
    left: 12px;
    padding: 6px 12px;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    border-radius: 4px;
    font-size: 14px;
  }
}

.remote-videos {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.remote-video-item {
  width: 200px;
  height: 150px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;

  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-info {
    position: absolute;
    bottom: 8px;
    left: 8px;
    padding: 4px 8px;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    border-radius: 4px;
    font-size: 12px;
  }
}

.control-bar {
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #2a2a2a;
}

.control-buttons {
  display: flex;
  gap: 16px;

  :deep(.el-button) {
    width: 48px;
    height: 48px;
  }
}
</style>

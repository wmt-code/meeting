<template>
  <div class="chat-panel">
    <div class="chat-messages" ref="messagesRef">
      <div v-for="msg in chatStore.messages" :key="msg.id" class="message-item" :class="{ 'is-mine': msg.senderId === userStore.userInfo?.id }">
        <div class="message-content">
          <div class="message-header">
            <span class="sender-name">{{ msg.senderName }}</span>
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
          </div>
          <div class="message-body">
            <template v-if="msg.messageType === ChatMessageType.TEXT">
              {{ msg.content }}
            </template>
            <template v-else-if="msg.messageType === ChatMessageType.IMAGE">
              <img :src="msg.fileUrl" alt="图片" class="message-image" @click="previewImage(msg.fileUrl)" />
            </template>
            <template v-else-if="msg.messageType === ChatMessageType.FILE">
              <div class="message-file">
                <el-icon><Document /></el-icon>
                <span>{{ msg.fileName }}</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="messageText"
        type="textarea"
        :rows="3"
        placeholder="输入消息..."
        @keydown.ctrl.enter="sendMessage"
      />
      <div class="input-actions">
        <el-button type="primary" size="small" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useChatStore, ChatMessageType } from '@/stores/chat'
import { chatApi } from '@/api/chat'
import { useMeetingStore } from '@/stores/meeting'

const userStore = useUserStore()
const chatStore = useChatStore()
const meetingStore = useMeetingStore()

const messageText = ref('')
const messagesRef = ref<HTMLElement>()

const formatTime = (time: string) => {
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const sendMessage = async () => {
  if (!messageText.value.trim()) return

  const meetingId = meetingStore.currentMeeting?.id
  if (!meetingId) {
    ElMessage.warning('未在会议中')
    return
  }

  try {
    await chatApi.sendTextMessage({
      meetingId,
      messageScope: 2, // 群聊
      content: messageText.value
    })
    messageText.value = ''
    scrollToBottom()
  } catch (error: any) {
    ElMessage.error(error.message || '发送失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

const previewImage = (url?: string) => {
  if (url) {
    window.open(url, '_blank')
  }
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped lang="scss">
.chat-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message-item {
  margin-bottom: 16px;

  &.is-mine {
    .message-content {
      margin-left: auto;
      background: #409eff;
      color: white;
    }
  }
}

.message-content {
  max-width: 70%;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  opacity: 0.8;
}

.message-body {
  word-break: break-word;
}

.message-image {
  max-width: 200px;
  border-radius: 4px;
  cursor: pointer;
}

.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #eee;
}

.input-actions {
  margin-top: 8px;
  text-align: right;
}
</style>

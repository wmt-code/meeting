<template>
  <div class="home-container">
    <!-- 顶部导航 -->
    <div class="home-header">
      <div class="header-left">
        <h2>会议系统</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :src="userStore.userInfo?.avatar || ''" icon="User" />
            <span class="user-name">{{ userStore.userInfo?.userName }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                设置
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="home-content">
      <div class="meeting-actions">
        <el-card class="action-card" shadow="hover" @click="handleQuickMeeting">
          <div class="card-icon">
            <el-icon :size="48" color="#409EFF"><VideoCamera /></el-icon>
          </div>
          <div class="card-title">快速会议</div>
          <div class="card-desc">立即发起一个会议</div>
        </el-card>

        <el-card class="action-card" shadow="hover" @click="showJoinDialog = true">
          <div class="card-icon">
            <el-icon :size="48" color="#67C23A"><Connection /></el-icon>
          </div>
          <div class="card-title">加入会议</div>
          <div class="card-desc">通过会议号加入</div>
        </el-card>
      </div>
    </div>

    <!-- 加入会议对话框 -->
    <el-dialog v-model="showJoinDialog" title="加入会议" width="400px">
      <el-form :model="joinForm" label-width="80px">
        <el-form-item label="会议号">
          <el-input v-model="joinForm.meetingCode" placeholder="请输入会议号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="joinForm.password" placeholder="如有密码请输入" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showJoinDialog = false">取消</el-button>
        <el-button type="primary" :loading="joining" @click="handleJoinMeeting">加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMeetingStore } from '@/stores/meeting'
import { meetingApi } from '@/api/meeting'

const router = useRouter()
const userStore = useUserStore()
const meetingStore = useMeetingStore()

const showJoinDialog = ref(false)
const joining = ref(false)

const joinForm = reactive({
  meetingCode: '',
  password: ''
})

/**
 * 快速会议
 */
const handleQuickMeeting = async () => {
  try {
    const meeting = await meetingApi.quickMeeting({
      meetingName: `${userStore.userInfo?.userName}的会议`
    })
    meetingStore.setMeetingInfo(meeting)
    router.push(`/meeting/${meeting.id}`)
  } catch (error: any) {
    ElMessage.error(error.message || '创建会议失败')
  }
}

/**
 * 加入会议
 */
const handleJoinMeeting = async () => {
  if (!joinForm.meetingCode) {
    ElMessage.warning('请输入会议号')
    return
  }

  joining.value = true
  try {
    const meeting = await meetingApi.joinMeeting(joinForm)
    meetingStore.setMeetingInfo(meeting)
    showJoinDialog.value = false
    router.push(`/meeting/${meeting.id}`)
  } catch (error: any) {
    ElMessage.error(error.message || '加入会议失败')
  } finally {
    joining.value = false
  }
}

/**
 * 下拉菜单命令
 */
const handleCommand = (command: string) => {
  switch (command) {
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style scoped lang="scss">
.home-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.home-header {
  height: 60px;
  padding: 0 24px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .header-left {
    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #333;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 8px;
      transition: background 0.3s;

      &:hover {
        background: #f5f7fa;
      }

      .user-name {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

.home-content {
  flex: 1;
  padding: 48px;
  overflow-y: auto;
}

.meeting-actions {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.action-card {
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-4px);
  }

  .card-icon {
    margin-bottom: 16px;
  }

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
  }

  .card-desc {
    font-size: 14px;
    color: #999;
  }
}
</style>

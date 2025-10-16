import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { initWebSocket, closeWebSocket } from '@/utils/websocket'

/**
 * 用户信息接口
 */
export interface UserInfo {
  id: number
  userName: string
  email: string
  avatar?: string
  meetingId?: number
}

/**
 * 用户状态管理
 */
export const useUserStore = defineStore(
  'user',
  () => {
    const router = useRouter()

    // 状态
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)
    const isLoggedIn = ref<boolean>(false)

    /**
     * 设置 token
     */
    function setToken(newToken: string) {
      token.value = newToken
      isLoggedIn.value = !!newToken
    }

    /**
     * 设置用户信息
     */
    function setUserInfo(info: UserInfo) {
      userInfo.value = info
    }

    /**
     * 登录
     */
    async function login(newToken: string, info: UserInfo) {
      setToken(newToken)
      setUserInfo(info)

      // 初始化 WebSocket
      try {
        await initWebSocket(newToken)
      } catch (error) {
        console.error('WebSocket 连接失败:', error)
      }
    }

    /**
     * 登出
     */
    function logout() {
      token.value = ''
      userInfo.value = null
      isLoggedIn.value = false

      // 关闭 WebSocket
      closeWebSocket()

      // 跳转到登录页
      router.push('/login')
    }

    /**
     * 更新用户信息
     */
    function updateUserInfo(info: Partial<UserInfo>) {
      if (userInfo.value) {
        userInfo.value = { ...userInfo.value, ...info }
      }
    }

    return {
      token,
      userInfo,
      isLoggedIn,
      setToken,
      setUserInfo,
      login,
      logout,
      updateUserInfo
    }
  },
  {
    persist: true // 持久化存储
  }
)

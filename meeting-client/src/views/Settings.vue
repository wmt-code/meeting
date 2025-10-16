<template>
  <div class="settings-container">
    <div class="settings-header">
      <el-button icon="ArrowLeft" @click="goBack">返回</el-button>
      <h2>设置</h2>
    </div>

    <div class="settings-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="profile">
          <el-form :model="userForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="用户名">
              <el-input v-model="userForm.userName" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="旧密码">
              <el-input v-model="passwordForm.oldPassword" type="password" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.newPassword" type="password" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirmPassword" type="password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('profile')

const userForm = reactive({
  userName: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const goBack = () => {
  router.back()
}

const handleSave = async () => {
  try {
    await userApi.updateUserInfo(userForm)
    userStore.updateUserInfo(userForm)
    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handleChangePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }

  try {
    await userApi.updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  }
}

onMounted(() => {
  if (userStore.userInfo) {
    userForm.userName = userStore.userInfo.userName
    userForm.email = userStore.userInfo.email
  }
})
</script>

<style scoped lang="scss">
.settings-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.settings-header {
  height: 60px;
  padding: 0 24px;
  background: white;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }
}

.settings-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;

  :deep(.el-tabs) {
    background: white;
    padding: 24px;
    border-radius: 8px;
  }
}
</style>

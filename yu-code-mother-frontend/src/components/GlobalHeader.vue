<!-- Header.vue -->
<template>
  <a-layout-header class="header">
    <a-row :wrap="false" class="header-row">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="200px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">鱼皮应用生成</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <a-button type="primary">登录</a-button>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { h, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const menuItems = ref([
  {
    key: '/',
    label: '首页',
    title: '首页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于我们',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.codefather.cn', target: '_blank' }, '编程导航'),
    title: '编程导航',
  },
])

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 动态更新安全区变量，确保 Header 适配刘海屏
const updateSafeArea = () => {
  // 获取安全区数值并设置为 CSS 变量
  const style = document.documentElement.style
  style.setProperty('--safe-area-top', 'env(safe-area-inset-top, 0px)')
  style.setProperty('--safe-area-right', 'env(safe-area-inset-right, 0px)')
  style.setProperty('--safe-area-bottom', 'env(safe-area-inset-bottom, 0px)')
  style.setProperty('--safe-area-left', 'env(safe-area-inset-left, 0px)')
}

onMounted(() => {
  updateSafeArea()
  // 监听窗口变化，重新计算
  window.addEventListener('resize', updateSafeArea)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateSafeArea)
})
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0;
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  /* 适配安全区顶部 */
  padding-top: var(--safe-area-top, env(safe-area-inset-top, 0px));
  box-sizing: border-box;
}

.header-row {
  /* 适配安全区左右 */
  padding-left: var(--safe-area-left, env(safe-area-inset-left, 0px));
  padding-right: var(--safe-area-right, env(safe-area-inset-right, 0px));
  background: #fff;
  height: 64px;
  display: flex;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
  object-fit: contain;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
  white-space: nowrap;
}

.ant-menu-horizontal {
  border-bottom: none !important;
  line-height: 64px;
}

.user-login-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 64px;
}

/* 响应式：小屏幕时隐藏标题文字 */
@media (max-width: 768px) {
  .site-title {
    display: none;
  }

  .header-left {
    gap: 8px;
  }

  .logo {
    height: 40px;
    width: 40px;
  }
}
</style>

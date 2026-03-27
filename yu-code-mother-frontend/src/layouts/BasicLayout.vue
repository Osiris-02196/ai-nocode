<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />

    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </a-layout-content>

    <!-- 底部版权信息 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { onMounted, onUnmounted } from 'vue'

// 动态更新安全区变量，确保内容区域适配
const updateSafeArea = () => {
  const style = document.documentElement.style
  style.setProperty('--safe-area-top', 'env(safe-area-inset-top, 0px)')
  style.setProperty('--safe-area-right', 'env(safe-area-inset-right, 0px)')
  style.setProperty('--safe-area-bottom', 'env(safe-area-inset-bottom, 0px)')
  style.setProperty('--safe-area-left', 'env(safe-area-inset-left, 0px)')
}

onMounted(() => {
  updateSafeArea()
  window.addEventListener('resize', updateSafeArea)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateSafeArea)
})
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  width: 100%;
  background: #f0f2f5;
  /* 确保布局容器完全填充 */
  display: flex;
  flex-direction: column;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  /* Header 使用 sticky 仍在布局流中，无需额外 margin-top */
  margin-top: 0;
  margin-bottom: 0; /* Footer 通过自身定位处理 */
  padding: 0;
  background: #f0f2f5;
  /* 适配安全区左右 */
  padding-left: var(--safe-area-left, env(safe-area-inset-left, 0px));
  padding-right: var(--safe-area-right, env(safe-area-inset-right, 0px));
  box-sizing: border-box;
}

/* 内容包裹器 - 限制最大宽度并居中 */
.content-wrapper {
  width: 100%;
  max-width: none;
  margin: 0;
  padding: 24px;
  background: white;
  border-radius: 0;
  box-shadow: none;
  min-height: calc(100vh - 64px - 64px); /* 视口高度 - Header - Footer */
  box-sizing: border-box;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .main-content {
    margin-top: 0;
  }

  .content-wrapper {
    padding: 16px;
    border-radius: 0;
    min-height: calc(100vh - 60px - 60px);
  }
}

/* 针对 iPad 等中等屏幕优化 */
@media (min-width: 769px) and (max-width: 1024px) {
  .content-wrapper {
    max-width: 90%;
    padding: 20px;
  }
}

/* 确保在固定 Footer 的情况下内容区域不会重叠 */
:deep(.ant-layout) {
  background: none;
}
</style>

<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <TopNavbar @menu-click="handleMenuClick" />
    
    <div class="layout-content">
      <!-- 左侧边栏 -->
      <Sidebar 
        :active-menu="activeMenu" 
        :collapsed="sidebarCollapsed"
        @menu-click="handleSidebarClick"
        @toggle-collapse="toggleSidebar"
      />
      
      <!-- 主内容区域 -->
      <div class="main-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import TopNavbar from './TopNavbar.vue'
import Sidebar from './Sidebar.vue'

const router = useRouter()
const sidebarCollapsed = ref(false)
const activeMenu = ref('swagger')

const handleMenuClick = (menu: string) => {
  activeMenu.value = menu
  // 根据顶部菜单切换侧边栏内容
  if (menu === 'swagger') {
    router.push('/swagger')
  } else if (menu === 'json') {
    router.push('/json')
  }
}

const handleSidebarClick = (route: string) => {
  router.push(route)
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
  transition: margin-left 0.3s ease;
  overflow-y: auto;
}

.sidebar-collapsed {
  margin-left: 0;
}
</style>

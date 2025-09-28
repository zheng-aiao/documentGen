<template>
  <div class="top-navbar">
    <div class="navbar-brand">
      <h2>文档生成工具</h2>
    </div>

    <div class="navbar-menu">
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        @select="handleMenuSelect"
        class="navbar-menu-list"
        :collapse="false"
        :ellipsis="false"
      >

        <el-menu-item index="swagger">
          <el-icon><Document /></el-icon>
          <span>Swagger文档生成</span>
        </el-menu-item>
<!--        <el-menu-item index="json">-->
<!--          <el-icon><Edit /></el-icon>-->
<!--          <span>JSON格式化</span>-->
<!--        </el-menu-item>-->
<!--        <el-menu-item index="tools">-->
<!--          <el-icon><Tools /></el-icon>-->
<!--          <span>其他工具</span>-->
<!--        </el-menu-item>-->
      </el-menu>
    </div>

    <div class="navbar-actions">
      <el-button type="text" @click="toggleTheme">
        <el-icon><Moon v-if="isDark" /><Sunny v-else /></el-icon>
      </el-button>
      <el-dropdown>
        <el-button type="text">
          <el-icon><User /></el-icon>
          <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>个人设置</el-dropdown-item>
            <el-dropdown-item>帮助文档</el-dropdown-item>
            <el-dropdown-item divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  Document,
  Moon,
  Sunny,
  User,
  ArrowDown
} from '@element-plus/icons-vue'

const emit = defineEmits<{
  menuClick: [menu: string]
}>()

const activeMenu = ref('swagger')
const isDark = ref(false)

const handleMenuSelect = (menu: string) => {
  activeMenu.value = menu
  emit('menuClick', menu)
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  // 这里可以添加主题切换逻辑
}
</script>

<style scoped>
.top-navbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.navbar-brand h2 {
  margin: 0;
  color: #409eff;
  font-size: 20px;
  font-weight: 600;
}

.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 200px; /* 确保有足够空间显示菜单项 */
}

.navbar-menu-list {
  border-bottom: none;
  /* 强制显示所有菜单项，不折叠到更多按钮 */
  overflow: visible !important;
}

/* 隐藏更多按钮 */
.navbar-menu-list .el-menu--horizontal .el-menu-item:last-child[style*="display: none"] {
  display: block !important;
}

/* 确保菜单项始终显示 */
.navbar-menu-list .el-menu--horizontal .el-menu-item {
  display: inline-block !important;
}

/* 强制显示菜单项，防止被隐藏 */
.navbar-menu-list .el-menu--horizontal {
  overflow: visible !important;
}

.navbar-menu-list .el-menu--horizontal .el-menu-item {
  visibility: visible !important;
  opacity: 1 !important;
}

.navbar-menu-list .el-menu-item {
  height: 60px;
  line-height: 60px;
  border-bottom: none;
  margin: 0 10px;
}

.navbar-menu-list .el-menu-item:hover {
  background-color: #f5f7fa;
  border-bottom: 2px solid #409eff;
}

.navbar-menu-list .el-menu-item.is-active {
  background-color: #ecf5ff;
  border-bottom: 2px solid #409eff;
  color: #409eff;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.navbar-actions .el-button {
  padding: 8px 12px;
  border: none;
  background: transparent;
}

.navbar-actions .el-button:hover {
  background-color: #f5f7fa;
}
</style>

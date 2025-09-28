<template>
  <div class="sidebar" :class="{ collapsed: collapsed }">
    <div class="sidebar-header">
      <el-button 
        type="text" 
        @click="$emit('toggleCollapse')"
        class="collapse-btn"
      >
        <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
      </el-button>
    </div>
    
    <div class="sidebar-content">
      <!-- Swagger文档生成菜单 -->
      <div v-if="activeMenu === 'swagger'" class="menu-section">
        <div class="menu-title">
          <el-icon><Document /></el-icon>
          <span v-if="!collapsed">Swagger文档生成</span>
        </div>
        <el-menu
          :default-active="activeRoute"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <el-menu-item index="/swagger/genew">
            <el-icon><OfficeBuilding /></el-icon>
            <span v-if="!collapsed">Genew公司</span>
          </el-menu-item>
          <el-menu-item index="/swagger/custom">
            <el-icon><Setting /></el-icon>
            <span v-if="!collapsed">自定义模板</span>
          </el-menu-item>
          <el-menu-item index="/swagger/standard">
            <el-icon><DocumentCopy /></el-icon>
            <span v-if="!collapsed">标准模板</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <!-- JSON格式化菜单 -->
      <div v-if="activeMenu === 'json'" class="menu-section">
        <div class="menu-title">
          <el-icon><Edit /></el-icon>
          <span v-if="!collapsed">JSON格式化</span>
        </div>
        <el-menu
          :default-active="activeRoute"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <el-menu-item index="/json/format">
            <el-icon><Edit /></el-icon>
            <span v-if="!collapsed">JSON格式化</span>
          </el-menu-item>
          <el-menu-item index="/json/validate">
            <el-icon><Check /></el-icon>
            <span v-if="!collapsed">JSON验证</span>
          </el-menu-item>
          <el-menu-item index="/json/compare">
            <el-icon><Document /></el-icon>
            <span v-if="!collapsed">JSON对比</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <!-- 其他工具菜单 -->
      <div v-if="activeMenu === 'tools'" class="menu-section">
        <div class="menu-title">
          <el-icon><Tools /></el-icon>
          <span v-if="!collapsed">其他工具</span>
        </div>
        <el-menu
          :default-active="activeRoute"
          @select="handleMenuSelect"
          class="sidebar-menu"
        >
          <el-menu-item index="/tools/markdown">
            <el-icon><Document /></el-icon>
            <span v-if="!collapsed">Markdown编辑器</span>
          </el-menu-item>
          <el-menu-item index="/tools/base64">
            <el-icon><Key /></el-icon>
            <span v-if="!collapsed">Base64编解码</span>
          </el-menu-item>
          <el-menu-item index="/tools/url">
            <el-icon><Link /></el-icon>
            <span v-if="!collapsed">URL编解码</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { 
  Document, 
  Edit, 
  Tools, 
  Fold, 
  Expand,
  OfficeBuilding,
  Setting,
  DocumentCopy,
  Check,
  Key,
  Link
} from '@element-plus/icons-vue'

defineProps<{
  activeMenu: string
  collapsed: boolean
}>()

const emit = defineEmits<{
  menuClick: [route: string]
  toggleCollapse: []
}>()

const route = useRoute()
const activeRoute = computed(() => route.path)

const handleMenuSelect = (route: string) => {
  emit('menuClick', route)
}
</script>

<style scoped>
.sidebar {
  width: 250px;
  background: #fff;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.1);
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e6e6e6;
}

.collapse-btn {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
}

.collapse-btn:hover {
  background-color: #f5f7fa;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.menu-section {
  margin-bottom: 20px;
}

.menu-title {
  padding: 15px 20px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-menu {
  border: none;
}

.sidebar-menu .el-menu-item {
  height: 45px;
  line-height: 45px;
  padding: 0 20px;
  margin: 2px 10px;
  border-radius: 6px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.sidebar.collapsed .menu-title span,
.sidebar.collapsed .sidebar-menu .el-menu-item span {
  display: none;
}

.sidebar.collapsed .menu-title {
  justify-content: center;
  padding: 15px 10px 10px;
}

.sidebar.collapsed .sidebar-menu .el-menu-item {
  padding: 0 10px;
  margin: 2px 5px;
  justify-content: center;
}
</style>

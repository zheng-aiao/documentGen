<template>
  <div class="service-history">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" type="text" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ serviceName }} - 历史版本</h2>
      </div>
    </div>

    <el-card class="table-card" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span>版本列表</span>
          <el-button type="primary" @click="refreshHistory" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="historyList" v-loading="loading" border style="width: 100%">
        <el-table-column prop="name" label="服务名" min-width="220" />
        <el-table-column prop="version" label="版本名" width="120" />
        <el-table-column prop="sourceBranch" label="来源分支" width="150" />
        <el-table-column prop="startTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="downloadVersion(row, 'docx')">
              word下载
            </el-button>
            <el-button size="small" type="primary" @click="downloadVersion(row, 'pdf')" style="background-color: #42b983;">
              pdf下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 12px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'

interface HistoryVersion {
  name: string
  startTime: string
  version: string
  sourceBranch: string
  docxPath: string
  pdfPath: string
}

const route = useRoute()
const router = useRouter()

const serviceName = ref('')
const loading = ref(false)
const historyList = ref<HistoryVersion[]>([])

const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

onMounted(() => {
  serviceName.value = route.query.serviceName as string || '未知服务'
  loadHistory()
})

async function loadHistory(refresh: boolean = false) {
  loading.value = true
  try {
    const resp = await fetch(`/doc/genew/history?serviceName=${encodeURIComponent(serviceName.value)}&refresh=${refresh}`)
    if (!resp.ok) {
      throw new Error(`Http ${resp.status}`)
    }
    const data = await resp.json()
    if (data) {
      historyList.value = data?.data ?? [];
      pagination.value.total = historyList.value.length;
    }else {
      historyList.value = []
      pagination.value.total = 0
    }
    
  } catch (error) {
    ElMessage.error('加载历史版本失败')
    console.error('Load history error:', error)
  } finally {
    loading.value = false
  }
}

function refreshHistory() {
  loadHistory(true)
}

function handleSizeChange(size: number) {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  loadHistory()
}

function handleCurrentChange(page: number) {
  pagination.value.currentPage = page
  loadHistory()
}

function formatTime(timeStr: string) {
  return new Date(timeStr).toLocaleString('zh-CN')
}

async function downloadVersion(row: HistoryVersion, type: string) {
  try {
    await ElMessageBox.confirm(
      `确定要下载 ${row.name} - ${row.version} ${type === 'word' ? 'word' : 'pdf'} 文档吗？`,
      '确认下载',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    const response = await fetch('/doc/genew/history/download', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({...row, type})
    })

    if (!response.ok) {
      if (response.status === 404) {
        const errorText = await response.text()
        if (errorText === 'FILE_NOT_FOUND') {
          ElMessage.error('文件在服务器中已被移除，下载失败。建议您刷新后下载！')
          return
        }
      }
      throw new Error(`下载失败：HTTP ${response.status}`)
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const safeName = `${row.name}.${type}`.replace(/[^\w\-.]+/g, '_')
    a.download = safeName
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('开始下载文档')
    
  } catch (error) {
    if (error instanceof Error && error.message.includes('下载失败')) {
      ElMessage.error(error.message)
    }
    // 用户取消下载或其他错误
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.service-history {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  padding: 8px 12px;
  font-size: 14px;
}

.back-btn:hover {
  background-color: #f5f7fa;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header span {
  font-weight: 600;
  color: #303133;
}

.el-table {
  flex: 1;
}

.el-pagination {
  margin-top: 16px;
}
</style>

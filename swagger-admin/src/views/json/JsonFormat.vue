<template>
  <div class="json-format">
    <div class="page-header">
      <h2>JSON格式化工具</h2>
      <p>在线JSON格式化、验证和美化工具</p>
    </div>
    
    <div class="content-area">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>输入JSON</span>
                <div class="header-actions">
                  <el-button size="small" @click="clearInput">清空</el-button>
                  <el-button size="small" @click="loadSample">示例</el-button>
                </div>
              </div>
            </template>
            
            <el-input
              v-model="inputJson"
              type="textarea"
              :rows="20"
              placeholder="请输入JSON字符串..."
              class="json-input"
            />
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>格式化结果</span>
                <div class="header-actions">
                  <el-button size="small" @click="formatJson" type="primary">格式化</el-button>
                  <el-button size="small" @click="copyResult">复制</el-button>
                </div>
              </div>
            </template>
            
            <div class="result-area">
              <div v-if="isValidJson" class="formatted-json">
                <pre>{{ formattedJson }}</pre>
              </div>
              <div v-else-if="inputJson.trim()" class="error-message">
                <el-alert
                  title="JSON格式错误"
                  :description="jsonError"
                  type="error"
                  show-icon
                />
              </div>
              <div v-else class="empty-state">
                <el-empty description="请输入JSON字符串" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-card class="tools-card">
        <template #header>
          <span>其他工具</span>
        </template>
        
        <div class="tools-grid">
          <el-button @click="compressJson" :disabled="!isValidJson">
            <el-icon><Minus /></el-icon>
            压缩JSON
          </el-button>
          <el-button @click="validateJson" :disabled="!inputJson.trim()">
            <el-icon><Check /></el-icon>
            验证JSON
          </el-button>
          <el-button @click="sortJson" :disabled="!isValidJson">
            <el-icon><Sort /></el-icon>
            排序键值
          </el-button>
          <el-button @click="convertToYaml" :disabled="!isValidJson">
            <el-icon><Document /></el-icon>
            转YAML
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Minus, Check, Sort, Document } from '@element-plus/icons-vue'

const inputJson = ref('')
const formattedJson = ref('')
const jsonError = ref('')

const isValidJson = computed(() => {
  if (!inputJson.value.trim()) return false
  
  try {
    JSON.parse(inputJson.value)
    return true
  } catch (error) {
    jsonError.value = error instanceof Error ? error.message : '未知错误'
    return false
  }
})

const formatJson = () => {
  if (!isValidJson.value) {
    ElMessage.warning('请输入有效的JSON字符串')
    return
  }
  
  try {
    const parsed = JSON.parse(inputJson.value)
    formattedJson.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('格式化成功')
  } catch (error) {
    ElMessage.error('格式化失败')
  }
}

const clearInput = () => {
  inputJson.value = ''
  formattedJson.value = ''
  jsonError.value = ''
}

const loadSample = () => {
  inputJson.value = JSON.stringify({
    "name": "示例API",
    "version": "1.0.0",
    "description": "这是一个示例API文档",
    "endpoints": [
      {
        "path": "/api/users",
        "method": "GET",
        "description": "获取用户列表"
      },
      {
        "path": "/api/users/{id}",
        "method": "GET",
        "description": "获取单个用户信息"
      }
    ],
    "author": {
      "name": "开发者",
      "email": "dev@example.com"
    }
  }, null, 2)
}

const copyResult = () => {
  if (formattedJson.value) {
    navigator.clipboard.writeText(formattedJson.value)
    ElMessage.success('已复制到剪贴板')
  } else {
    ElMessage.warning('没有可复制的内容')
  }
}

const compressJson = () => {
  if (!isValidJson.value) return
  
  try {
    const parsed = JSON.parse(inputJson.value)
    formattedJson.value = JSON.stringify(parsed)
    ElMessage.success('压缩完成')
  } catch (error) {
    ElMessage.error('压缩失败')
  }
}

const validateJson = () => {
  if (isValidJson.value) {
    ElMessage.success('JSON格式正确')
  } else {
    ElMessage.error('JSON格式错误')
  }
}

const sortJson = () => {
  if (!isValidJson.value) return
  
  try {
    const parsed = JSON.parse(inputJson.value)
    const sorted = sortObjectKeys(parsed)
    formattedJson.value = JSON.stringify(sorted, null, 2)
    ElMessage.success('排序完成')
  } catch (error) {
    ElMessage.error('排序失败')
  }
}

const convertToYaml = () => {
  ElMessage.info('YAML转换功能开发中...')
}

const sortObjectKeys = (obj: any): any => {
  if (Array.isArray(obj)) {
    return obj.map(item => sortObjectKeys(item))
  } else if (obj !== null && typeof obj === 'object') {
    const sortedKeys = Object.keys(obj).sort()
    const sortedObj: any = {}
    sortedKeys.forEach(key => {
      sortedObj[key] = sortObjectKeys(obj[key])
    })
    return sortedObj
  }
  return obj
}
</script>

<style scoped>
.json-format {
  height: 100%;
  overflow-y: auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.content-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.json-input {
  font-family: 'Courier New', monospace;
}

.result-area {
  height: 400px;
  overflow-y: auto;
}

.formatted-json {
  height: 100%;
}

.formatted-json pre {
  margin: 0;
  padding: 10px;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.error-message {
  height: 100%;
  display: flex;
  align-items: center;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tools-card {
  margin-top: 20px;
}

.tools-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
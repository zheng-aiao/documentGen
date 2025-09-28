<template>
  <div class="json-validate">
    <div class="page-header">
      <h2>JSON验证工具</h2>
      <p>验证JSON格式的正确性和完整性</p>
    </div>
    
    <div class="content-area">
      <el-card>
        <template #header>
          <span>JSON验证</span>
        </template>
        
        <el-input
          v-model="inputJson"
          type="textarea"
          :rows="15"
          placeholder="请输入要验证的JSON字符串..."
          class="json-input"
        />
        
        <div class="actions">
          <el-button type="primary" @click="validateJson">验证JSON</el-button>
          <el-button @click="clearInput">清空</el-button>
        </div>
        
        <div v-if="validationResult" class="result">
          <el-alert
            :title="validationResult.title"
            :type="validationResult.type"
            :description="validationResult.description"
            show-icon
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const inputJson = ref('')
const validationResult = ref<any>(null)

const validateJson = () => {
  if (!inputJson.value.trim()) {
    ElMessage.warning('请输入JSON字符串')
    return
  }
  
  try {
    JSON.parse(inputJson.value)
    validationResult.value = {
      title: '验证成功',
      type: 'success',
      description: 'JSON格式正确'
    }
  } catch (error) {
    validationResult.value = {
      title: '验证失败',
      type: 'error',
      description: error instanceof Error ? error.message : '未知错误'
    }
  }
}

const clearInput = () => {
  inputJson.value = ''
  validationResult.value = null
}
</script>

<style scoped>
.json-validate {
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

.json-input {
  font-family: 'Courier New', monospace;
}

.actions {
  margin-bottom: 15px;
}

.result {
  margin-top: 15px;
}
</style>
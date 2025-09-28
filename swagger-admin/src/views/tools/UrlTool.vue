<template>
  <div class="url-tool">
    <div class="page-header">
      <h2>URL编解码工具</h2>
      <p>URL编码和解码工具</p>
    </div>
    
    <div class="content-area">
      <el-card>
        <template #header>
          <span>URL编解码</span>
        </template>
        
        <el-input
          v-model="urlInput"
          type="textarea"
          :rows="8"
          placeholder="请输入要编码/解码的URL..."
          class="url-input"
        />
        
        <div class="actions">
          <el-button type="primary" @click="encodeUrl">编码URL</el-button>
          <el-button @click="decodeUrl">解码URL</el-button>
          <el-button @click="clearUrl">清空</el-button>
        </div>
        
        <el-input
          v-model="urlOutput"
          type="textarea"
          :rows="8"
          placeholder="编码/解码结果将显示在这里..."
          readonly
          class="url-output"
        />
      </el-card>
      
      <el-card>
        <template #header>
          <span>URL解析</span>
        </template>
        
        <el-input
          v-model="parseInput"
          placeholder="请输入要解析的URL..."
          class="parse-input"
        />
        
        <el-button type="primary" @click="parseUrl" style="margin: 10px 0;">解析URL</el-button>
        
        <div v-if="parseResult" class="parse-result">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="协议">{{ parseResult.protocol }}</el-descriptions-item>
            <el-descriptions-item label="主机">{{ parseResult.hostname }}</el-descriptions-item>
            <el-descriptions-item label="端口">{{ parseResult.port || '默认' }}</el-descriptions-item>
            <el-descriptions-item label="路径">{{ parseResult.pathname }}</el-descriptions-item>
            <el-descriptions-item label="查询参数">{{ parseResult.search || '无' }}</el-descriptions-item>
            <el-descriptions-item label="哈希">{{ parseResult.hash || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const urlInput = ref('')
const urlOutput = ref('')
const parseInput = ref('')
const parseResult = ref<any>(null)

const encodeUrl = () => {
  if (!urlInput.value.trim()) {
    ElMessage.warning('请输入要编码的URL')
    return
  }
  
  try {
    urlOutput.value = encodeURIComponent(urlInput.value)
    ElMessage.success('URL编码成功')
  } catch (error) {
    ElMessage.error('编码失败')
  }
}

const decodeUrl = () => {
  if (!urlInput.value.trim()) {
    ElMessage.warning('请输入要解码的URL')
    return
  }
  
  try {
    urlOutput.value = decodeURIComponent(urlInput.value)
    ElMessage.success('URL解码成功')
  } catch (error) {
    ElMessage.error('解码失败')
  }
}

const clearUrl = () => {
  urlInput.value = ''
  urlOutput.value = ''
}

const parseUrl = () => {
  if (!parseInput.value.trim()) {
    ElMessage.warning('请输入要解析的URL')
    return
  }
  
  try {
    const url = new URL(parseInput.value)
    parseResult.value = {
      protocol: url.protocol,
      hostname: url.hostname,
      port: url.port,
      pathname: url.pathname,
      search: url.search,
      hash: url.hash
    }
    ElMessage.success('URL解析成功')
  } catch (error) {
    ElMessage.error('URL格式错误，无法解析')
    parseResult.value = null
  }
}
</script>

<style scoped>
.url-tool {
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

.url-input,
.url-output,
.parse-input {
  font-family: 'Courier New', monospace;
}

.actions {
  display: flex;
  gap: 10px;
  margin: 15px 0;
}

.parse-result {
  margin-top: 15px;
}
</style>
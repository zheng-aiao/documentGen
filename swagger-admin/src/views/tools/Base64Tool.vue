<template>
  <div class="base64-tool">
    <div class="page-header">
      <h2>Base64编解码工具</h2>
      <p>Base64编码和解码工具</p>
    </div>
    
    <div class="content-area">
      <el-card>
        <template #header>
          <span>Base64编解码</span>
        </template>
        
        <el-tabs v-model="activeTab" class="demo-tabs">
          <el-tab-pane label="文本编解码" name="text">
            <div class="tab-content">
              <el-input
                v-model="textInput"
                type="textarea"
                :rows="8"
                placeholder="请输入要编码的文本..."
                class="input-area"
              />
              
              <div class="actions">
                <el-button type="primary" @click="encodeText">编码</el-button>
                <el-button @click="decodeText">解码</el-button>
                <el-button @click="clearText">清空</el-button>
              </div>
              
              <el-input
                v-model="textOutput"
                type="textarea"
                :rows="8"
                placeholder="编码/解码结果将显示在这里..."
                readonly
                class="output-area"
              />
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="文件编解码" name="file">
            <div class="tab-content">
              <el-upload
                class="upload-demo"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
              >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持任意格式文件
                  </div>
                </template>
              </el-upload>
              
              <div v-if="fileInfo" class="file-info">
                <p><strong>文件名:</strong> {{ fileInfo.name }}</p>
                <p><strong>文件大小:</strong> {{ fileInfo.size }} bytes</p>
                <p><strong>文件类型:</strong> {{ fileInfo.type }}</p>
              </div>
              
              <div class="actions">
                <el-button type="primary" @click="encodeFile" :disabled="!fileInfo">编码文件</el-button>
                <el-button @click="clearFile">清空</el-button>
              </div>
              
              <el-input
                v-model="fileOutput"
                type="textarea"
                :rows="10"
                placeholder="文件编码结果将显示在这里..."
                readonly
                class="output-area"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const activeTab = ref('text')
const textInput = ref('')
const textOutput = ref('')
const fileInfo = ref<any>(null)
const fileOutput = ref('')

const encodeText = () => {
  if (!textInput.value.trim()) {
    ElMessage.warning('请输入要编码的文本')
    return
  }
  
  try {
    textOutput.value = btoa(unescape(encodeURIComponent(textInput.value)))
    ElMessage.success('编码成功')
  } catch (error) {
    ElMessage.error('编码失败')
  }
}

const decodeText = () => {
  if (!textInput.value.trim()) {
    ElMessage.warning('请输入要解码的Base64字符串')
    return
  }
  
  try {
    textOutput.value = decodeURIComponent(escape(atob(textInput.value)))
    ElMessage.success('解码成功')
  } catch (error) {
    ElMessage.error('解码失败，请检查Base64格式')
  }
}

const clearText = () => {
  textInput.value = ''
  textOutput.value = ''
}

const handleFileChange = (file: any) => {
  fileInfo.value = {
    name: file.name,
    size: file.size,
    type: file.type,
    raw: file.raw
  }
}

const encodeFile = () => {
  if (!fileInfo.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    const result = e.target?.result as string
    fileOutput.value = result.split(',')[1] // 移除data:type;base64,前缀
    ElMessage.success('文件编码成功')
  }
  reader.readAsDataURL(fileInfo.value.raw)
}

const clearFile = () => {
  fileInfo.value = null
  fileOutput.value = ''
}
</script>

<style scoped>
.base64-tool {
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

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.input-area,
.output-area {
  font-family: 'Courier New', monospace;
}

.actions {
  display: flex;
  gap: 10px;
}

.file-info {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  margin: 10px 0;
}

.file-info p {
  margin: 5px 0;
}
</style>
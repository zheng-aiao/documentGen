<template>
  <div class="swagger-custom">
    <div class="page-header">
      <h2>自定义模板 - Swagger文档生成</h2>
      <p>使用自定义模板生成API文档</p>
    </div>
    
    <div class="content-area">
      <el-card>
        <template #header>
          <span>自定义模板配置</span>
        </template>
        
        <el-form :model="form" label-width="120px">
          <el-form-item label="模板名称">
            <el-input v-model="form.templateName" placeholder="请输入模板名称" />
          </el-form-item>
          
          <el-form-item label="模板描述">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="3"
              placeholder="请输入模板描述"
            />
          </el-form-item>
          
          <el-form-item label="上传模板文件">
            <el-upload
              class="upload-demo"
              drag
              action="#"
              :auto-upload="false"
              :on-change="handleFileChange"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将模板文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .docx, .html 格式文件
                </div>
              </template>
            </el-upload>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveTemplate">保存模板</el-button>
            <el-button @click="previewTemplate">预览模板</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const form = reactive({
  templateName: '',
  description: '',
  templateFile: null as File | null
})

const handleFileChange = (file: any) => {
  form.templateFile = file.raw
}

const saveTemplate = () => {
  ElMessage.info('保存模板功能开发中...')
}

const previewTemplate = () => {
  ElMessage.info('预览模板功能开发中...')
}
</script>

<style scoped>
.swagger-custom {
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
</style>
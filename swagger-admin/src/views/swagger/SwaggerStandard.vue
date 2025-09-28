<template>
  <div class="swagger-standard">
    <div class="page-header">
      <h2>标准模板 - Swagger文档生成</h2>
      <p>使用标准模板生成API文档</p>
    </div>
    
    <div class="content-area">
      <el-card>
        <template #header>
          <span>标准模板配置</span>
        </template>
        
        <el-form :model="form" label-width="120px">
          <el-form-item label="Swagger URL">
            <el-input 
              v-model="form.swaggerUrl" 
              placeholder="请输入Swagger JSON URL"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="输出格式">
            <el-radio-group v-model="form.outputFormat">
              <el-radio label="docx">Word文档</el-radio>
              <el-radio label="pdf">PDF文档</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="generateDocument" :loading="loading">
              生成文档
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)

const form = reactive({
  swaggerUrl: '',
  outputFormat: 'docx'
})

const generateDocument = async () => {
  if (!form.swaggerUrl) {
    ElMessage.warning('请输入Swagger URL')
    return
  }
  
  loading.value = true
  try {
    // 这里调用后端API生成文档
    ElMessage.success('文档生成成功')
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.swagger-standard {
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
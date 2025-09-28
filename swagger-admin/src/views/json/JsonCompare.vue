<template>
  <div class="json-compare">
    <div class="page-header">
      <h2>JSON对比工具</h2>
      <p>对比两个JSON对象的差异</p>
    </div>
    
    <div class="content-area">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>JSON A</span>
            </template>
            <el-input
              v-model="jsonA"
              type="textarea"
              :rows="15"
              placeholder="请输入第一个JSON..."
              class="json-input"
            />
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>JSON B</span>
            </template>
            <el-input
              v-model="jsonB"
              type="textarea"
              :rows="15"
              placeholder="请输入第二个JSON..."
              class="json-input"
            />
          </el-card>
        </el-col>
      </el-row>
      
      <el-card>
        <template #header>
          <div class="card-header">
            <span>对比结果</span>
            <el-button type="primary" @click="compareJson">开始对比</el-button>
          </div>
        </template>
        
        <div v-if="compareResult" class="result">
          <el-alert
            :title="compareResult.title"
            :type="compareResult.type"
            :description="compareResult.description"
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

const jsonA = ref('')
const jsonB = ref('')
const compareResult = ref<any>(null)

const compareJson = () => {
  if (!jsonA.value.trim() || !jsonB.value.trim()) {
    ElMessage.warning('请输入两个JSON字符串')
    return
  }
  
  try {
    const objA = JSON.parse(jsonA.value)
    const objB = JSON.parse(jsonB.value)
    
    const isEqual = JSON.stringify(objA) === JSON.stringify(objB)
    
    compareResult.value = {
      title: isEqual ? 'JSON相同' : 'JSON不同',
      type: isEqual ? 'success' : 'warning',
      description: isEqual ? '两个JSON对象完全相同' : '两个JSON对象存在差异'
    }
  } catch (error) {
    compareResult.value = {
      title: '对比失败',
      type: 'error',
      description: 'JSON格式错误，请检查输入'
    }
  }
}
</script>

<style scoped>
.json-compare {
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
}

.json-input {
  font-family: 'Courier New', monospace;
}

.result {
  margin-top: 15px;
}
</style>
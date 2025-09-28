<template>
  <div class="markdown-editor">
    <div class="page-header">
      <h2>Markdown编辑器</h2>
      <p>在线Markdown编辑和预览工具</p>
    </div>
    
    <div class="content-area">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>Markdown编辑器</span>
            <div class="header-actions">
              <el-button size="small" @click="clearContent">清空</el-button>
              <el-button size="small" @click="loadSample">示例</el-button>
              <el-button size="small" @click="exportHtml">导出HTML</el-button>
            </div>
          </div>
        </template>
        
        <el-input
          v-model="markdownContent"
          type="textarea"
          :rows="20"
          placeholder="请输入Markdown内容..."
          class="markdown-input"
        />
      </el-card>
      
      <el-card>
        <template #header>
          <span>预览</span>
        </template>
        
        <div class="preview" v-html="htmlContent"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const markdownContent = ref('')

const htmlContent = computed(() => {
  // 简单的Markdown转HTML实现
  let html = markdownContent.value
  
  // 标题
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  // 粗体和斜体
  html = html.replace(/\*\*(.*)\*\*/gim, '<strong>$1</strong>')
  html = html.replace(/\*(.*)\*/gim, '<em>$1</em>')
  
  // 代码块
  html = html.replace(/```([\s\S]*?)```/gim, '<pre><code>$1</code></pre>')
  html = html.replace(/`([^`]+)`/gim, '<code>$1</code>')
  
  // 链接
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/gim, '<a href="$2">$1</a>')
  
  // 换行
  html = html.replace(/\n/gim, '<br>')
  
  return html
})

const clearContent = () => {
  markdownContent.value = ''
}

const loadSample = () => {
  markdownContent.value = `# Markdown示例

## 这是一个二级标题

这是一个**粗体**文本和*斜体*文本的示例。

### 代码示例

\`\`\`javascript
function hello() {
  console.log("Hello, World!");
}
\`\`\`

### 链接示例

这是一个[链接示例](https://example.com)。

### 列表

- 项目1
- 项目2
- 项目3

### 表格

| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 数据1 | 数据2 | 数据3 |
| 数据4 | 数据5 | 数据6 |`
}

const exportHtml = () => {
  if (!markdownContent.value.trim()) {
    ElMessage.warning('没有内容可导出')
    return
  }
  
  const html = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Markdown导出</title>
  <style>
    body { font-family: Arial, sans-serif; line-height: 1.6; margin: 40px; }
    h1, h2, h3 { color: #333; }
    code { background: #f4f4f4; padding: 2px 4px; border-radius: 3px; }
    pre { background: #f4f4f4; padding: 10px; border-radius: 5px; overflow-x: auto; }
  </style>
</head>
<body>
  ${htmlContent.value}
</body>
</html>`
  
  const blob = new Blob([html], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'markdown-export.html'
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('HTML文件已导出')
}
</script>

<style scoped>
.markdown-editor {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.markdown-input {
  font-family: 'Courier New', monospace;
}

.preview {
  min-height: 300px;
  padding: 15px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  background: #fafafa;
  line-height: 1.6;
}

.preview h1, .preview h2, .preview h3 {
  color: #333;
  margin-top: 20px;
  margin-bottom: 10px;
}

.preview h1 { font-size: 24px; }
.preview h2 { font-size: 20px; }
.preview h3 { font-size: 16px; }

.preview code {
  background: #f4f4f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.preview pre {
  background: #f4f4f4;
  padding: 10px;
  border-radius: 5px;
  overflow-x: auto;
}

.preview pre code {
  background: none;
  padding: 0;
}
</style>
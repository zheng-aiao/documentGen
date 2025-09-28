<template>
  <div class="swagger-genew">
    <div class="page-header">
      <h2>Genew公司 - Swagger文档生成</h2>
      <div class="page-actions">
        <el-button type="primary" @click="onOpenSettings">
          <el-icon><Setting /></el-icon>
          设置
        </el-button>
      </div>
    </div>

    <el-card class="table-card" style="margin-top: 16px;">
      <template #header>
        <div class="card-header">
          <span>服务列表</span>
        </div>
      </template>

      <div style="margin-bottom: 12px; display: flex; gap: 8px; align-items: center;">
        <el-input
          v-model="search.keyword"
          placeholder="按服务名称搜索"
          clearable
          style="max-width: 280px;"
          @keyup.enter.native="onSearch"
        />
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="onResetSearch">重置</el-button>
      </div>

      <el-table :data="pagedServices" v-loading="loadingServices" border style="width: 100%" :height="tableHeight">
        <el-table-column prop="name" label="服务名称" min-width="100" max-width="150" />
        <el-table-column prop="swaggerUrl" label="Swagger地址" min-width="180">
          <template #default="{ row }">
            <a :href="row.swaggerUrl" target="_blank">{{ row.swaggerUrl }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <span
              class="status-badge"
              :class="row.status === '正常' ? 'status-normal' : (row.status === '异常' ? 'status-error' : 'status-unknown')"
            >{{ row.status || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="onPreview(row)">预览</el-button>
            <el-button size="small" @click="onDownload(row)" :disabled="row.status === '异常'">下载</el-button>
            <el-button size="small" type="info" @click="onHistory(row)">历史版本</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 12px; display: flex; justify-content: flex-end;">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          :page-size="pagination.pageSize"
          :current-page="pagination.page"
          :page-sizes="[10, 20, 30, 50]"
          @size-change="onPageSizeChange"
          @current-change="onPageChange"
        />
      </div>

      <div v-if="!services.length && !loadingServices" class="empty">
        <el-empty description="请先配置并解析后显示服务列表" />
      </div>
    </el-card>

    <el-dialog
      v-model="downloadDialog.visible"
      :title="downloadDialog.serviceName || '下载确认'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="downloadDialog.form" label-width="100px">
        <el-form-item label="文件名称">
          <el-input v-model="downloadDialog.form.filename" placeholder="请输入文件名称" />
        </el-form-item>
        <el-form-item label="下载格式">
          <el-radio-group v-model="downloadDialog.form.format">
            <el-radio label="pdf">PDF</el-radio>
            <el-radio label="word">Word</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onCancelDownload">取消</el-button>
        <el-button type="primary" @click="onConfirmDownload">确认下载</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="settings.visible" title="系统设置" width="560px" :close-on-click-modal="false">
      <el-form :model="settings.form" :rules="settings.rules" ref="settingsFormRef" label-width="120px">
        <el-form-item label="文档服务地址" prop="docServiceUrl">
          <el-input v-model="settings.form.docServiceUrl" placeholder="例如：https://newdev.rdapp.com:53839/swagger/swagger-resources" />
        </el-form-item>
        <el-form-item label="name字段" prop="name">
          <el-input v-model="settings.form.name" placeholder="document.name" />
        </el-form-item>
        <el-form-item label="url字段" prop="url">
          <el-input v-model="settings.form.url" placeholder="document.url" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="settings.visible = false">取消</el-button>
        <el-button type="primary" @click="onSaveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import axios from 'axios'

type ServiceItem = {
  id: string
  name: string
  swaggerUrl: string
  status: string
  startTime: string
}

const router = useRouter()
const loadingServices = ref(false)
const services = ref<ServiceItem[]>([])
const serviceStatus = ref<Record<string, string>>({})
const search = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10 })
const tableHeight = 500
const downloadDialog = reactive({
  visible: false,
  serviceName: '',
  form: {
    filename: '',
    format: 'word' as 'word' | 'pdf'
  },
  row: null as ServiceItem | null
})
const filteredServices = computed(() => {
  const k = search.keyword.trim()
  if (!k) return services.value
  return services.value.filter(s => s.name?.includes(k))
})
const totalItems = computed(() => filteredServices.value.length)
const pagedServices = computed(() => {
  const start = (pagination.page - 1) * pagination.pageSize
  return filteredServices.value.slice(start, start + pagination.pageSize)
})

const settings = reactive({
  visible: false,
  form: {
    docServiceUrl: 'https://newdev.rdapp.com:53839/swagger/swagger-resources',
    name: 'document.name',
    url: 'document.url'
  },
  rules: {
    docServiceUrl: [{ required: true, message: '请输入文档服务地址', trigger: 'blur' }],
    name: [{ required: true, message: '请输入服务名称解析字段', trigger: 'blur' }],
    url: [{ required: true, message: '请输入swagger地址解析字段', trigger: 'blur' }]
  }
})

const settingsFormRef = ref()

const SETTINGS_KEY = 'genew_settings'


function onOpenSettings() {
  settings.visible = true
}

function saveSettings() {
  localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings.form))
}

function onSaveSettings() {
  ;(settingsFormRef.value as any)?.validate(async (ok: boolean) => {
    if (!ok) return
    saveSettings()
    settings.visible = false
    ElMessage.success('已保存设置')
    // 更改配置后，重新加载数据
    await loadServices()
  })
}

function ensureSettingsOrAsk(): boolean {
  if (!settings.form.docServiceUrl || !settings.form.name || !settings.form.url) {
    ElMessageBox.alert('请先在设置中配置“文档服务地址”和“认证登录地址”。', '提示', {
      confirmButtonText: '去设置'
    }).then(() => {
      settings.visible = true
    })
    return false
  }
  return true
}

function resolveUrlForRequest(rawUrl: string): string {
  try {
    if (import.meta.env.DEV && rawUrl.startsWith('http')) {
      return rawUrl.replace(/^https?:\/\/[^/]+/, '')
    }
  } catch {}
  return rawUrl
}

function getByPath(obj: any, path: string): any {
  if (!obj || !path) return undefined
  const segments = path.split('.')
  let current: any = obj
  for (const key of segments) {
    if (current == null) return undefined
    current = current[key]
  }
  return current
}

function mapResponseToServices(list: any[]): ServiceItem[] {
  return (list || []).map((it, idx) => {
    const nameField = (settings.form as any).name?.trim()
    const urlField = (settings.form as any).url?.trim()
    const fromName = nameField ? getByPath(it, nameField) : undefined
    const fromUrl = urlField ? getByPath(it, urlField) : undefined
    const doc = it?.document || {}
    const name: string = fromName || doc.name || it?.name || `服务-${idx + 1}`
    const swaggerUrl: string = fromUrl || doc.url || it?.url || ''
    const startTime = doc.startTime || ''
  const status = serviceStatus.value[name] || ''
    return {
      id: String(idx),
      name,
      swaggerUrl,
      startTime,
    status
    }
  })
}

async function loadServices(payload?: any[]) {
  if (!ensureSettingsOrAsk()) return
  loadingServices.value = true
  try {
    let data = payload
    if (!data) {
      const url = resolveUrlForRequest(settings.form.docServiceUrl)
      const resp = await axios.get(url, { withCredentials: true })
      data = resp.data
    }
    let list = mapResponseToServices(Array.isArray(data) ? data : [])
    services.value = list
    const maxPage = Math.max(1, Math.ceil(services.value.length / pagination.pageSize))
    if (pagination.page > maxPage) pagination.page = maxPage
    ElMessage.success('加载完成')
  } catch (e) {
    ElMessage.error('加载失败，请检查服务配置或网络')
  } finally {
    loadingServices.value = false
  }
}

async function loadStatuses() {
  try {
    const resp = await axios.get('/doc/genew/status', { withCredentials: true })
    const data = (resp.data && (resp.data.data ?? resp.data)) || {}
    serviceStatus.value = data as Record<string, string>
  } catch (e) {
    serviceStatus.value = {}
  }
}

function loadSettings() {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY)
    if (raw) {
      const data = JSON.parse(raw)
      settings.form.docServiceUrl = data.docServiceUrl || settings.form.docServiceUrl
      settings.form.url = data.url || settings.form.url
      settings.form.name = data.name || settings.form.name
    }
  } catch {}
}


function onPreview(row: ServiceItem) {
  window.open(row.swaggerUrl, '_blank')
}

function onDownload(row: ServiceItem) {
  const name = row.name || ''
  downloadDialog.serviceName = name
  downloadDialog.form.filename = name
  downloadDialog.form.format = 'word'
  downloadDialog.row = row
  downloadDialog.visible = true
}

function onHistory(row: ServiceItem) {
  // 跳转到历史版本页面
  router.push({
    path: '/swagger/history',
    query: {
      serviceName: row.name,
      serviceId: row.id || row.name // 使用服务名作为ID，实际应该有唯一ID
    }
  })
}

function onCancelDownload() {
  downloadDialog.visible = false
}

async function onConfirmDownload() {
  const row = downloadDialog.row
  if (!row) return
  const { filename, format } = downloadDialog.form
  downloadDialog.visible = false
  try {
    const type = format === 'pdf' ? 'pdf' : 'docx'
    ElMessage.success(`开始下载：${filename}.${type}`)
    const resp = await axios.get('/doc/download', {
      params: { url: row.swaggerUrl, startTime: row.startTime, fileName: filename, type },
      withCredentials: true,
      responseType: 'blob'
    })
    const blob = new Blob([resp.data], { type: type === 'pdf' ? 'application/pdf' : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${filename}.${type}`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败，请稍后重试')
  }
}

function onPageChange(page: number) {
  pagination.page = page
}

function onPageSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
}

function onSearch() {
  pagination.page = 1
}

function onResetSearch() {
  search.keyword = ''
  pagination.page = 1
}

onMounted(async () => {

  // 加载配置
  loadSettings()



  // 加载数据
  await loadStatuses()
  await loadServices()
})
</script>

<style scoped>
.swagger-genew {
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

.page-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.parse-form {
  max-width: 640px;
}

.empty {
  padding: 24px 0;
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 12px;
  color: #fff;
}

.status-normal {
  background-color: #409eff; /* Element Plus primary */
}

.status-error {
  background-color: #f56c6c; /* Element Plus danger */
}

.status-unknown {
  background-color: #909399; /* Element Plus info/placeholder */
}
</style>
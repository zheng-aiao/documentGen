# 前端布局说明

## 布局结构

新的前端应用采用了顶部导航栏 + 左侧边栏的布局结构：

### 顶部导航栏 (TopNavbar.vue)
- **Swagger文档生成**: 包含Genew公司、自定义模板、标准模板等子选项
- **JSON格式化**: 包含JSON格式化、JSON验证、JSON对比等子选项  
- **其他工具**: 包含Markdown编辑器、Base64编解码、URL编解码等子选项

### 左侧边栏 (Sidebar.vue)
根据顶部导航栏的选择，显示对应的二级菜单：

#### Swagger文档生成
- Genew公司
- 自定义模板
- 标准模板

#### JSON格式化
- JSON格式化
- JSON验证
- JSON对比

#### 其他工具
- Markdown编辑器
- Base64编解码
- URL编解码

### 主内容区域
使用Vue Router进行页面路由，每个功能都有对应的页面组件。

## 页面组件

### Swagger文档生成相关
- `SwaggerGenew.vue` - Genew公司专用模板
- `SwaggerCustom.vue` - 自定义模板
- `SwaggerStandard.vue` - 标准模板

### JSON工具相关
- `JsonFormat.vue` - JSON格式化工具
- `JsonValidate.vue` - JSON验证工具
- `JsonCompare.vue` - JSON对比工具

### 其他工具
- `MarkdownEditor.vue` - Markdown编辑器
- `Base64Tool.vue` - Base64编解码工具
- `UrlTool.vue` - URL编解码工具

## 路由配置

所有路由都配置在 `src/router/index.ts` 中，采用嵌套路由结构：
- `/swagger/genew` - Genew公司模板
- `/swagger/custom` - 自定义模板
- `/swagger/standard` - 标准模板
- `/json/format` - JSON格式化
- `/json/validate` - JSON验证
- `/json/compare` - JSON对比
- `/tools/markdown` - Markdown编辑器
- `/tools/base64` - Base64工具
- `/tools/url` - URL工具

## 特性

1. **响应式设计**: 左侧边栏支持折叠/展开
2. **主题切换**: 顶部导航栏包含主题切换按钮
3. **用户菜单**: 包含用户设置和帮助文档
4. **图标支持**: 使用Element Plus图标库
5. **路由导航**: 完整的路由导航系统

## 开发说明

- 使用Vue 3 + TypeScript
- 使用Element Plus UI组件库
- 使用Vue Router 4进行路由管理
- 使用Pinia进行状态管理
- 支持热重载开发

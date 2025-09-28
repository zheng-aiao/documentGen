import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'
import SwaggerGenew from '@/views/swagger/SwaggerGenew.vue'
import SwaggerCustom from '@/views/swagger/SwaggerCustom.vue'
import SwaggerStandard from '@/views/swagger/SwaggerStandard.vue'
import ServiceHistory from '@/views/swagger/ServiceHistory.vue'
import JsonFormat from '@/views/json/JsonFormat.vue'
import JsonValidate from '@/views/json/JsonValidate.vue'
import JsonCompare from '@/views/json/JsonCompare.vue'
import MarkdownEditor from '@/views/tools/MarkdownEditor.vue'
import Base64Tool from '@/views/tools/Base64Tool.vue'
import UrlTool from '@/views/tools/UrlTool.vue'

const routes = [
    {
        path: '/',
        component: Layout,
        children: [
            {
                path: '',
                redirect: '/swagger'
            },
            // Swagger文档生成模块
            {
                path: 'swagger',
                redirect: '/swagger/genew',
                children: [
                    {
                        path: 'genew',
                        name: 'SwaggerGenew',
                        component: SwaggerGenew
                    },
                    {
                        path: 'custom',
                        name: 'SwaggerCustom',
                        component: SwaggerCustom
                    },
                    {
                        path: 'standard',
                        name: 'SwaggerStandard',
                        component: SwaggerStandard
                    },
                    {
                        path: 'history',
                        name: 'ServiceHistory',
                        component: ServiceHistory
                    }
                ]
            },
            // JSON工具模块
            {
                path: 'json',
                redirect: '/json/format',
                children: [
                    {
                        path: 'format',
                        name: 'JsonFormat',
                        component: JsonFormat
                    },
                    {
                        path: 'validate',
                        name: 'JsonValidate',
                        component: JsonValidate
                    },
                    {
                        path: 'compare',
                        name: 'JsonCompare',
                        component: JsonCompare
                    }
                ]
            },
            // 其他工具模块
            {
                path: 'tools',
                redirect: '/tools/markdown',
                children: [
                    {
                        path: 'markdown',
                        name: 'MarkdownEditor',
                        component: MarkdownEditor
                    },
                    {
                        path: 'base64',
                        name: 'Base64Tool',
                        component: Base64Tool
                    },
                    {
                        path: 'url',
                        name: 'UrlTool',
                        component: UrlTool
                    }
                ]
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router

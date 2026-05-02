<template>
  <div class="tag-graph-container">
    <div class="page-header">
      <h2 class="page-title">智慧图谱</h2>
      <el-button type="primary" @click="refreshGraph">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <div class="graph-content">
      <el-empty v-if="loading === false && graphData.nodes.length === 0" description="暂无标签数据，无法生成图谱" />
      
      <div v-else class="graph-wrapper">
        <div ref="chartRef" class="chart-container"></div>
        
        <div class="legend-panel">
          <h4 class="legend-title">图例说明</h4>
          <div class="legend-list">
            <div v-for="(color, index) in categoryColors" :key="index" class="legend-item">
              <span class="legend-dot" :style="{ backgroundColor: color }"></span>
              <span class="legend-text">标签分类 {{ index + 1 }}</span>
            </div>
          </div>
          <div class="legend-tip">
            <p><strong>提示：</strong></p>
            <p>• 节点大小表示笔记数量多少</p>
            <p>• 连线表示标签共同出现在同一笔记中</p>
            <p>• 鼠标悬停查看详细信息</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getTagGraph } from '@/api/tag'
import type { TagGraphVO, GraphNodeVO, GraphEdgeVO } from '@/types'

const router = useRouter()

const chartRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const chartInstance = ref<echarts.ECharts | null>(null)

const graphData = ref<TagGraphVO>({
  nodes: [],
  edges: []
})

const categoryColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399'
]

const fetchGraphData = async () => {
  loading.value = true
  try {
    const res = await getTagGraph()
    graphData.value = res.data
    await nextTick()
    renderChart()
  } catch (error) {
    console.error('获取图谱数据失败:', error)
    ElMessage.error('获取图谱数据失败')
  } finally {
    loading.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) return

  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartRef.value)
  }

  const { nodes, edges } = graphData.value

  if (nodes.length === 0) {
    chartInstance.value.clear()
    return
  }

  const maxNoteCount = Math.max(...nodes.map(n => n.noteCount || 1), 1)
  
  const echartsNodes = nodes.map((node: GraphNodeVO) => ({
    id: node.id.toString(),
    name: node.name,
    value: node.noteCount,
    symbolSize: Math.max(30, Math.min(80, 30 + (node.noteCount || 0) / maxNoteCount * 50)),
    itemStyle: {
      color: node.color
    },
    category: node.category % 5,
    label: {
      show: true,
      fontSize: 12,
      color: '#303133'
    }
  }))

  const echartsLinks = edges.map((edge: GraphEdgeVO) => ({
    source: edge.source.toString(),
    target: edge.target.toString(),
    value: edge.value,
    lineStyle: {
      width: Math.max(1, Math.min(5, edge.value)),
      opacity: 0.6
    }
  }))

  const categories = categoryColors.map((color, index) => ({
    name: `分类 ${index + 1}`
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          const data = params.data
          return `<div style="font-weight: bold; margin-bottom: 5px;">${data.name}</div>
                  <div>笔记数量: <span style="color: #409EFF; font-weight: bold;">${data.value || 0}</span></div>`
        } else if (params.dataType === 'edge') {
          const data = params.data
          const sourceNode = nodes.find(n => n.id.toString() === data.source)
          const targetNode = nodes.find(n => n.id.toString() === data.target)
          return `<div style="font-weight: bold; margin-bottom: 5px;">标签关联</div>
                  <div>${sourceNode?.name} ↔ ${targetNode?.name}</div>
                  <div>共同笔记: <span style="color: #409EFF; font-weight: bold;">${data.value}</span></div>`
        }
        return ''
      }
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: echartsNodes,
        links: echartsLinks,
        categories: categories,
        roam: true,
        label: {
          show: true,
          position: 'right',
          formatter: '{b}'
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 10
          }
        },
        force: {
          repulsion: 400,
          edgeLength: [80, 200],
          gravity: 0.1
        },
        draggable: true
      }
    ]
  }

  chartInstance.value.setOption(option)

  chartInstance.value.off('click')
  chartInstance.value.on('click', (params: any) => {
    if (params.dataType === 'node') {
      const tagId = Number(params.data.id)
      router.push({
        path: '/notes',
        query: { tagId: tagId.toString() }
      })
    }
  })
}

const handleResize = () => {
  chartInstance.value?.resize()
}

const refreshGraph = () => {
  fetchGraphData()
}

onMounted(() => {
  fetchGraphData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
  chartInstance.value = null
})
</script>

<style scoped>
.tag-graph-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-shrink: 0;
}

.page-title {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.graph-content {
  flex: 1;
  overflow: auto;
}

.graph-wrapper {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);
  min-height: 500px;
}

.chart-container {
  flex: 1;
  height: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.legend-panel {
  width: 220px;
  flex-shrink: 0;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
}

.legend-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-text {
  font-size: 12px;
  color: #606266;
}

.legend-tip {
  border-top: 1px solid #e4e7ed;
  padding-top: 12px;
}

.legend-tip p {
  margin: 4px 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.legend-tip p:first-child {
  color: #606266;
  font-weight: 500;
}
</style>

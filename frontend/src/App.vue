<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="logo">
        <h2>🤖 客服 Agent</h2>
      </div>
      <button class="new-chat-btn" @click="createNewSession">
        + 新建会话
      </button>
      <div class="sessions-list">
        <div
          v-for="session in sessions"
          :key="session.conversationId"
          :class="['session-item', { active: activeSessionId === session.conversationId }]"
          @click="selectSession(session.conversationId)"
        >
          <span class="session-title">{{ session.title }}</span>
          <span class="session-time">{{ formatTime(session.updatedAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 主聊天区 -->
    <div class="main-content">
      <div v-if="!activeSessionId" class="empty-state">
        <h2>欢迎使用智能客服 Agent</h2>
        <p>基于 DeepSeek API + 知识库的智能客服系统</p>
        <button @click="createNewSession" class="start-btn">开始聊天</button>
      </div>

      <div v-else class="chat-area">
        <!-- 消息列表 -->
        <div class="messages" ref="messagesContainer">
          <div
            v-for="(msg, idx) in currentMessages"
            :key="idx"
            :class="['message', msg.role]"
          >
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
          <div v-if="isLoading" class="loading">
            <span></span><span></span><span></span>
          </div>
        </div>

        <!-- 输入框 -->
        <div class="input-area">
          <input
            v-model="userInput"
            type="text"
            placeholder="输入消息..."
            @keydown.enter="sendMessage"
            :disabled="isLoading"
          />
          <button @click="sendMessage" :disabled="isLoading || !userInput.trim()">
            {{ isLoading ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 右侧知识库 -->
    <div class="knowledge-panel">
      <h3>📚 知识库</h3>
      <button class="import-btn" @click="showKnowledgeModal = true">导入文档</button>
      <div class="knowledge-list">
        <div
          v-for="doc in knowledgeDocs"
          :key="doc.id"
          class="knowledge-item"
        >
          <div class="doc-title">{{ doc.title }}</div>
          <div class="doc-category">{{ doc.category }}</div>
        </div>
      </div>
    </div>
  </div>

  <!-- 知识库导入对话框 -->
  <div v-if="showKnowledgeModal" class="modal-overlay" @click="showKnowledgeModal = false">
    <div class="modal" @click.stop>
      <h3>导入知识库文档</h3>
      <input v-model="knowledgeForm.title" type="text" placeholder="文档标题" />
      <input v-model="knowledgeForm.category" type="text" placeholder="分类" />
      <textarea v-model="knowledgeForm.content" placeholder="文档内容"></textarea>
      <input v-model="knowledgeForm.sourceUrl" type="text" placeholder="来源链接（可选）" />
      <div class="modal-actions">
        <button @click="importKnowledge" class="confirm-btn">导入</button>
        <button @click="showKnowledgeModal = false" class="cancel-btn">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import api from './api/client'

interface Conversation {
  conversationId: string
  title: string
  userId: string
  status: string
  createdAt: string
  updatedAt: string
}

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

interface KnowledgeDoc {
  id: number
  title: string
  category: string
  content: string
  createdAt: string
}

const userId = 'user-' + Math.random().toString(36).substr(2, 9)
const sessions = ref<Conversation[]>([])
const activeSessionId = ref<string>('')
const currentMessages = ref<ChatMessage[]>([])
const userInput = ref('')
const isLoading = ref(false)
const knowledgeDocs = ref<KnowledgeDoc[]>([])
const showKnowledgeModal = ref(false)
const messagesContainer = ref<HTMLElement>()

const knowledgeForm = ref({
  title: '',
  category: '',
  content: '',
  sourceUrl: ''
})

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const createNewSession = async () => {
  try {
    const res = await api.post('/api/sessions', {
      userId,
      title: '新会话 ' + new Date().toLocaleTimeString()
    })
    sessions.value.unshift(res.data)
    selectSession(res.data.conversationId)
  } catch (error) {
    alert('创建会话失败')
  }
}

const selectSession = async (conversationId: string) => {
  activeSessionId.value = conversationId
  try {
    const res = await api.get(`/api/chat/history/${conversationId}`)
    currentMessages.value = res.data || []
    userInput.value = ''
    await nextTick()
    scrollToBottom()
  } catch (error) {
    alert('加载会话历史失败')
  }
}

const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  const message = userInput.value
  userInput.value = ''
  isLoading.value = true

  try {
    // 添加用户消息到 UI
    currentMessages.value.push({
      role: 'user',
      content: message,
      createdAt: new Date().toISOString()
    })
    await nextTick()
    scrollToBottom()

    // 调用后端 API
    const res = await api.post('/api/chat', {
      conversationId: activeSessionId.value,
      userId,
      message
    })

    // 添加 AI 回复到 UI
    currentMessages.value.push({
      role: 'assistant',
      content: res.data.answer,
      createdAt: new Date().toISOString()
    })

    // 刷新会话列表
    await loadSessions()
  } catch (error) {
    alert('发送消息失败')
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const loadSessions = async () => {
  try {
    const res = await api.get(`/api/sessions/${userId}`)
    sessions.value = res.data || []
  } catch (error) {
    console.error('加载会话失败', error)
  }
}

const loadKnowledge = async () => {
  try {
    const res = await api.get('/api/knowledge')
    knowledgeDocs.value = res.data || []
  } catch (error) {
    console.error('加载知识库失败', error)
  }
}

const importKnowledge = async () => {
  try {
    await api.post('/api/knowledge/import', knowledgeForm.value)
    knowledgeForm.value = { title: '', category: '', content: '', sourceUrl: '' }
    showKnowledgeModal.value = false
    await loadKnowledge()
    alert('知识库导入成功')
  } catch (error) {
    alert('知识库导入失败')
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadSessions()
  loadKnowledge()
})
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  width: 250px;
  background: #fff;
  border-right: 1px solid #e1e6eb;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.logo {
  margin-bottom: 16px;
  text-align: center;
}

.logo h2 {
  font-size: 18px;
  color: #1d2129;
}

.new-chat-btn {
  background: #0052cc;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  margin-bottom: 16px;
  transition: background 0.3s;
}

.new-chat-btn:hover {
  background: #0039a6;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  border-left: 3px solid transparent;
}

.session-item:hover {
  background: #f2f3f5;
}

.session-item.active {
  background: #e8f0ff;
  border-left-color: #0052cc;
}

.session-title {
  display: block;
  font-size: 14px;
  color: #1d2129;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  display: block;
  font-size: 12px;
  color: #8a919f;
  margin-top: 4px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.empty-state h2 {
  font-size: 28px;
  color: #1d2129;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: #8a919f;
  margin-bottom: 32px;
}

.start-btn {
  background: #0052cc;
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 16px;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message.user {
  align-self: flex-end;
}

.message.assistant {
  align-self: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 6px;
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 14px;
}

.message.user .message-content {
  background: #0052cc;
  color: white;
}

.message.assistant .message-content {
  background: #f2f3f5;
  color: #1d2129;
}

.message-time {
  font-size: 12px;
  color: #8a919f;
  margin-top: 4px;
  text-align: right;
}

.message.assistant .message-time {
  text-align: left;
}

.loading {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: #f2f3f5;
  border-radius: 6px;
  width: fit-content;
}

.loading span {
  width: 8px;
  height: 8px;
  background: #8a919f;
  border-radius: 50%;
  animation: bounce 1.4s infinite;
}

.loading span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 80%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  40% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e1e6eb;
  background: #fff;
}

.input-area input {
  flex: 1;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}

.input-area input:focus {
  border-color: #0052cc;
}

.input-area input:disabled {
  background: #f5f5f5;
  color: #8a919f;
}

.input-area button {
  background: #0052cc;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  white-space: nowrap;
}

.input-area button:disabled {
  background: #c9cace;
  cursor: not-allowed;
}

.knowledge-panel {
  width: 280px;
  background: #fff;
  border-left: 1px solid #e1e6eb;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.knowledge-panel h3 {
  font-size: 16px;
  color: #1d2129;
  margin-bottom: 12px;
}

.import-btn {
  background: #f2f3f5;
  color: #1d2129;
  border: 1px solid #d9d9d9;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-bottom: 12px;
}

.import-btn:hover {
  background: #e8e8e8;
}

.knowledge-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.knowledge-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.doc-title {
  color: #1d2129;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-category {
  color: #8a919f;
  margin-top: 4px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal h3 {
  font-size: 16px;
  color: #1d2129;
  margin-bottom: 16px;
}

.modal input,
.modal textarea {
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.modal input:focus,
.modal textarea:focus {
  outline: none;
  border-color: #0052cc;
}

.modal textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 16px;
}

.confirm-btn {
  background: #0052cc;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.cancel-btn {
  background: #f2f3f5;
  color: #1d2129;
  border: 1px solid #d9d9d9;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
</style>

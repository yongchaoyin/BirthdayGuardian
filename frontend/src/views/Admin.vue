<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1>管理员后台</h1>
      <button @click="logout" class="logout-btn">退出登录</button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <h3>总用户数</h3>
        <p class="stat-number">{{ stats.totalUsers || 0 }}</p>
      </div>
      <div class="stat-card">
        <h3>管理员数</h3>
        <p class="stat-number">{{ stats.adminCount || 0 }}</p>
      </div>
      <div class="stat-card">
        <h3>普通用户数</h3>
        <p class="stat-number">{{ stats.userCount || 0 }}</p>
      </div>
    </div>

    <!-- 标签页切换 -->
    <div class="tabs">
      <button
        :class="['tab', { active: activeTab === 'users' }]"
        @click="activeTab = 'users'"
      >
        用户管理
      </button>
      <button
        :class="['tab', { active: activeTab === 'announcements' }]"
        @click="activeTab = 'announcements'"
      >
        公告管理
      </button>
    </div>

    <!-- 用户管理 -->
    <div v-show="activeTab === 'users'" class="tab-content">
      <div class="search-bar">
        <input
          v-model="userSearch"
          placeholder="搜索用户名或邮箱"
          @keyup.enter="loadUsers"
        />
        <button @click="loadUsers">搜索</button>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>
              <select
                :value="user.role"
                @change="changeUserRole(user.id, $event.target.value)"
                :disabled="user.id === currentUserId"
              >
                <option value="user">普通用户</option>
                <option value="admin">管理员</option>
              </select>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <button
                @click="deleteUserConfirm(user.id)"
                class="btn-danger"
                :disabled="user.id === currentUserId"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button @click="prevUserPage" :disabled="userPage === 1">上一页</button>
        <span>第 {{ userPage }} 页</span>
        <button @click="nextUserPage" :disabled="users.length < userPageSize">下一页</button>
      </div>
    </div>

    <!-- 公告管理 -->
    <div v-show="activeTab === 'announcements'" class="tab-content">
      <button @click="showCreateAnnouncementForm" class="btn-primary">发布新公告</button>

      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="announcement in announcements" :key="announcement.id">
            <td>{{ announcement.id }}</td>
            <td>{{ announcement.title }}</td>
            <td>
              <span :class="announcement.status === 1 ? 'status-active' : 'status-inactive'">
                {{ announcement.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(announcement.createTime) }}</td>
            <td>
              <button @click="editAnnouncement(announcement)" class="btn-edit">编辑</button>
              <button @click="deleteAnnouncementConfirm(announcement.id)" class="btn-danger">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button @click="prevAnnouncementPage" :disabled="announcementPage === 1">上一页</button>
        <span>第 {{ announcementPage }} 页</span>
        <button @click="nextAnnouncementPage" :disabled="announcements.length < announcementPageSize">下一页</button>
      </div>
    </div>

    <!-- 公告表单弹窗 -->
    <div v-if="showAnnouncementForm" class="modal">
      <div class="modal-content">
        <h2>{{ editingAnnouncement ? '编辑公告' : '发布新公告' }}</h2>
        <form @submit.prevent="saveAnnouncement">
          <div class="form-group">
            <label>标题</label>
            <input v-model="announcementForm.title" required />
          </div>
          <div class="form-group">
            <label>内容</label>
            <textarea v-model="announcementForm.content" rows="6" required></textarea>
          </div>
          <div class="form-group">
            <label>状态</label>
            <select v-model="announcementForm.status">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-primary">保存</button>
            <button type="button" @click="closeAnnouncementForm" class="btn-secondary">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { getUserList, getUserStats, updateUserRole, deleteUser } from '../api/admin'
import { getAnnouncementList, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '../api/announcement'

export default {
  name: 'Admin',
  data() {
    return {
      activeTab: 'users',
      stats: {},

      // 用户管理
      users: [],
      userPage: 1,
      userPageSize: 10,
      userSearch: '',
      currentUserId: null,

      // 公告管理
      announcements: [],
      announcementPage: 1,
      announcementPageSize: 10,
      showAnnouncementForm: false,
      editingAnnouncement: null,
      announcementForm: {
        title: '',
        content: '',
        status: 1
      }
    }
  },
  mounted() {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      this.currentUserId = user.userId

      // 验证是否是管理员
      if (user.role !== 'admin') {
        alert('无权访问管理员页面')
        this.$router.push('/home')
        return
      }
    }

    this.loadStats()
    this.loadUsers()
    this.loadAnnouncements()
  },
  methods: {
    async loadStats() {
      try {
        const res = await getUserStats()
        if (res.code === 200) {
          this.stats = res.data
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    },

    async loadUsers() {
      try {
        const res = await getUserList({
          pageNum: this.userPage,
          pageSize: this.userPageSize,
          keyword: this.userSearch
        })
        if (res.code === 200) {
          this.users = res.data.records
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        alert('加载用户列表失败')
      }
    },

    async changeUserRole(userId, newRole) {
      try {
        const res = await updateUserRole(userId, newRole)
        if (res.code === 200) {
          alert('角色更新成功')
          this.loadUsers()
          this.loadStats()
        }
      } catch (error) {
        console.error('更新角色失败:', error)
        alert('更新角色失败')
      }
    },

    async deleteUserConfirm(userId) {
      if (!confirm('确定要删除此用户吗?')) return

      try {
        const res = await deleteUser(userId)
        if (res.code === 200) {
          alert('用户删除成功')
          this.loadUsers()
          this.loadStats()
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        alert('删除用户失败')
      }
    },

    prevUserPage() {
      if (this.userPage > 1) {
        this.userPage--
        this.loadUsers()
      }
    },

    nextUserPage() {
      this.userPage++
      this.loadUsers()
    },

    async loadAnnouncements() {
      try {
        const res = await getAnnouncementList({
          pageNum: this.announcementPage,
          pageSize: this.announcementPageSize
        })
        if (res.code === 200) {
          this.announcements = res.data.records
        }
      } catch (error) {
        console.error('加载公告列表失败:', error)
        alert('加载公告列表失败')
      }
    },

    showCreateAnnouncementForm() {
      this.editingAnnouncement = null
      this.announcementForm = {
        title: '',
        content: '',
        status: 1
      }
      this.showAnnouncementForm = true
    },

    editAnnouncement(announcement) {
      this.editingAnnouncement = announcement
      this.announcementForm = {
        title: announcement.title,
        content: announcement.content,
        status: announcement.status
      }
      this.showAnnouncementForm = true
    },

    async saveAnnouncement() {
      try {
        let res
        if (this.editingAnnouncement) {
          res = await updateAnnouncement(this.editingAnnouncement.id, this.announcementForm)
        } else {
          res = await createAnnouncement(this.announcementForm)
        }

        if (res.code === 200) {
          alert(this.editingAnnouncement ? '公告更新成功' : '公告创建成功')
          this.closeAnnouncementForm()
          this.loadAnnouncements()
        }
      } catch (error) {
        console.error('保存公告失败:', error)
        alert('保存公告失败')
      }
    },

    closeAnnouncementForm() {
      this.showAnnouncementForm = false
      this.editingAnnouncement = null
    },

    async deleteAnnouncementConfirm(id) {
      if (!confirm('确定要删除此公告吗?')) return

      try {
        const res = await deleteAnnouncement(id)
        if (res.code === 200) {
          alert('公告删除成功')
          this.loadAnnouncements()
        }
      } catch (error) {
        console.error('删除公告失败:', error)
        alert('删除公告失败')
      }
    },

    prevAnnouncementPage() {
      if (this.announcementPage > 1) {
        this.announcementPage--
        this.loadAnnouncements()
      }
    },

    nextAnnouncementPage() {
      this.announcementPage++
      this.loadAnnouncements()
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN')
    },

    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.admin-header h1 {
  color: #333;
}

.logout-btn {
  padding: 10px 20px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  border-radius: 8px;
  color: white;
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: normal;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  margin: 0;
}

.tabs {
  display: flex;
  border-bottom: 2px solid #e0e0e0;
  margin-bottom: 20px;
}

.tab {
  padding: 12px 24px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
}

.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
  font-weight: bold;
}

.tab-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-bar input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-bar button {
  padding: 10px 20px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.data-table th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
}

.data-table tr:hover {
  background-color: #f9f9f9;
}

.data-table select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-primary {
  padding: 10px 20px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 20px;
}

.btn-edit {
  padding: 6px 12px;
  background-color: #ffc107;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}

.btn-danger {
  padding: 6px 12px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-danger:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.status-active {
  color: #28a745;
  font-weight: bold;
}

.status-inactive {
  color: #dc3545;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 16px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-secondary {
  padding: 10px 20px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>

<template>
  <div class="admin-container">
    <div class="admin-header">
      <h1>管理员后台</h1>
      <button @click="logout" class="logout-btn">退出登录</button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card highlight">
        <h3>总用户</h3>
        <p class="stat-number">{{ stats.totalUsers || 0 }}</p>
        <span class="stat-sub">守护者大家庭</span>
      </div>
      <div class="stat-card">
        <h3>管理员</h3>
        <p class="stat-number">{{ stats.adminCount || 0 }}</p>
        <span class="stat-sub">运营守护助手</span>
      </div>
      <div class="stat-card">
        <h3>VIP守护者</h3>
        <p class="stat-number">{{ stats.vipCount || 0 }}</p>
        <span class="stat-sub">拥有短信计划名额</span>
      </div>
      <div class="stat-card">
        <h3>温馨体验</h3>
        <p class="stat-number">{{ stats.freeCount || 0 }}</p>
        <span class="stat-sub">可以升级的挚友</span>
      </div>
    </div>

    <div class="stats-cards secondary">
      <div class="stat-card">
        <h3>今日生日</h3>
        <p class="stat-number">{{ stats.todayBirthdayCount || 0 }}</p>
        <span class="stat-sub">正在被守护的心意</span>
      </div>
      <div class="stat-card">
        <h3>今日邮件提醒</h3>
        <p class="stat-number">{{ stats.todayEmailCount || 0 }}</p>
        <span class="stat-sub">已送达的暖心问候</span>
      </div>
      <div class="stat-card">
        <h3>今日短信提醒</h3>
        <p class="stat-number">{{ stats.todaySmsCount || 0 }}</p>
        <span class="stat-sub">短信服务预备中</span>
      </div>
    </div>

    <div class="stats-cards secondary">
      <div class="stat-card">
        <h3>明日生日</h3>
        <p class="stat-number">{{ stats.tomorrowBirthdayCount || 0 }}</p>
        <span class="stat-sub">稍后送上的祝福</span>
      </div>
      <div class="stat-card">
        <h3>明日邮件计划</h3>
        <p class="stat-number">{{ stats.tomorrowEmailPlanCount || 0 }}</p>
        <span class="stat-sub">排队中的提醒</span>
      </div>
      <div class="stat-card">
        <h3>明日短信计划</h3>
        <p class="stat-number">{{ stats.tomorrowSmsPlanCount || 0 }}</p>
        <span class="stat-sub">VIP优先通知</span>
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

      <div class="broadcast-panel">
        <el-card class="broadcast-card">
          <template #header>
            <div class="card-title">
              <span>📧 群发邮件</span>
            </div>
          </template>
          <el-form label-width="80px">
            <el-form-item label="主题">
              <el-input v-model="broadcastEmailForm.subject" placeholder="请输入邮件主题" />
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="broadcastEmailForm.content"
                type="textarea"
                :rows="4"
                placeholder="向所有有邮箱的用户发送提醒"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="sendBroadcastEmail" :loading="broadcastEmailLoading">
                发送群邮
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="broadcast-card">
          <template #header>
            <div class="card-title">
              <span>📱 群发短信</span>
            </div>
          </template>
          <el-form label-width="80px">
            <el-form-item label="内容">
              <el-input
                v-model="broadcastSmsForm.content"
                type="textarea"
                :rows="4"
                placeholder="向所有填写手机号的用户发送短信"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="sendBroadcastSms" :loading="broadcastSmsLoading">
                发送群短信
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <div class="chart-card">
        <div class="chart-card-header">📈 通知发送趋势</div>
        <div ref="notificationChart" class="trend-chart"></div>
      </div>

      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>手机号</th>
            <th>角色</th>
            <th>会员等级</th>
            <th>可守护人数</th>
            <th>VIP到期</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.phone || '—' }}</td>
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
            <td>
              <span :class="['membership-pill', user.vipActive ? 'vip' : 'free']">
                {{ user.vipActive ? 'VIP守护者' : '温馨体验' }}
              </span>
            </td>
            <td>{{ user.maxRoleCount || (user.vipActive ? 20 : 3) }}</td>
            <td>{{ formatVipExpire(user.vipExpireTime) }}</td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <button
                class="btn-secondary"
                @click="changeUserMembership(user, user.vipActive ? 'FREE' : 'VIP')"
                :disabled="membershipUpdatingId === user.id || user.id === currentUserId"
              >
                {{ user.vipActive ? '降为体验' : '设为VIP' }}
              </button>
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
import { getUserList, getUserStats, updateUserRole, updateUserMembership, deleteUser, broadcastEmail, broadcastSms, getNotificationStats } from '../api/admin'
import { getAnnouncementList, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '../api/announcement'
import * as echarts from 'echarts'

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
      membershipUpdatingId: null,
      broadcastEmailForm: {
        subject: '',
        content: ''
      },
      broadcastSmsForm: {
        content: ''
      },
      broadcastEmailLoading: false,
      broadcastSmsLoading: false,
      notificationTrend: [],
      notificationChart: null,

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
    this.loadNotificationTrend()
  },
  beforeUnmount() {
    if (this.notificationChart) {
      window.removeEventListener('resize', this.handleChartResize)
      this.notificationChart.dispose()
      this.notificationChart = null
    }
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

    async changeUserMembership(user, targetLevel) {
      if (this.membershipUpdatingId === user.id) return

      if (targetLevel === 'VIP' && user.vipActive) {
        alert('该用户已经是VIP守护者啦')
        return
      }
      if (targetLevel === 'FREE' && !user.vipActive) {
        alert('该用户当前处于温馨体验计划')
        return
      }

      const confirmMessage = targetLevel === 'VIP'
        ? `确认将用户「${user.username}」升级为VIP守护者吗？`
        : `确认将用户「${user.username}」调整为温馨体验计划吗？`

      if (!confirm(confirmMessage)) return

      try {
        this.membershipUpdatingId = user.id
        const res = await updateUserMembership(user.id, targetLevel)
        if (res.code === 200) {
          alert('会员状态更新成功')
          this.loadUsers()
          this.loadStats()
        }
      } catch (error) {
        console.error('更新会员状态失败:', error)
        alert('更新会员状态失败')
      } finally {
        this.membershipUpdatingId = null
      }
    },

    async sendBroadcastEmail() {
      if (!this.broadcastEmailForm.subject.trim() || !this.broadcastEmailForm.content.trim()) {
        alert('请填写邮件主题和内容')
        return
      }
      try {
        this.broadcastEmailLoading = true
        const res = await broadcastEmail({
          subject: this.broadcastEmailForm.subject,
          content: this.broadcastEmailForm.content
        })
        if (res.code === 200) {
          alert(`邮件已发送：成功 ${res.data.successCount} / ${res.data.targetCount}`)
          this.loadNotificationTrend()
        }
      } catch (error) {
        console.error('群发邮件失败:', error)
        alert('群发邮件失败，请稍后再试')
      } finally {
        this.broadcastEmailLoading = false
      }
    },

    async sendBroadcastSms() {
      if (!this.broadcastSmsForm.content.trim()) {
        alert('请填写短信内容')
        return
      }
      try {
        this.broadcastSmsLoading = true
        const res = await broadcastSms({
          content: this.broadcastSmsForm.content
        })
        if (res.code === 200) {
          alert(`短信已发送：成功 ${res.data.successCount} / ${res.data.targetCount}`)
          this.loadNotificationTrend()
        }
      } catch (error) {
        console.error('群发短信失败:', error)
        alert('群发短信失败，请稍后再试')
      } finally {
        this.broadcastSmsLoading = false
      }
    },

    async loadNotificationTrend() {
      try {
        const res = await getNotificationStats({ days: 14 })
        if (res.code === 200) {
          this.notificationTrend = res.data.points || []
          this.$nextTick(() => {
            this.renderNotificationChart()
          })
        }
      } catch (error) {
        console.error('加载通知趋势失败:', error)
      }
    },

    renderNotificationChart() {
      if (!this.$refs.notificationChart) {
        return
      }
      if (!this.notificationChart) {
        this.notificationChart = echarts.init(this.$refs.notificationChart)
        window.addEventListener('resize', this.handleChartResize)
      }

      const categories = this.notificationTrend.map(item => item.date)
      const emailData = this.notificationTrend.map(item => item.emailCount || 0)
      const smsData = this.notificationTrend.map(item => item.smsCount || 0)
      const wechatData = this.notificationTrend.map(item => item.wechatCount || 0)

      const option = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['邮件', '短信', '微信'] },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: categories
        },
        yAxis: {
          type: 'value',
          minInterval: 1
        },
        series: [
          {
            name: '邮件',
            type: 'line',
            smooth: true,
            data: emailData,
            symbol: 'circle'
          },
          {
            name: '短信',
            type: 'line',
            smooth: true,
            data: smsData,
            symbol: 'circle'
          },
          {
            name: '微信',
            type: 'line',
            smooth: true,
            data: wechatData,
            symbol: 'circle'
          }
        ]
      }

      this.notificationChart.setOption(option, true)
    },

    handleChartResize() {
      if (this.notificationChart) {
        this.notificationChart.resize()
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

    formatVipExpire(dateStr) {
      if (!dateStr) return '—'
      return this.formatDate(dateStr)
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
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stats-cards.secondary {
  margin-top: -10px;
}

.stat-card {
  background: #ffffff;
  padding: 20px;
  border-radius: 12px;
  color: #333;
  text-align: center;
  box-shadow: 0 10px 24px rgba(102, 126, 234, 0.12);
  border: 1px solid rgba(102, 126, 234, 0.15);
}

.stat-card.highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.3);
}

.stats-cards.secondary .stat-card {
  box-shadow: 0 6px 14px rgba(103, 114, 229, 0.12);
}

.stat-card h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: normal;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
}

.stat-card.highlight .stat-number {
  color: #fff;
}

.stat-sub {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.stat-card:not(.highlight) .stat-sub {
  color: #888;
}

.membership-pill {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  background: #eef3ff;
  color: #4a5fe2;
}

.membership-pill.vip {
  background: #fff3e6;
  color: #ff8c42;
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

.broadcast-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.broadcast-card {
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.12);
}

.broadcast-card .card-title {
  font-weight: 600;
  color: #4b4f7c;
}

.chart-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
}

.chart-card-header {
  font-weight: 600;
  color: #4b4f7c;
  margin-bottom: 10px;
}

.trend-chart {
  width: 100%;
  height: 320px;
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

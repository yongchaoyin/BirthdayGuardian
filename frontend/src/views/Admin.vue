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
          @keyup.enter="handleSearchUsers"
        />
        <button @click="handleSearchUsers">搜索</button>
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

      <div class="table-card">
        <div class="table-card__header">
          <h3>守护者列表</h3>
          <p>查看并管理每一位守护者的权限与会员状态</p>
        </div>
        <div class="table-card__body">
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
        </div>
      </div>

      <div class="table-pagination" v-if="userTotal > userPageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="userTotal"
          :page-size="userPageSize"
          :current-page="userPage"
          @current-change="handleUserPageChange"
        />
      </div>
    </div>

    <!-- 公告管理 -->
    <div v-show="activeTab === 'announcements'" class="tab-content">
      <button @click="showCreateAnnouncementForm" class="btn-primary">发布新公告</button>

      <div class="table-card">
        <div class="table-card__header">
          <h3>公告列表</h3>
          <p>及时维护站内公告，向守护者传递最新信息</p>
        </div>
        <div class="table-card__body">
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
        </div>
      </div>

      <div class="table-pagination" v-if="announcementTotal > announcementPageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="announcementTotal"
          :page-size="announcementPageSize"
          :current-page="announcementPage"
          @current-change="handleAnnouncementPageChange"
        />
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
      userTotal: 0,
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
      announcementTotal: 0,
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
          const payload = res.data || {}
          const records = Array.isArray(payload.records) ? payload.records : Array.isArray(payload) ? payload : []
          this.users = records
          const total = payload.total
          this.userTotal = typeof total === 'number' ? total : records.length
        }
      } catch (error) {
        console.error('加载用户列表失败:', error)
        alert('加载用户列表失败')
      }
    },

    handleUserPageChange(page) {
      this.userPage = page
      this.loadUsers()
    },

    handleSearchUsers() {
      this.userPage = 1
      this.loadUsers()
    },

    handleAnnouncementPageChange(page) {
      this.announcementPage = page
      this.loadAnnouncements()
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
          if (this.users.length === 1 && this.userPage > 1) {
            this.userPage--
          }
          this.loadUsers()
          this.loadStats()
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        alert('删除用户失败')
      }
    },

    async loadAnnouncements() {
      try {
        const res = await getAnnouncementList({
          pageNum: this.announcementPage,
          pageSize: this.announcementPageSize
        })
        if (res.code === 200) {
          const payload = res.data || {}
          this.announcements = Array.isArray(payload.records) ? payload.records : []
          const total = payload.total
          this.announcementTotal = typeof total === 'number' ? total : this.announcements.length
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
          if (this.announcements.length === 1 && this.announcementPage > 1) {
            this.announcementPage--
          }
          this.loadAnnouncements()
        }
      } catch (error) {
        console.error('删除公告失败:', error)
        alert('删除公告失败')
      }
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
  max-width: 1220px;
  margin: 0 auto;
  padding: 48px 32px 64px;
  min-height: 100vh;
  background: var(--bg-gradient);
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 36px;
  padding: 20px 28px;
  border-radius: 32px;
  background: var(--surface-glass);
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: var(--shadow-elevated);
  backdrop-filter: blur(18px);
}

.admin-header h1 {
  margin: 0;
  font-size: 28px;
  color: #2f1f1c;
  font-family: var(--font-heading);
}

.logout-btn {
  padding: 12px 28px;
  border-radius: var(--btn-radius);
  border: none;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.02em;
  background: linear-gradient(135deg, #ff9c95, #ff6f61);
  box-shadow: 0 16px 32px rgba(255, 111, 97, 0.28);
  cursor: pointer;
  transition: transform var(--transition-quick), box-shadow var(--transition-quick);
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 38px rgba(255, 111, 97, 0.34);
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
  background: var(--surface-strong);
  padding: 24px 20px;
  border-radius: 28px;
  color: #342724;
  text-align: center;
  box-shadow: 0 20px 42px rgba(132, 124, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.68);
  backdrop-filter: blur(14px);
}

.stat-card.highlight {
  background: linear-gradient(135deg, rgba(255, 224, 198, 0.92), rgba(240, 139, 103, 0.9));
  color: #fff;
  box-shadow: 0 24px 42px rgba(240, 139, 103, 0.32);
}

.stats-cards.secondary .stat-card {
  box-shadow: 0 6px 14px rgba(103, 114, 229, 0.12);
}

.stat-card h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
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
  color: rgba(98, 81, 75, 0.75);
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
  display: inline-flex;
  padding: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.6);
  margin-bottom: 24px;
  box-shadow: 0 16px 32px rgba(168, 146, 255, 0.16);
}

.tab {
  padding: 10px 24px;
  background: transparent;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-size: 15px;
  color: rgba(78, 60, 55, 0.7);
  transition: all var(--transition-quick);
}

.tab.active {
  background: rgba(255, 255, 255, 0.9);
  color: #2f1f1c;
  box-shadow: 0 10px 20px rgba(160, 150, 255, 0.18);
}

.tab-content {
  background: var(--surface-strong);
  padding: 26px 28px;
  border-radius: 32px;
  box-shadow: 0 20px 42px rgba(176, 160, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.68);
  backdrop-filter: blur(14px);
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 18px 20px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.68);
  box-shadow: 0 18px 34px rgba(173, 164, 255, 0.18);
  backdrop-filter: blur(12px);
}

.search-bar input {
  flex: 1;
  padding: 12px 18px;
  border: 1px solid rgba(240, 139, 103, 0.22);
  border-radius: 999px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.9);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-bar button {
  padding: 12px 26px;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: 0.02em;
  background: var(--primary-gradient);
  color: #fff;
  box-shadow: 0 16px 32px rgba(240, 139, 103, 0.28);
  transition: transform var(--transition-quick), box-shadow var(--transition-quick);
}

.search-bar button:hover {
  transform: translateY(-1px);
  box-shadow: 0 18px 36px rgba(240, 139, 103, 0.34);
}

.search-bar input:focus {
  outline: none;
  border-color: rgba(240, 139, 103, 0.45);
  box-shadow: 0 0 0 3px rgba(240, 139, 103, 0.18);
}

.table-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.75);
  box-shadow: 0 18px 48px rgba(170, 152, 255, 0.22);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-bottom: 20px;
}

.table-card__header {
  padding: 24px 28px 16px;
  border-bottom: 1px solid rgba(230, 225, 255, 0.7);
}

.table-card__header h3 {
  margin: 0;
  font-size: 20px;
  color: #3f2f2b;
}

.table-card__header p {
  margin: 8px 0 0;
  color: rgba(93, 76, 71, 0.8);
  font-size: 13px;
}

.table-card__body {
  padding: 0 4px 4px;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.table-pagination :deep(.el-pagination.is-background .el-pager li) {
  border-radius: 12px;
  border: none;
}

.table-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background-color: #f08b67;
}

.data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: rgba(255, 255, 255, 0.95);
}

.data-table th,
.data-table td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid rgba(230, 223, 255, 0.8);
}

.data-table th {
  background: linear-gradient(135deg, rgba(247, 239, 255, 0.95), rgba(235, 248, 255, 0.9));
  font-weight: 600;
  color: #5a4a46;
  border-bottom: 1px solid rgba(227, 221, 255, 0.9);
}

.data-table tbody tr {
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.data-table tbody tr:hover {
  background: rgba(250, 243, 255, 0.85);
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(201, 186, 255, 0.24);
}

.data-table select {
  padding: 6px 12px;
  border: 1px solid rgba(214, 206, 255, 0.9);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
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
  padding: 12px 26px;
  background: linear-gradient(135deg, #f08b67, #f5a178);
  color: white;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  margin-bottom: 20px;
  box-shadow: 0 16px 32px rgba(240, 139, 103, 0.24);
}

.btn-edit {
  padding: 8px 16px;
  background: linear-gradient(135deg, #ffd56a, #ffc043);
  color: #4a362f;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  margin-right: 8px;
  box-shadow: 0 10px 20px rgba(255, 192, 67, 0.24);
}

.btn-danger {
  padding: 8px 16px;
  background: linear-gradient(135deg, #ff6b6b, #ff8787);
  color: white;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(255, 107, 107, 0.25);
}

.btn-danger:disabled {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.5), rgba(255, 135, 135, 0.5));
  cursor: not-allowed;
  box-shadow: none;
}

.status-active {
  color: #28a745;
  font-weight: bold;
}

.status-inactive {
  color: #dc3545;
  font-weight: bold;
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
  font-weight: 600;
  color: #3b2a26;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(240, 139, 103, 0.22);
  border-radius: 18px;
  font-size: 14px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.9);
  transition: border-color var(--transition-quick), box-shadow var(--transition-quick);
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: rgba(240, 139, 103, 0.45);
  box-shadow: 0 0 0 3px rgba(240, 139, 103, 0.16);
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-secondary {
  padding: 12px 26px;
  background: rgba(255, 255, 255, 0.9);
  color: #3d2a26;
  border: 1px solid rgba(240, 139, 103, 0.28);
  border-radius: 999px;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: 0.02em;
  box-shadow: 0 16px 32px rgba(160, 150, 255, 0.18);
  transition: transform var(--transition-quick), box-shadow var(--transition-quick);
}

.btn-secondary:hover {
  transform: translateY(-1px);
  box-shadow: 0 20px 36px rgba(160, 150, 255, 0.26);
}
</style>

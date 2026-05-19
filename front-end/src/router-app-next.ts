import { createRouter, createWebHistory } from 'vue-router'
import TeacherShell from '@/layouts/TeacherWorkbenchShellNext.vue'
import AdminShell from '@/layouts/AdminShell.vue'
import TeacherDashboardView from '@/views/TeacherDashboardHomePage.vue'
import TeacherAssignmentDetailView from '@/views/TeacherAssignmentDetailWorkspace.vue'
import TeacherAssignmentListView from '@/views/TeacherAssignmentListWorkspace.vue'
import TeacherAssignmentFormView from '@/views/TeacherAssignmentBuilderPage.vue'
import TeacherStatisticsView from '@/views/TeacherStatisticsWorkspace.vue'
import TeacherGradingView from '@/views/TeacherGradingCenterPage.vue'
import TeacherClassManagementPage from '@/views/TeacherClassManagementPage.vue'
import TeacherMessageCenterPage from '@/views/TeacherMessageCenterPage.vue'
import LoginView from '@/views/UnifiedLoginView.vue'
import AdminLoginView from '@/views/AdminLoginPage.vue'
import AdminDashboardPage from '@/views/AdminDashboardPage.vue'
import AdminOrganizationPage from '@/views/AdminOrganizationPage.vue'
import AdminAccountsPage from '@/views/AdminAccountsPage.vue'
import AdminRelationsPage from '@/views/AdminRelationsPage.vue'
import { getAuthSession, getDefaultRouteForRole } from '@/utils/auth-session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { title: '统一登录', public: true }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLoginView,
      meta: { title: '管理员登录', public: true }
    },
    {
      path: '/',
      component: TeacherShell,
      meta: { requiresAuth: true, allowedRoles: ['teacher'] },
      children: [
        { path: '', redirect: '/dashboard' },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: TeacherDashboardView,
          meta: { title: '教师工作台' }
        },
        {
          path: 'assignments',
          name: 'assignments',
          component: TeacherAssignmentListView,
          meta: { title: '作业列表' }
        },
        {
          path: 'assignments/new',
          name: 'assignment-new',
          component: TeacherAssignmentFormView,
          meta: { title: '发布作业' }
        },
        {
          path: 'assignments/:id',
          name: 'assignment-detail',
          component: TeacherAssignmentDetailView,
          meta: { title: '作业详情' }
        },
        {
          path: 'assignments/:id/grading',
          name: 'grading',
          component: TeacherGradingView,
          meta: { title: '批改中心' }
        },
        {
          path: 'grading-center',
          name: 'grading-center',
          component: TeacherGradingView,
          meta: { title: '批改中心' }
        },
        {
          path: 'messages',
          name: 'teacher-messages',
          component: TeacherMessageCenterPage,
          meta: { title: '消息中心' }
        },
        {
          path: 'class-management',
          name: 'class-management',
          component: TeacherClassManagementPage,
          meta: { title: '班级管理' }
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: TeacherStatisticsView,
          meta: { title: '统计分析' }
        }
      ]
    },
    {
      path: '/admin',
      component: AdminShell,
      meta: { requiresAuth: true, allowedRoles: ['admin'] },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: AdminDashboardPage,
          meta: { title: '数据看板' }
        },
        {
          path: 'organization',
          name: 'admin-organization',
          component: AdminOrganizationPage,
          meta: { title: '组织管理' }
        },
        {
          path: 'accounts',
          name: 'admin-accounts',
          component: AdminAccountsPage,
          meta: { title: '账号管理' }
        },
        {
          path: 'relations',
          name: 'admin-relations',
          component: AdminRelationsPage,
          meta: { title: '关系配置' }
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const session = getAuthSession()
  const isAuthenticated = Boolean(session?.authenticated)
  const allowedRoles = to.matched.flatMap((record) =>
    Array.isArray(record.meta.allowedRoles) ? (record.meta.allowedRoles as string[]) : []
  )
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const adminOnly = allowedRoles.includes('admin')

  if (requiresAuth && !isAuthenticated) {
    return { name: adminOnly ? 'admin-login' : 'login' }
  }

  if ((to.name === 'login' || to.name === 'admin-login') && session) {
    return getDefaultRouteForRole(session.role)
  }

  if (allowedRoles.length > 0 && session && !allowedRoles.includes(session.role)) {
    return getDefaultRouteForRole(session.role)
  }

  return true
})

router.afterEach((to) => {
  const pageTitle = typeof to.meta.title === 'string' ? to.meta.title : '小学课后作业系统'
  document.title = `${pageTitle} | 小学课后作业系统`
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import TeacherShell from '@/layouts/TeacherShell.vue'
import AdminShell from '@/layouts/AdminShell.vue'
import TeacherDashboardView from '@/views/TeacherDashboardView.vue'
import TeacherAssignmentDetailView from '@/views/TeacherAssignmentDetailBusinessView.vue'
import TeacherAssignmentListView from '@/views/TeacherAssignmentListBusinessView.vue'
import TeacherAssignmentFormView from '@/views/TeacherAssignmentFormPage.vue'
import TeacherStatisticsView from '@/views/TeacherStatisticsPage.vue'
import TeacherGradingView from '@/views/TeacherGradingPage.vue'
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
      meta: { title: '统一登录中心', public: true }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLoginView,
      meta: { title: '管理后台登录', public: true }
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
          path: 'statistics',
          name: 'statistics',
          component: TeacherStatisticsView,
          meta: { title: '统计分析' }
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

  if (to.meta.requiresAuth && !isAuthenticated) {
    return { name: 'login' }
  }

  if (to.name === 'login' && session) {
    return getDefaultRouteForRole(session.role)
  }

  if (allowedRoles.length > 0 && session && !allowedRoles.includes(session.role)) {
    return { name: 'access-hub' }
  }

  return true
})

router.afterEach((to) => {
  const pageTitle = typeof to.meta.title === 'string' ? to.meta.title : '统一登录中心'
  document.title = `${pageTitle} · 小学课后作业系统`
})

export default router

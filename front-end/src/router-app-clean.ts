import { createRouter, createWebHistory } from 'vue-router'
import { getAuthSession, getDefaultRouteForRole } from '@/utils/auth-session-clean'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/UnifiedLoginViewClean.vue'),
      meta: { title: '\u7edf\u4e00\u767b\u5f55', public: true }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('@/views/AdminLoginPageClean.vue'),
      meta: { title: '\u7ba1\u7406\u5458\u767b\u5f55', public: true }
    },
    {
      path: '/student',
      component: () => import('@/layouts/StudentWorkbenchShell.vue'),
      meta: { requiresAuth: true, allowedRoles: ['student'] },
      children: [
        { path: '', redirect: '/student/home' },
        {
          path: 'home',
          name: 'student-home',
          component: () => import('@/views/StudentHomePage.vue'),
          meta: { title: '\u5b66\u751f\u9996\u9875' }
        },
        {
          path: 'homeworks',
          name: 'student-homeworks',
          component: () => import('@/views/StudentHomeworkListPage.vue'),
          meta: { title: '\u6211\u7684\u4f5c\u4e1a' }
        },
        {
          path: 'homeworks/:id',
          name: 'student-homework-detail',
          component: () => import('@/views/StudentHomeworkDetailPage.vue'),
          meta: { title: '\u4f5c\u4e1a\u8be6\u60c5' }
        },
        {
          path: 'homeworks/:id/print',
          name: 'student-homework-print',
          component: () => import('@/views/StudentHomeworkPrintPage.vue'),
          meta: { title: '\u6253\u5370\u4f5c\u4e1a' }
        },
        {
          path: 'homeworks/:id/submit',
          name: 'student-homework-submit',
          component: () => import('@/views/StudentHomeworkSubmitPage.vue'),
          meta: { title: '\u63d0\u4ea4\u4f5c\u4e1a' }
        },
        {
          path: 'homeworks/:id/feedback',
          name: 'student-homework-feedback',
          component: () => import('@/views/StudentFeedbackPage.vue'),
          meta: { title: '\u6279\u6539\u53cd\u9988' }
        },
        {
          path: 'wrong-book',
          name: 'student-wrong-book',
          component: () => import('@/views/StudentWrongBookPage.vue'),
          meta: { title: '\u9519\u9898\u672c' }
        },
        {
          path: 'wrong-book/practice',
          name: 'student-wrong-book-practice',
          component: () => import('@/views/StudentWrongBookPracticePage.vue'),
          meta: { title: '\u9519\u9898\u5c0f\u7ec3\u4e60' }
        },
        {
          path: 'wrong-book/practice/result',
          name: 'student-wrong-book-practice-result',
          component: () => import('@/views/StudentWrongBookPracticeResultPage.vue'),
          meta: { title: '\u7ec3\u4e60\u7ed3\u679c' }
        },
        {
          path: 'wrong-book/practice/history',
          name: 'student-wrong-book-practice-history',
          component: () => import('@/views/StudentWrongBookPracticeHistoryPage.vue'),
          meta: { title: '\u7ec3\u4e60\u8bb0\u5f55' }
        },
        {
          path: 'wrong-book/practice/:practiceId',
          name: 'student-wrong-book-practice-detail',
          component: () => import('@/views/StudentWrongBookPracticeDetailPage.vue'),
          meta: { title: '\u7ec3\u4e60\u8be6\u60c5' }
        },
        {
          path: 'messages',
          name: 'student-messages',
          component: () => import('@/views/StudentMessagesPage.vue'),
          meta: { title: '\u6d88\u606f\u4e2d\u5fc3' }
        },
        {
          path: 'profile',
          name: 'student-profile',
          component: () => import('@/views/StudentProfilePage.vue'),
          meta: { title: '\u6211\u7684' }
        }
      ]
    },
    {
      path: '/parent',
      component: () => import('@/layouts/ParentWorkbenchShell.vue'),
      meta: { requiresAuth: true, allowedRoles: ['parent'] },
      children: [
        { path: '', redirect: '/parent/home' },
        {
          path: 'home',
          name: 'parent-home',
          component: () => import('@/views/ParentHomePage.vue'),
          meta: { title: '\u5bb6\u957f\u9996\u9875' }
        },
        {
          path: 'homeworks/:childId/:id',
          name: 'parent-homework-detail',
          component: () => import('@/views/ParentHomeworkDetailPage.vue'),
          meta: { title: '\u5b69\u5b50\u4f5c\u4e1a' }
        },
        {
          path: 'homeworks/:childId/:id/print',
          name: 'parent-homework-print',
          component: () => import('@/views/ParentHomeworkPrintPage.vue'),
          meta: { title: '\u6253\u5370\u4f5c\u4e1a' }
        },
        {
          path: 'homeworks/:childId/:id/assist',
          name: 'parent-homework-assist',
          component: () => import('@/views/ParentAssistPage.vue'),
          meta: { title: '\u534f\u52a9\u63d0\u4ea4' }
        },
        {
          path: 'homeworks/:childId/:id/feedback',
          name: 'parent-homework-feedback',
          component: () => import('@/views/ParentFeedbackPage.vue'),
          meta: { title: '\u8001\u5e08\u53cd\u9988' }
        },
        {
          path: 'messages',
          name: 'parent-messages',
          component: () => import('@/views/ParentMessagesPage.vue'),
          meta: { title: '\u6d88\u606f\u4e2d\u5fc3' }
        },
        {
          path: 'profile',
          name: 'parent-profile',
          component: () => import('@/views/ParentProfilePage.vue'),
          meta: { title: '\u6211\u7684' }
        }
      ]
    },
    {
      path: '/',
      component: () => import('@/layouts/TeacherWorkbenchShellNext.vue'),
      meta: { requiresAuth: true, allowedRoles: ['teacher'] },
      children: [
        { path: '', redirect: '/dashboard' },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/TeacherDashboardView.vue'),
          meta: { title: '\u6559\u5e08\u5de5\u4f5c\u53f0' }
        },
        {
          path: 'assignments',
          name: 'assignments',
          component: () => import('@/views/TeacherAssignmentListView.vue'),
          meta: { title: '\u4f5c\u4e1a\u5217\u8868' }
        },
        {
          path: 'assignments/new',
          name: 'assignment-new',
          component: () => import('@/views/TeacherAssignmentFormView.vue'),
          meta: { title: '\u53d1\u5e03\u4f5c\u4e1a' }
        },
        {
          path: 'assignments/:id',
          name: 'assignment-detail',
          component: () => import('@/views/TeacherAssignmentDetailView.vue'),
          meta: { title: '\u4f5c\u4e1a\u8be6\u60c5' }
        },
        {
          path: 'assignments/:id/print',
          name: 'assignment-print',
          component: () => import('@/views/TeacherAssignmentPrintView.vue'),
          meta: { title: '\u6253\u5370\u4f5c\u4e1a' }
        },
        {
          path: 'assignments/:id/grading',
          name: 'grading',
          component: () => import('@/views/TeacherGradingView.vue'),
          meta: { title: '\u6279\u6539\u4e2d\u5fc3' }
        },
        {
          path: 'grading-center',
          name: 'grading-center',
          component: () => import('@/views/TeacherGradingView.vue'),
          meta: { title: '\u6279\u6539\u4e2d\u5fc3' }
        },
        {
          path: 'messages',
          name: 'teacher-messages',
          component: () => import('@/views/TeacherMessageCenterPage.vue'),
          meta: { title: '\u6d88\u606f\u4e2d\u5fc3' }
        },
        {
          path: 'class-management',
          name: 'class-management',
          component: () => import('@/views/TeacherClassManagementPage.vue'),
          meta: { title: '\u73ed\u7ea7\u7ba1\u7406' }
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: () => import('@/views/TeacherStatisticsView.vue'),
          meta: { title: '\u7edf\u8ba1\u5206\u6790' }
        }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminShellClean.vue'),
      meta: { requiresAuth: true, allowedRoles: ['admin'] },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/AdminDashboardPage.vue'),
          meta: { title: '\u6570\u636e\u770b\u677f' }
        },
        {
          path: 'organization',
          name: 'admin-organization',
          component: () => import('@/views/AdminOrganizationPage.vue'),
          meta: { title: '\u7ec4\u7ec7\u7ba1\u7406' }
        },
        {
          path: 'accounts',
          name: 'admin-accounts',
          component: () => import('@/views/AdminAccountsPage.vue'),
          meta: { title: '\u8d26\u53f7\u7ba1\u7406' }
        },
        {
          path: 'relations',
          name: 'admin-relations',
          component: () => import('@/views/AdminRelationsPage.vue'),
          meta: { title: '\u5173\u7cfb\u914d\u7f6e' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      redirect: () => {
        const session = getAuthSession()
        return session ? getDefaultRouteForRole(session.role) : '/login'
      }
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
  const pageTitle = typeof to.meta.title === 'string' ? to.meta.title : '\u5c0f\u5b66\u8bfe\u540e\u4f5c\u4e1a\u7cfb\u7edf'
  document.title = `${pageTitle} | \u5c0f\u5b66\u8bfe\u540e\u4f5c\u4e1a\u7cfb\u7edf`
})

export default router

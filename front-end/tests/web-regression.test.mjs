import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'

const projectRoot = path.resolve(process.cwd())

function readSource(relativePath) {
  return fs.readFileSync(path.join(projectRoot, relativePath), 'utf8')
}

const mainSource = readSource('src/main.ts')
const authSource = readSource('src/utils/auth-session-clean.ts')
const routerSource = readSource('src/router-app-clean.ts')
const loginSource = readSource('src/views/UnifiedLoginViewClean.vue')
const adminLoginSource = readSource('src/views/AdminLoginPageClean.vue')
const roleGuideSource = readSource('src/views/RoleGuideView.vue')
const visiblePageSources = [
  readSource('src/views/TeacherMessageCenterPage.vue'),
  readSource('src/views/AdminRelationsPage.vue'),
  readSource('src/views/StudentMessagesPage.vue'),
  readSource('src/views/ParentMessagesPage.vue'),
  readSource('src/stores/adminPortal.ts'),
  readSource('src/utils/admin-portal-view.ts'),
  readSource('src/utils/student-portal-view.ts'),
  readSource('src/utils/parent-portal-view.ts')
]
const failureStateSources = [
  readSource('src/api/http.ts'),
  readSource('src/api/http-clean.ts'),
  readSource('src/api/auth-service.ts'),
  readSource('src/api/auth-service-clean.ts'),
  readSource('src/api/teacher.ts'),
  readSource('src/api/student.ts'),
  readSource('src/api/parent.ts')
]

const suspiciousTokens = [
  '\u93c1',
  '\u93c3',
  '\u7f01',
  '\u9359',
  '\u9427',
  '\u7b20',
  '\u701b',
  '\u5a11',
  '\u93b5',
  '\u941d',
  '\u7480',
  '\u20ac',
  '澶辫触',
  '鎺ュ彛',
  '璇锋眰',
  '鍚庣',
  '鐧诲綍'
]

assert.match(mainSource, /router-app-clean/)
assert.match(authSource, /student\/home/)
assert.match(authSource, /parent\/home/)
assert.match(routerSource, /\(\) => import\(/)
assert.match(routerSource, /StudentWorkbenchShell\.vue/)
assert.match(routerSource, /student-home/)
assert.match(routerSource, /student-wrong-book/)
assert.match(routerSource, /student-messages/)
assert.match(routerSource, /student-profile/)
assert.match(routerSource, /ParentWorkbenchShell\.vue/)
assert.match(routerSource, /parent-home/)
assert.match(routerSource, /parent-homework-detail/)
assert.match(routerSource, /parent-homework-assist/)
assert.match(routerSource, /parent-homework-feedback/)
assert.match(routerSource, /parent-messages/)
assert.match(routerSource, /parent-profile/)
assert.match(loginSource, /heroTitle: '\\u7edf\\u4e00\\u8d26\\u53f7\\u767b\\u5f55'/)
assert.match(loginSource, /remember: '\\u8bb0\\u4f4f\\u767b\\u5f55\\u72b6\\u6001'/)
assert.match(adminLoginSource, /title: '\\u7ba1\\u7406\\u5458\\u767b\\u5f55'/)
assert.match(roleGuideSource, /studentTitle: '\\u5b66\\u751f\\u5165\\u53e3\\u5df2\\u5c31\\u7eea'/)

for (const source of [authSource, routerSource, loginSource, adminLoginSource, roleGuideSource]) {
  for (const token of suspiciousTokens) {
    assert.equal(source.includes(token), false)
  }
}

for (const source of failureStateSources) {
  for (const token of suspiciousTokens) {
    assert.equal(source.includes(token), false)
  }
}

for (const source of visiblePageSources) {
  for (const token of suspiciousTokens) {
    assert.equal(source.includes(token), false)
  }
}

assert.match(visiblePageSources[0], /消息中心/)
assert.match(visiblePageSources[1], /关系配置/)
assert.match(visiblePageSources[2], /全部消息/)
assert.match(visiblePageSources[3], /全部消息/)
assert.match(visiblePageSources[4], /（\$\{meta\}）/)

console.log('web-regression: ok')

/**
 * 身份验证相关 API
 */

// TODO: 等后端接口完成后，取消下面的注释，并删除 Mock 代码
// import axios from 'axios'
// export function loginApi(data: any) {
//   return axios.post('/api/auth/login', data)
// }

// ================= Mock 代码区 =================
export function loginApi(username?: string, password?: string): Promise<any> {
  return new Promise((resolve, reject) => {
    // 模拟 500ms 的网络请求延迟
    setTimeout(() => {
      // 只要点击登录就当做成功（你可以根据需要在此处写死特定的账号密码校验）
      resolve({
        code: 200,
        message: 'success',
        data: {
          token: 'mock-jwt-token-999988887777', // 模拟返回的鉴权 Token
          userInfo: {
            id: 101,
            name: '测试教师',
            role: 'teacher'
          }
        }
      })
    }, 500)
  })
}
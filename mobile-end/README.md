# 小学课后作业移动端

基于 `uni-app + Vue 3 + TypeScript` 的学生端 / 家长端共用工程，当前优先面向微信小程序。

## 已完成内容

- 统一登录入口，登录后按角色分流
- 学生端：作业首页、作业详情、提交作业、查看反馈、消息中心、我的
- 家长端：孩子首页、作业详情、协助提交、消息中心、我的
- 共用状态管理、请求封装、页面守卫、本地演示数据

## 启动与构建

```bash
npm install
npm run build:mp-weixin
```

如需本地浏览器预览，可继续使用：

```bash
npm run dev:h5
```

## 微信小程序运行方式

1. 执行 `npm run build:mp-weixin`
2. 打开微信开发者工具
3. 选择“导入项目”
4. 项目目录指向：`dist/build/mp-weixin`
5. `AppID` 暂时可先留空或使用测试号，后续上线前再替换正式 AppID

## 目录说明

- `src/pages/auth`：统一登录入口
- `src/pages/student`：学生端页面
- `src/pages/parent`：家长端页面
- `src/pages/profile`：共用我的页面
- `src/store`：登录态与移动端数据状态
- `src/services`：请求层与后续接口接入入口
- `src/data`：当前本地演示数据
- `src/utils`：路由守卫、状态文案等辅助工具

## 联调说明

- 当前默认启用本地演示登录，便于先看页面和交互
- 若要接入真实登录接口，可设置 `VITE_USE_MOBILE_MOCK=false`
- 接口基础地址可通过 `VITE_API_BASE_URL` 配置

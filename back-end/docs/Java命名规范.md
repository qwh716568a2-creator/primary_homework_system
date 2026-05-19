# 后端 Java 命名规范

这套规范只追求一件事：类名短、直白、好认。

适用范围：
- 当前项目后端
- 后续新建的 controller、service、mapper、dto、query、vo、po

## 1. 总原则

- 一个类名尽量只表达一个业务对象
- 不要把类名写得太长
- 优先用 `StudentDto` 这种简单命名
- 能不用复杂前缀，就不用复杂前缀
- 看到类名，能立刻知道它是干什么的

## 2. 包结构

推荐继续用下面这套：

```text
com.primaryhomework.backend
├─ controller
├─ service
│  └─ impl
├─ mapper
├─ entity
│  ├─ dto
│  ├─ query
│  ├─ vo
│  └─ po
├─ config
└─ utils
```

## 3. 命名规则

### Controller

- 学生：`StudentController`
- 教师：`TeacherController`
- 家长：`ParentController`
- 登录：`AuthController`
- 作业：`HomeworkController`

规则：
- 业务名 + `Controller`
- 不要写太细太长

不推荐：
- `TeacherHomeworkManagementController`
- `StudentHomeworkTaskController`

### Service

- `StudentService`
- `TeacherService`
- `AuthService`
- `HomeworkService`

实现类：
- `StudentServiceImpl`
- `TeacherServiceImpl`
- `AuthServiceImpl`

规则：
- 业务名 + `Service`
- 实现类固定 `Impl`

### Mapper

- `StudentMapper`
- `TeacherMapper`
- `ParentMapper`
- `UserMapper`
- `HomeworkMapper`

规则：
- 表对应的业务名 + `Mapper`

## 4. 对象命名

### Dto

用于：
- 接收前端参数
- 返回前端数据
- service 层简单传参

推荐：
- `LoginDto`
- `RegisterDto`
- `StudentDto`
- `TeacherDto`
- `HomeworkDto`

规则：
- 业务名 + `Dto`
- 不用 `DTO`
- 不用 `FormDTO`
- 不用 `CreateStudentRequestDTO` 这种超长命名

### Query

用于：
- 列表查询参数
- 筛选条件

推荐：
- `StudentQuery`
- `TeacherQuery`
- `HomeworkQuery`

规则：
- 业务名 + `Query`

### Vo

用于：
- 页面展示对象
- 明确是返回视图数据时使用

推荐：
- `LoginVo`
- `UserVo`
- `HomeworkVo`

规则：
- 业务名 + `Vo`
- 如果直接返回 `Dto` 就够用，也可以不用 `Vo`

### Po

用于：
- 对应数据库表

推荐两种方案，二选一：

方案一，最简单：
- `Student`
- `Teacher`
- `Parent`
- `User`
- `Homework`

方案二，保留分层标识：
- `StudentPo`
- `TeacherPo`
- `ParentPo`
- `UserPo`
- `HomeworkPo`

当前项目建议：
- 如果你想更简单，就直接用方案一
- 如果你想一眼看出它是数据库对象，就用方案二

不要再用：
- `StudentProfilePO`
- `TeacherProfilePO`
- `UserAccountPO`

## 5. 当前项目建议统一方案

为了简单和统一，当前项目后面建议这样定：

- controller：`XxxController`
- service：`XxxService`
- service 实现：`XxxServiceImpl`
- mapper：`XxxMapper`
- 查询参数：`XxxQuery`
- 接口对象：`XxxDto`
- 页面返回：`XxxVo`
- 数据库对象：`XxxPo`

也就是统一用：
- `Dto`
- `Vo`
- `Po`

不要混用：
- `DTO`
- `VO`
- `PO`

## 6. 登录模块示例

登录模块后面推荐这样命名：

- `AuthController`
- `AuthService`
- `AuthServiceImpl`
- `AuthMapper`
- `LoginDto`
- `RegisterDto`
- `UserDto`
- `LoginVo`
- `UserPo`
- `StudentPo`
- `TeacherPo`
- `ParentPo`

## 7. 作业模块示例

- `HomeworkController`
- `HomeworkService`
- `HomeworkServiceImpl`
- `HomeworkMapper`
- `HomeworkDto`
- `HomeworkQuery`
- `HomeworkVo`
- `HomeworkPo`

## 8. 学生模块示例

- `StudentController`
- `StudentService`
- `StudentServiceImpl`
- `StudentMapper`
- `StudentDto`
- `StudentQuery`
- `StudentVo`
- `StudentPo`

## 9. 命名时的禁止项

尽量不要出现这些情况：

- 一个类名里出现 4 个以上单词
- 同时带多个后缀
- `CurrentAuthenticatedUserResponseVO`
- `TeacherHomeworkListPageQueryDTO`
- `StudentProfileEntityPO`

## 10. 以后默认执行

从现在开始，当前项目默认按这套命名：

- 短
- 直白
- 黑马风格
- 初学者一眼能看懂

如果没有特殊说明，后面我会优先给你起这种名字：

- `StudentDto`
- `TeacherDto`
- `LoginDto`
- `RegisterDto`
- `HomeworkDto`
- `StudentQuery`
- `HomeworkQuery`
- `StudentPo`
- `UserPo`

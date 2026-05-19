import type { MobilePreviewState } from '@/types/mobile'

const previewState: MobilePreviewState = {
  studentProfile: {
    id: 'student-01',
    name: '林知夏',
    school: '东湖实验小学',
    className: '三年级（2）班',
    studentNo: '20260312',
    headline: '今天还有 2 项作业需要在放学前完成。'
  },
  parentProfile: {
    id: 'parent-01',
    name: '周女士',
    mobile: '13900001234',
    school: '东湖实验小学',
    headline: '及时掌握孩子作业状态，协助完成上传与订正。'
  },
  children: [
    {
      id: 'child-01',
      name: '林知夏',
      className: '三年级（2）班',
      gradeName: '三年级',
      pendingCount: 2,
      submittedCount: 3,
      revisionCount: 1
    },
    {
      id: 'child-02',
      name: '林予安',
      className: '一年级（4）班',
      gradeName: '一年级',
      pendingCount: 1,
      submittedCount: 2,
      revisionCount: 0
    }
  ],
  activeChildId: 'child-01',
  studentHomeworks: [
    {
      id: 'hw-math-01',
      title: '数学口算闯关',
      subject: '数学',
      teacherName: '张丽',
      deadline: '今天 19:30',
      status: 'pending',
      summary: '完成练习册第 18 页，并拍照上传。',
      content:
        '请完成练习册第 18 页口算题，并在纸上列出两道你觉得最难的题的思路。拍照时保持光线明亮，图片清晰可读。',
      allowParentAssist: true,
      attachments: [
        {
          id: 'att-math-sheet',
          name: '练习要求.jpg',
          type: 'image',
          url: '/static/placeholder-sheet.png'
        }
      ],
      submitTypes: ['图片上传', '文字说明'],
      hasFeedback: false
    },
    {
      id: 'hw-chinese-01',
      title: '课文朗读录音',
      subject: '语文',
      teacherName: '李敏',
      deadline: '明天 08:00',
      status: 'revision',
      summary: '朗读《燕子》并补充停顿标记。',
      content:
        '请重新朗读《燕子》第一、二自然段，注意轻声与停顿。上传录音前先在书本上完成停顿标记并拍照。',
      allowParentAssist: true,
      attachments: [
        {
          id: 'att-chinese-mark',
          name: '停顿示例.pdf',
          type: 'file',
          url: '/static/placeholder-guide.pdf'
        }
      ],
      submitTypes: ['图片上传', '文字说明'],
      hasFeedback: true,
      latestSubmission: {
        text: '我已经重新标注停顿，并录好了朗读。',
        images: ['/static/placeholder-work-1.png'],
        submittedAt: '昨天 20:10'
      },
      review: {
        status: 'revision_required',
        level: '待订正',
        comment: '朗读速度稍快，请再放慢一些，注意“轻快地”的停顿。',
        reviewedAt: '昨天 21:00'
      }
    },
    {
      id: 'hw-science-01',
      title: '观察豆苗生长',
      subject: '科学',
      teacherName: '周岚',
      deadline: '4 月 12 日 18:00',
      status: 'submitted',
      summary: '连续三天记录豆苗变化。',
      content:
        '请把三天的观察记录整理成图文，至少包含一张豆苗照片和一句自己的发现。',
      allowParentAssist: false,
      attachments: [],
      submitTypes: ['图片上传', '文字说明'],
      hasFeedback: true,
      latestSubmission: {
        text: '豆苗第三天长高了，我觉得阳光很重要。',
        images: ['/static/placeholder-work-2.png'],
        submittedAt: '今天 08:30'
      },
      review: {
        status: 'completed',
        score: 96,
        level: 'A',
        comment: '观察认真，记录完整。',
        reviewedAt: '今天 11:10'
      }
    }
  ],
  parentHomeworks: {
    'child-01': [
      {
        id: 'hw-math-01',
        title: '数学口算闯关',
        subject: '数学',
        teacherName: '张丽',
        deadline: '今天 19:30',
        status: 'pending',
        summary: '完成练习册第 18 页，并拍照上传。',
        content:
          '请提醒孩子独立完成后再拍照上传，如有光线不足可由家长协助整理拍摄角度。',
        allowParentAssist: true,
        attachments: [],
        submitTypes: ['图片上传', '文字说明'],
        hasFeedback: false
      },
      {
        id: 'hw-chinese-01',
        title: '课文朗读录音',
        subject: '语文',
        teacherName: '李敏',
        deadline: '明天 08:00',
        status: 'revision',
        summary: '朗读《燕子》并补充停顿标记。',
        content:
          '孩子已收到老师反馈，请协助检查停顿标记是否完整后再重新提交。',
        allowParentAssist: true,
        attachments: [],
        submitTypes: ['图片上传', '文字说明'],
        hasFeedback: true,
        latestSubmission: {
          text: '我已经重新标注停顿，并录好了朗读。',
          images: ['/static/placeholder-work-1.png'],
          submittedAt: '昨天 20:10'
        },
        review: {
          status: 'revision_required',
          level: '待订正',
          comment: '朗读速度稍快，请再放慢一些，注意“轻快地”的停顿。',
          reviewedAt: '昨天 21:00'
        }
      },
      {
        id: 'hw-science-01',
        title: '观察豆苗生长',
        subject: '科学',
        teacherName: '周岚',
        deadline: '4 月 12 日 18:00',
        status: 'submitted',
        summary: '连续三天记录豆苗变化。',
        content: '已完成提交，等待老师批改。',
        allowParentAssist: false,
        attachments: [],
        submitTypes: ['图片上传', '文字说明'],
        hasFeedback: true,
        latestSubmission: {
          text: '豆苗第三天长高了，我觉得阳光很重要。',
          images: ['/static/placeholder-work-2.png'],
          submittedAt: '今天 08:30'
        },
        review: {
          status: 'completed',
          score: 96,
          level: 'A',
          comment: '观察认真，记录完整。',
          reviewedAt: '今天 11:10'
        }
      }
    ],
    'child-02': [
      {
        id: 'hw-handwriting-01',
        title: '生字描红',
        subject: '语文',
        teacherName: '陈雪',
        deadline: '今天 18:30',
        status: 'pending',
        summary: '完成本周生字描红 2 页。',
        content: '请先完成书写，再由家长协助拍照上传，注意纸张摆正。',
        allowParentAssist: true,
        attachments: [],
        submitTypes: ['图片上传'],
        hasFeedback: false
      },
      {
        id: 'hw-reading-01',
        title: '亲子阅读打卡',
        subject: '阅读',
        teacherName: '孙悦',
        deadline: '4 月 10 日 21:00',
        status: 'completed',
        summary: '阅读 20 分钟并提交一句心得。',
        content: '已提交并完成。',
        allowParentAssist: true,
        attachments: [],
        submitTypes: ['文字说明'],
        hasFeedback: true,
        latestSubmission: {
          text: '今天读了《安徒生童话》，最喜欢海的故事。',
          images: [],
          submittedAt: '今天 20:05'
        },
        review: {
          status: 'completed',
          level: '已完成',
          comment: '坚持得很好。',
          reviewedAt: '今天 20:40'
        }
      }
    ]
  },
  studentWrongBooks: [
    {
      id: 'wb-01',
      homeworkId: 'hw-math-01',
      taskId: 'task-math-01',
      reviewId: 'review-math-01',
      subjectCode: 'math',
      subjectName: '数学',
      sourceType: 'teacher_mark',
      status: 'pending_fix',
      questionNo: '1',
      questionText: '45 + 38 = ?',
      studentAnswer: '73',
      correctAnswer: '83',
      analysisText: '进位计算时漏加了十位。',
      wrongReasonCode: 'calc_error',
      wrongReasonLabel: '计算错误',
      teacherName: '张丽',
      createdAt: '2026-04-10 18:20',
      fixCount: 0,
      assets: [
        {
          id: 'wb-01-q',
          assetRole: 'question_image',
          assetType: 'image',
          assetUrl: '/static/placeholder-work-1.png',
          assetName: '第1题截图'
        }
      ]
    },
    {
      id: 'wb-02',
      homeworkId: 'hw-chinese-01',
      taskId: 'task-chinese-01',
      reviewId: 'review-chinese-01',
      subjectCode: 'chinese',
      subjectName: '语文',
      sourceType: 'student_manual',
      status: 'fixed',
      questionNo: '朗读',
      questionText: '《燕子》第一自然段停顿不够清楚。',
      studentAnswer: '朗读速度偏快，停顿不明显。',
      correctAnswer: '根据停顿符号慢速朗读。',
      analysisText: '需要再练习节奏和停顿。',
      wrongReasonCode: 'reading_error',
      wrongReasonLabel: '审题错误',
      teacherName: '李敏',
      createdAt: '2026-04-09 21:15',
      lastFixedAt: '2026-04-10 08:15',
      lastFixedText: '已经重新录音，并按照停顿符号朗读。',
      fixCount: 1,
      assets: []
    }
  ],
  studentMessages: [
    {
      id: 'msg-stu-01',
      title: '老师提醒你优先完成数学口算',
      content: '请在 19:30 前提交，图片需要清晰可见。',
      time: '10 分钟前',
      kind: 'remind',
      unread: true
    },
    {
      id: 'msg-stu-02',
      title: '语文作业收到订正意见',
      content: '朗读速度偏快，请按老师意见重新提交。',
      time: '昨天 21:05',
      kind: 'review',
      unread: true
    },
    {
      id: 'msg-stu-03',
      title: '科学作业已批改',
      content: '老师评价：观察认真，记录完整。',
      time: '今天 11:12',
      kind: 'review',
      unread: false
    }
  ],
  parentMessages: [
    {
      id: 'msg-par-01',
      title: '林知夏还有 1 项作业待订正',
      content: '请在明天上课前协助孩子重新提交语文朗读。',
      time: '昨天 21:08',
      kind: 'review',
      unread: true,
      childName: '林知夏'
    },
    {
      id: 'msg-par-02',
      title: '林予安待完成生字描红',
      content: '建议晚饭后先完成书写，再协助上传作业图片。',
      time: '20 分钟前',
      kind: 'assignment',
      unread: true,
      childName: '林予安'
    },
    {
      id: 'msg-par-03',
      title: '科学作业已完成批改',
      content: '老师给出 A 级评价，可以查看详细反馈。',
      time: '今天 11:15',
      kind: 'review',
      unread: false,
      childName: '林知夏'
    }
  ]
}

export function createPreviewState(): MobilePreviewState {
  return JSON.parse(JSON.stringify(previewState)) as MobilePreviewState
}

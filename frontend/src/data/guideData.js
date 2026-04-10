export const guideData = {
  '/elderly/home': {
    title: '首页使用指南',
    steps: [
      { icon: '🏠', text: '点击"活动中心"卡片，查看社区活动' },
      { icon: '🤝', text: '点击"社区服务"卡片，预约社区服务' },
      { icon: '❤️', text: '点击"健康记录"卡片，查看健康数据' },
      { icon: '🏛️', text: '点击"场地预约"卡片，预约活动场地' },
      { icon: '📰', text: '点击"新闻资讯"卡片，了解社区动态' }
    ],
    tips: '首页是您使用系统的起点，从这里可以快速访问所有功能'
  },
  '/elderly/activities': {
    title: '活动页面使用指南',
    steps: [
      { icon: '📋', text: '浏览活动列表，查看所有可报名的活动' },
      { icon: '👁', text: '点击活动卡片，查看活动详细信息' },
      { icon: '✅', text: '点击"报名参加"按钮，完成活动报名' },
      { icon: '❌', text: '点击"取消报名"按钮，取消已报名的活动' },
      { icon: '🔍', text: '使用分页器，浏览更多活动' }
    ],
    tips: '活动报名需要在活动开始前一周到活动结束前一小时之间进行'
  },
  '/elderly/activities/:id': {
    title: '活动详情使用指南',
    steps: [
      { icon: '📖', text: '查看活动标题、内容、时间、地点等详细信息' },
      { icon: '👥', text: '查看活动参与人数和剩余名额' },
      { icon: '💬', text: '在评论区查看其他用户的评论' },
      { icon: '⭐', text: '点击"收藏"按钮，收藏感兴趣的活动' },
      { icon: '🔙', text: '点击"返回"按钮，返回活动列表' }
    ],
    tips: '活动详情页展示活动的完整信息，包括活动描述、时间安排等'
  },
  '/elderly/services': {
    title: '服务页面使用指南',
    steps: [
      { icon: '📋', text: '浏览服务列表，查看所有可预约的服务' },
      { icon: '', text: '点击服务卡片，查看服务详情' },
      { icon: '📅', text: '选择预约日期和时间' }
    ],
    tips: '服务分为生活服务、健康服务、文化服务等不同类别'
  },
  '/elderly/services/:id': {
    title: '服务详情使用指南',
    steps: [
      { icon: '📖', text: '查看服务名称、描述、价格、时长等信息' },
      { icon: '⏰', text: '选择预约日期和时间段' },
      { icon: '📝', text: '填写预约备注信息' },
      { icon: '💳', text: '点击"立即预约"按钮，提交预约申请' },
      { icon: '⭐', text: '点击"收藏"按钮，收藏该服务' }
    ],
    tips: '预约成功后，请在预约时间前做好准备'
  },
  '/elderly/venues': {
    title: '场地页面使用指南',
    steps: [
      { icon: '📋', text: '浏览场地列表，查看所有可预约的场地' },
      { icon: '👁', text: '点击场地卡片，查看场地详细信息' },
      { icon: '📅', text: '查看场地开放时间和容纳人数' },
      { icon: '🔧', text: '查看场地设备信息' }
    ],
    tips: '场地状态分为可预约、维护中、不可预约三种状态'
  },
  '/elderly/venues/:id': {
    title: '场地详情使用指南',
    steps: [
      { icon: '📖', text: '查看场地名称、描述、容量等信息' },
      { icon: '📅', text: '查看场地开放时间' },
      { icon: '📋', text: '查看场地预约时间表' },
      { icon: '➕', text: '点击"预约场地"按钮，选择预约时间' },
      { icon: '📝', text: '填写预约用途和参与人数' },
      { icon: '✅', text: '点击"提交预约"按钮，完成预约' }
    ],
    tips: '预约提交后需要等待工作人员审核'
  },
  '/elderly/venues/:id/schedule': {
    title: '场地预约时间表使用指南',
    steps: [
      { icon: '📅', text: '查看场地的预约时间安排' },
      { icon: '🟢', text: '绿色表示该时间段可预约' },
      { icon: '🔴', text: '红色表示该时间段已被预约' },
      { icon: '🟡', text: '黄色表示该时间段维护中' },
      { icon: '🔙', text: '点击"返回"按钮，返回场地详情' }
    ],
    tips: '请选择绿色时间段进行预约'
  },
  '/elderly/news': {
    title: '新闻页面使用指南',
    steps: [
      { icon: '📋', text: '浏览新闻列表，查看社区最新动态' },
      { icon: '👁', text: '点击新闻卡片，查看新闻详情' },
      { icon: '🏷', text: '新闻分为新闻、公告、政策解读三种类型' },
      { icon: '🔍', text: '使用分页器，浏览更多新闻' }
    ],
    tips: '新闻按发布时间倒序排列，最新的新闻显示在最前面'
  },
  '/elderly/news/:id': {
    title: '新闻详情使用指南',
    steps: [
      { icon: '📖', text: '阅读新闻标题和完整内容' },
      { icon: '👤', text: '查看新闻发布者信息' },
      { icon: '📅', text: '查看新闻发布时间' },
      { icon: '👀', text: '查看新闻浏览次数' },
      { icon: '💬', text: '在评论区发表评论或查看其他评论' },
      { icon: '🔙', text: '点击"返回"按钮，返回新闻列表' }
    ],
    tips: '您可以对新闻进行评论，与其他用户交流'
  },
  '/elderly/health': {
    title: '健康记录使用指南',
    steps: [
      { icon: '📋', text: '查看您的健康记录列表' },
      { icon: '👁', text: '点击"查看详情"按钮，查看完整健康报告' },
      { icon: '📊', text: '健康报告分为基础项目、血常规、肝功能等' },
      { icon: '🟢', text: '绿色表示指标正常' },
      { icon: '🔴', text: '红色表示指标异常' },
      { icon: '🔍', text: '使用分页器，查看更多记录' }
    ],
    tips: '健康记录由社区工作人员录入，如有疑问请联系工作人员'
  },
  '/elderly/bookings': {
    title: '我的预约使用指南',
    steps: [
      { icon: '🏷', text: '点击"活动报名"、"服务预约"、"场地预约"标签切换不同类型' },
      { icon: '📋', text: '查看对应类型的预约列表' },
      { icon: '👁', text: '点击预约项，查看预约详情' },
      { icon: '❌', text: '点击"取消预约"按钮，取消不需要的预约' },
      { icon: '💳', text: '查看预约支付状态' }
    ],
    tips: '我的预约页面集中管理您所有的预约记录'
  },
  '/elderly/favorites': {
    title: '我的收藏使用指南',
    steps: [
      { icon: '🏷', text: '点击"活动"或"服务"标签切换收藏类型' },
      { icon: '📋', text: '浏览收藏列表，查看所有收藏内容' },
      { icon: '👁', text: '点击收藏项，跳转到对应详情页' },
      { icon: '❤️', text: '点击"❤️"按钮，取消收藏' },
      { icon: '🔍', text: '使用分页器，查看更多收藏' }
    ],
    tips: '收藏后可以快速找到感兴趣的活动和服务'
  },
  '/elderly/profile': {
    title: '个人中心使用指南',
    steps: [
      { icon: '👤', text: '查看和编辑个人基本信息（姓名、手机号等）' },
      { icon: '📱', text: '修改手机号，方便接收通知' },
      { icon: '🏠', text: '修改家庭住址' },
      { icon: '📞', text: '修改紧急联系人信息' },
      { icon: '🖼️', text: '点击头像区域，上传或更换头像' },
      { icon: '💾', text: '点击"保存"按钮，保存修改的信息' }
    ],
    tips: '个人中心用于管理您的个人信息，请保持信息准确'
  },
  '/elderly/payment': {
    title: '支付页面使用指南',
    steps: [
      { icon: '📖', text: '查看订单信息（服务名称、预约时间、金额）' },
      { icon: '💳', text: '选择支付方式（支付宝等）' },
      { icon: '✅', text: '点击"立即支付"按钮，完成支付' },
      { icon: '⏰', text: '请在30分钟内完成支付，否则订单会自动取消' },
      { icon: '🔙', text: '支付完成后，点击"返回"按钮' }
    ],
    tips: '支付成功后，您可以在"我的预约"中查看预约状态'
  }
}

export const getGuideByPath = (path) => {
  const keys = Object.keys(guideData)
  keys.sort((a, b) => b.length - a.length)
  for (const key of keys) {
    const pattern = key.replace(/:\w+/g, '\\d+')
    const regex = new RegExp(`^${pattern}$`)
    if (regex.test(path)) {
      return guideData[key]
    }
  }
  return null
}

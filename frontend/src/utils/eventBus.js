import mitt from 'mitt'

/**
 * 事件总线
 * 用于组件间的事件通信，基于mitt库实现
 * 提供发布-订阅模式，实现跨组件的数据传递
 */
const emitter = mitt()

export default emitter

package com.sudooom.mahjong.broker.session

/**
 * 服务状态枚举
 * 简化版本，只有上线和下线两种状态
 */
enum class ServiceStatus {
    /**
     * 在线
     * 服务正常运行，可以接收请求
     */
    ONLINE,

    /**
     * 离线
     * 服务已断开连接，不参与任何请求处理
     */
    OFFLINE;

    /**
     * 判断服务是否可以接收新请求
     */
    fun canAcceptNewRequests(): Boolean = this == ONLINE

    /**
     * 判断服务是否可以参与路由选择
     */
    fun isRoutable(): Boolean = this == ONLINE
}

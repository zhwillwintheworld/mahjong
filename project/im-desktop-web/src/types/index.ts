/**
 * 消息类型
 */
export interface Message {
    id: string
    fromUserId: string
    toUserId?: string
    groupId?: string
    type: MessageType
    content: string
    timestamp: number
    ext?: Record<string, string>
}

export enum MessageType {
    TEXT = 0,
    IMAGE = 1,
    FILE = 2,
    VOICE = 3,
    VIDEO = 4,
    SYSTEM = 5,
    HEARTBEAT = 6,
}

/**
 * 用户信息
 */
export interface User {
    userId: string
    username: string
    avatar?: string
    nickname?: string
    status: UserStatus
}

export enum UserStatus {
    OFFLINE = 0,
    ONLINE = 1,
    BUSY = 2,
    AWAY = 3,
}

/**
 * 联系人
 */
export interface Contact {
    userId: string
    username: string
    avatar?: string
    lastMessage?: Message
    unreadCount: number
}

/**
 * 群组
 */
export interface Group {
    groupId: string
    name: string
    avatar?: string
    members: User[]
    lastMessage?: Message
    unreadCount: number
}

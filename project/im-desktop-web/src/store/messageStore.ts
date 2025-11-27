import { create } from 'zustand'
import { Message, Contact } from '../types'

interface MessageState {
    messages: Record<string, Message[]> // userId/groupId -> messages
    contacts: Contact[]
    currentChat: string | null // 当前聊天对象 ID
    addMessage: (chatId: string, message: Message) => void
    setMessages: (chatId: string, messages: Message[]) => void
    setContacts: (contacts: Contact[]) => void
    setCurrentChat: (chatId: string | null) => void
}

export const useMessageStore = create<MessageState>((set) => ({
    messages: {},
    contacts: [],
    currentChat: null,

    addMessage: (chatId, message) =>
        set((state) => ({
            messages: {
                ...state.messages,
                [chatId]: [...(state.messages[chatId] || []), message],
            },
        })),

    setMessages: (chatId, messages) =>
        set((state) => ({
            messages: {
                ...state.messages,
                [chatId]: messages,
            },
        })),

    setContacts: (contacts) => set({ contacts }),
    setCurrentChat: (chatId) => set({ currentChat: chatId }),
}))

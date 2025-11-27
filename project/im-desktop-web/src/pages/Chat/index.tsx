import { Layout } from 'antd'
import './style.css'

const { Header, Sider, Content } = Layout

const Chat = () => {
    return (
        <Layout className="chat-container">
            <Sider width={300} theme="light" className="contact-sider">
                <div className="contact-header">
                    <h2>联系人</h2>
                </div>
                <div className="contact-list">
                    {/* TODO: 联系人列表组件 */}
                    <p style={{ padding: '20px', color: '#999' }}>暂无联系人</p>
                </div>
            </Sider>
            <Layout>
                <Header className="chat-header">
                    <h2>聊天窗口</h2>
                </Header>
                <Content className="chat-content">
                    <div className="message-list">
                        {/* TODO: 消息列表组件 */}
                        <p style={{ textAlign: 'center', color: '#999', marginTop: '100px' }}>
                            选择联系人开始聊天
                        </p>
                    </div>
                    <div className="message-input">
                        {/* TODO: 消息输入组件 */}
                    </div>
                </Content>
            </Layout>
        </Layout>
    )
}

export default Chat

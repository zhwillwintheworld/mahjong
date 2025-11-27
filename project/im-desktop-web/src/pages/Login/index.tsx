import { useState } from 'react'
import { Form, Input, Button, Card, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useUserStore } from '../../store/userStore'
import './style.css'

interface LoginForm {
    username: string
    password: string
}

const Login = () => {
    const navigate = useNavigate()
    const { setUser, setToken } = useUserStore()
    const [loading, setLoading] = useState(false)

    const onFinish = async (values: LoginForm) => {
        setLoading(true)
        try {
            // TODO: 调用登录 API
            console.log('登录:', values)

            // 模拟登录成功
            setTimeout(() => {
                setToken('mock-token')
                setUser({
                    userId: '1',
                    username: values.username,
                    status: 1,
                })
                message.success('登录成功')
                navigate('/chat')
            }, 1000)
        } catch (error) {
            message.error('登录失败')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-container">
            <Card className="login-card" title="IM 即时通讯">
                <Form
                    name="login"
                    onFinish={onFinish}
                    autoComplete="off"
                    size="large"
                >
                    <Form.Item
                        name="username"
                        rules={[{ required: true, message: '请输入用户名' }]}
                    >
                        <Input prefix={<UserOutlined />} placeholder="用户名" />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[{ required: true, message: '请输入密码' }]}
                    >
                        <Input.Password prefix={<LockOutlined />} placeholder="密码" />
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={loading} block>
                            登录
                        </Button>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    )
}

export default Login

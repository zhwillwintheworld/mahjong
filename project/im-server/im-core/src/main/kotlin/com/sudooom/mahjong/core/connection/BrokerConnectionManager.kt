package com.sudooom.mahjong.core.connection

import com.sudooom.mahjong.common.log.Loggable
import com.sudooom.mahjong.core.config.BrokerConnectionProperties
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Component
import reactor.core.Disposable

/** Broker 连接管理器 负责管理与 Broker 的 RSocket 连接 */
@Component
class BrokerConnectionManager(
        private val brokerRSocketRequester: RSocketRequester,
        private val properties: BrokerConnectionProperties,
) : Loggable {

    private val connected = AtomicBoolean(false)
    private val disposableRef = AtomicReference<Disposable?>()

    /** 应用启动时自动连接 Broker */
    @PostConstruct
    fun connect() {
        logger.info("Connecting to Broker at ${properties.host}:${properties.port}...")

        val disposable =
                brokerRSocketRequester
                        .rsocketClient()
                        .source()
                        .doOnNext { rSocket ->
                            connected.set(true)
                            logger.info(
                                    "Successfully connected to Broker at ${properties.host}:${properties.port}"
                            )

                            // 监听连接关闭事件
                            rSocket.onClose()
                                    .doOnTerminate {
                                        connected.set(false)
                                        logger.warn("Connection to Broker closed")
                                    }
                                    .subscribe()
                        }
                        .doOnError { error ->
                            connected.set(false)
                            logger.error("Failed to connect to Broker: ${error.message}", error)
                        }
                        .subscribe()

        disposableRef.set(disposable)
    }

    /** 应用关闭时断开连接 */
    @PreDestroy
    fun disconnect() {
        logger.info("Disconnecting from Broker...")
        disposableRef.get()?.dispose()
        connected.set(false)
    }

    /** 检查是否已连接到 Broker */
    fun isConnected(): Boolean = connected.get()

    /** 获取 RSocket Requester 用于发送消息 */
    fun getRequester(): RSocketRequester = brokerRSocketRequester
}

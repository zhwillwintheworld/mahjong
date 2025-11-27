package com.sudooom.mahjong.common.util

import java.util.concurrent.atomic.AtomicLong

/**
 * 雪花算法 ID 生成器
 * Twitter Snowflake ID 生成器
 */
class SnowflakeIdGenerator(
    private val workerId: Long = 1,
    private val datacenterId: Long = 1
) {
    companion object {
        private const val EPOCH = 1609459200000L // 2021-01-01 00:00:00
        
        private const val WORKER_ID_BITS = 5L
        private const val DATACENTER_ID_BITS = 5L
        private const val SEQUENCE_BITS = 12L
        
        private const val MAX_WORKER_ID = -1L xor (-1L shl WORKER_ID_BITS.toInt())
        private const val MAX_DATACENTER_ID = -1L xor (-1L shl DATACENTER_ID_BITS.toInt())
        
        private const val WORKER_ID_SHIFT = SEQUENCE_BITS
        private const val DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS
        private const val TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS
        
        private const val SEQUENCE_MASK = -1L xor (-1L shl SEQUENCE_BITS.toInt())
    }
    
    private val sequence = AtomicLong(0L)
    private var lastTimestamp = -1L
    
    init {
        require(workerId in 0..MAX_WORKER_ID) { "Worker ID 超出范围" }
        require(datacenterId in 0..MAX_DATACENTER_ID) { "Datacenter ID 超出范围" }
    }
    
    @Synchronized
    fun nextId(): Long {
        var timestamp = timeGen()
        
        if (timestamp < lastTimestamp) {
            throw RuntimeException("时钟回拨，拒绝生成 ID")
        }
        
        if (timestamp == lastTimestamp) {
            val seq = sequence.incrementAndGet() and SEQUENCE_MASK
            if (seq == 0L) {
                timestamp = tilNextMillis(lastTimestamp)
            }
            sequence.set(seq)
        } else {
            sequence.set(0L)
        }
        
        lastTimestamp = timestamp
        
        return ((timestamp - EPOCH) shl TIMESTAMP_LEFT_SHIFT.toInt()) or
                (datacenterId shl DATACENTER_ID_SHIFT.toInt()) or
                (workerId shl WORKER_ID_SHIFT.toInt()) or
                sequence.get()
    }
    
    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = timeGen()
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen()
        }
        return timestamp
    }
    
    private fun timeGen(): Long = System.currentTimeMillis()
}

// 全局单例
object IdGenerator {
    private val generator = SnowflakeIdGenerator()
    
    fun nextId(): Long = generator.nextId()
    
    fun nextIdString(): String = nextId().toString()
}

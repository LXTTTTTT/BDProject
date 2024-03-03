package com.bdtx.mod_util.Utils

import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

// 线程池工具（不用这个，用另一个）
object ThreadPoolManagementUtils {
    private val corePoolSize = 5
    private val maximumPoolSize = 10
    private val keepAliveTime = 60L
    private val timeUnit = TimeUnit.SECONDS
    private val workQueue = LinkedBlockingQueue<Runnable>()
    private val threadFactory = Executors.defaultThreadFactory()
    private val rejectionHandler = ThreadPoolExecutor.AbortPolicy()
    private val threadPoolExecutor = ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, workQueue, threadFactory, rejectionHandler)

    fun executeTask(task: Runnable) {
        threadPoolExecutor.execute(task)
    }

    fun shutdown() {
        threadPoolExecutor.shutdown()
    }

    fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        return threadPoolExecutor.awaitTermination(timeout, unit)
    }

}
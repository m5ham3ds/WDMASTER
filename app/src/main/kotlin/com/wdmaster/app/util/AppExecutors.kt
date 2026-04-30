package com.wdmaster.app.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AppExecutors(
    val diskIO: ExecutorService = Executors.newSingleThreadExecutor(),
    val networkIO: ExecutorService = Executors.newFixedThreadPool(3)
) {
    fun shutdown() {
        diskIO.shutdown()
        networkIO.shutdown()
    }
}

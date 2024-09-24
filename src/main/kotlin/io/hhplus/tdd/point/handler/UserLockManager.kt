package io.hhplus.tdd.point.handler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

class UserLockManager {

    private val userLocks = ConcurrentHashMap<Long, ReentrantLock>()

    fun getLockForUser(userId: Long): ReentrantLock {
        // 유저별로 고유한 락을 할당
        return userLocks.computeIfAbsent(userId) { ReentrantLock() }
    }
}
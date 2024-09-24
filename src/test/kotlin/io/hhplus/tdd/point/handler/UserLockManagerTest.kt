package io.hhplus.tdd.point.handler

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class UserLockManagerTest {

    private val userLockManager = UserLockManager()

    @Test
    @DisplayName("동일한 유저에게 동일한 락 생성되는지 테스트")
    fun getUserLock() {
        // Given
        val userId = 1L

        // When
        val lock1 = userLockManager.getLockForUser(userId)
        val lock2 = userLockManager.getLockForUser(userId)

        // Then
        assertTrue(lock1 === lock2)
    }
}
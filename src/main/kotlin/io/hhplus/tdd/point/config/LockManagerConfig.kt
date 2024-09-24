package io.hhplus.tdd.point.config

import io.hhplus.tdd.point.handler.UserLockManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LockManagerConfig {

    @Bean
    fun userLockManager(): UserLockManager {
        return UserLockManager()
    }
}
package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.handler.UserLockManager
import io.hhplus.tdd.point.service.domain.PointHistory
import io.hhplus.tdd.point.service.domain.TransactionType
import io.hhplus.tdd.point.service.domain.UserPoint
import org.springframework.stereotype.Service
import kotlin.concurrent.withLock

@Service
class PointServiceImpl(
    val pointHistoryTable: PointHistoryTable,
    val userPointTable: UserPointTable,
    val userLockManager: UserLockManager,
): PointService {

    override fun getUserPoint(id: Long): UserPoint {
        return userPointTable.selectById(id)
    }

    override fun getUserPointHistory(id: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(id)
    }

    override fun savePoint(userPoint: UserPoint): UserPoint {
        val lock = userLockManager.getLockForUser(userPoint.id)

        lock.withLock {
            val currentUserPoint = userPointTable.selectById(userPoint.id)
            val saveUserPoint = currentUserPoint.savePoint(userPoint.point)

            userPointTable.insertOrUpdate(saveUserPoint.id, saveUserPoint.point)
            pointHistoryTable.insert(saveUserPoint.id, userPoint.point, TransactionType.CHARGE, saveUserPoint.updateMillis)

            return saveUserPoint
        }
    }

    override fun usePoint(userPoint: UserPoint): UserPoint {
        val lock = userLockManager.getLockForUser(userPoint.id)

        lock.withLock {
            val currentUserPoint = userPointTable.selectById(userPoint.id)
            val saveUserPoint = currentUserPoint.usePoint(userPoint.point)

            userPointTable.insertOrUpdate(saveUserPoint.id, saveUserPoint.point)
            pointHistoryTable.insert(saveUserPoint.id, userPoint.point, TransactionType.USE, saveUserPoint.updateMillis)

            return saveUserPoint
        }
    }
}
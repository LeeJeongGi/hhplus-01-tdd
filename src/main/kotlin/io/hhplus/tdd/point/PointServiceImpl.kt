package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UserPointResponse
import io.hhplus.tdd.point.handler.UserLockManager
import org.springframework.stereotype.Service
import kotlin.concurrent.withLock

@Service
class PointServiceImpl(
    val pointHistoryTable: PointHistoryTable,
    val userPointTable: UserPointTable,
    val userLockManager: UserLockManager,
): PointService {

    override fun getUserPoint(id: Long): UserPointResponse {
        return userPointTable.selectById(id).convertDto()
    }

    override fun getUserPointHistory(id: Long): List<PointHistoryResponse> {
        val userPointHistory = pointHistoryTable.selectAllByUserId(id)
        return userPointHistory.map { it.convertDto() }
    }

    override fun savePoint(pointRequest: PointRequest): UserPointResponse {
        val lock = userLockManager.getLockForUser(pointRequest.userId)

        lock.withLock {
            pointRequest.checkPointValidity(pointRequest.amount)

            val currentUserPoint = userPointTable.selectById(pointRequest.userId)
            currentUserPoint.validateMaxPointBalance(pointRequest.amount)

            val saveUserPoint = userPointTable.insertOrUpdate(currentUserPoint.id, currentUserPoint.point + pointRequest.amount)
            pointHistoryTable.insert(saveUserPoint.id, pointRequest.amount, TransactionType.CHARGE, saveUserPoint.updateMillis)

            return saveUserPoint.convertDto()
        }
    }

    override fun usePoint(pointRequest: PointRequest): UserPointResponse {
        val lock = userLockManager.getLockForUser(pointRequest.userId)

        lock.withLock {
            val currentUserPoint = userPointTable.selectById(pointRequest.userId)
            currentUserPoint.validateSufficientPoints(pointRequest.amount)

            val saveUserPoint = userPointTable.insertOrUpdate(currentUserPoint.id, currentUserPoint.point - pointRequest.amount)
            pointHistoryTable.insert(saveUserPoint.id, pointRequest.amount, TransactionType.USE, saveUserPoint.updateMillis)

            return saveUserPoint.convertDto()
        }
    }
}
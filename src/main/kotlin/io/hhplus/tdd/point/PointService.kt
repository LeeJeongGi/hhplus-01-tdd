package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import org.springframework.stereotype.Service

@Service
class PointService(
    val pointHistoryTable: PointHistoryTable,
    val userPointTable: UserPointTable,
) {

    fun getUserPoint(id: Long): UserPointResponse {
        return userPointTable.selectById(id).convertDto()
    }

    fun getUserPointHistory(id: Long): List<PointHistoryResponse> {
        val userPointHistory = pointHistoryTable.selectAllByUserId(id)
        userPointHistory.map { it.convertDto() }
        return userPointHistory.map { it.convertDto() }
    }

    @Synchronized
    fun savePoint(id: Long, amount: Long): UserPointResponse {

        val currentUserPoint = userPointTable.selectById(id)

        val saveUserPoint = userPointTable.insertOrUpdate(currentUserPoint.id, currentUserPoint.point + amount)
        pointHistoryTable.insert(saveUserPoint.id, saveUserPoint.point, TransactionType.CHARGE, System.currentTimeMillis())

        return saveUserPoint.convertDto()
    }

    @Synchronized
    fun usePoint(id: Long, amount: Long): UserPointResponse {

        val currentUserPoint = userPointTable.selectById(id)
        currentUserPoint.validateSufficientPoints(amount)

        val saveUserPoint = userPointTable.insertOrUpdate(currentUserPoint.id, currentUserPoint.point - amount)
        pointHistoryTable.insert(saveUserPoint.id, saveUserPoint.point, TransactionType.USE, System.currentTimeMillis())

        return saveUserPoint.convertDto()
    }
}
package io.hhplus.tdd.point

import io.hhplus.tdd.point.dto.PointHistoryResponse

data class PointHistory(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
) {
    fun convertDto(): PointHistoryResponse {
        return PointHistoryResponse(
            id = this.id,
            userId = this.userId,
            type = this.type,
            amount = this.amount,
            timeMillis = this.timeMillis,
        )
    }
}

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
enum class TransactionType {
    CHARGE, USE
}
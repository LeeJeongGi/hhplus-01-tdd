package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.TransactionType

data class PointHistoryResponse(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
)
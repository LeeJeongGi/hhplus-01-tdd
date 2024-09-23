package io.hhplus.tdd.point.dto

data class UserPointResponse(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
}
package io.hhplus.tdd.point

import io.hhplus.tdd.point.dto.UserPointResponse

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    private val MAX_POINT_BALANCE = 1_000_000L

    fun validateSufficientPoints(amount: Long) {
        if (this.point < amount) {
            throw IllegalArgumentException("사용할 수 있는 포인트가 부족합니다. 현재 포인트: ${this.point}, 요청한 포인트: $amount")
        }
    }

    fun validateMaxPointBalance(amount: Long) {
        if (this.point + amount > MAX_POINT_BALANCE) {
            throw IllegalArgumentException("최대 포인트는 ${MAX_POINT_BALANCE}원입니다. 현재 포인트: ${this.point}, 요청한 포인트: $amount")
        }
    }

    fun convertDto(): UserPointResponse {
        return UserPointResponse(
            id = this.id,
            point = this.point,
            updateMillis = this.updateMillis,
        )
    }
}

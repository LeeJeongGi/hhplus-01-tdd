package io.hhplus.tdd.point.service.domain

import io.hhplus.tdd.point.dto.UserPointResponse

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    private val MAX_POINT_BALANCE = 1_000_000L

    companion object {
        fun of(id: Long, point: Long): UserPoint {
            return UserPoint(id, point, System.currentTimeMillis())
        }
    }

    fun convertDto(): UserPointResponse {
        return UserPointResponse(
            id = this.id,
            point = this.point,
            updateMillis = this.updateMillis,
        )
    }

    fun savePoint(point: Long): UserPoint {
        if (point < 0) {
            throw IllegalArgumentException("잘못된 충전 요청: 충전 금액은 0 이상이어야 합니다. 요청한 금액: $point")
        }

        if (this.point + point > MAX_POINT_BALANCE) {
            throw IllegalArgumentException("최대 포인트는 ${MAX_POINT_BALANCE}원입니다. 현재 포인트: ${this.point}, 요청한 포인트: $point")
        }

        return UserPoint(this.id, this.point + point, System.currentTimeMillis())
    }

    fun usePoint(point: Long): UserPoint {
        if (this.point < point) {
            throw IllegalArgumentException("사용할 수 있는 포인트가 부족합니다. 현재 포인트: ${this.point}, 요청한 포인트: $point")
        }

        return UserPoint(this.id, this.point - point, System.currentTimeMillis())
    }
}

package io.hhplus.tdd.point.dto

data class PointRequest(
    val userId: Long,
    val amount: Long,
) {
    fun checkPointValidity(point: Long) {
        if (point < 0) {
            throw IllegalArgumentException("잘못된 충전 요청: 충전 금액은 0 이상이어야 합니다. 요청한 금액: $point")
        }
    }
}
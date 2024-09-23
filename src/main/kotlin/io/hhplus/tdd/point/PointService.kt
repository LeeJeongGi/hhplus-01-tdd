package io.hhplus.tdd.point

import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.UserPointResponse

interface PointService {
    fun getUserPoint(id: Long): UserPointResponse

    fun getUserPointHistory(id: Long): List<PointHistoryResponse>

    fun savePoint(id: Long, amount: Long): UserPointResponse

    fun usePoint(id: Long, amount: Long): UserPointResponse
}
package io.hhplus.tdd.point

import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UserPointResponse

interface PointService {
    fun getUserPoint(id: Long): UserPointResponse

    fun getUserPointHistory(id: Long): List<PointHistoryResponse>

    fun savePoint(pointRequest: PointRequest): UserPointResponse

    fun usePoint(pointRequest: PointRequest): UserPointResponse
}
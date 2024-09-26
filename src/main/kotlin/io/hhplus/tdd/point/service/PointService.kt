package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.service.domain.PointHistory
import io.hhplus.tdd.point.service.domain.UserPoint

interface PointService {
    fun getUserPoint(id: Long): UserPoint

    fun getUserPointHistory(id: Long): List<PointHistory>

    fun savePoint(userPoint: UserPoint): UserPoint

    fun usePoint(userPoint: UserPoint): UserPoint
}
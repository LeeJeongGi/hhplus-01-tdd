package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import io.hhplus.tdd.point.service.PointService
import io.hhplus.tdd.point.service.domain.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    val pointService: PointService
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPointResponse {
        return pointService.getUserPoint(id).convertDto()
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistoryResponse> {
        val userPointHistory = pointService.getUserPointHistory(id)
        return userPointHistory.map { it.convertDto() }
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPointResponse {
        return pointService.savePoint(UserPoint.of(id, amount)).convertDto()
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPointResponse {
        return pointService.usePoint(UserPoint.of(id, amount)).convertDto()
    }
}
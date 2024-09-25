package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.handler.UserLockManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
class PointServiceImplTest {

    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointService: PointServiceImpl

    @BeforeEach
    fun setUp() {
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()

        pointService = PointServiceImpl(pointHistoryTable, userPointTable, userLockManager = UserLockManager())
    }

    @Test
    @DisplayName("유저의 포인트를 조회")
    fun getUserPointTest() {
        // given
        userPointTable.insertOrUpdate(1L, 300L)

        // when
        val userPoint = pointService.getUserPoint(1L)

        // then
        assertThat(userPoint).isNotNull
        assertThat(userPoint.id).isEqualTo(1L)
        assertThat(userPoint.point).isEqualTo(300L)
    }

    @Test
    @DisplayName("저장이 아직 안된 유저의 포인트를 조회")
    fun getDoNotSaveUserPointTest() {
        // when
        val userPoint = pointService.getUserPoint(1L)

        // then
        assertThat(userPoint).isNotNull
        assertThat(userPoint.id).isEqualTo(1L)
        assertThat(userPoint.point).isEqualTo(0)
    }

    @Test
    @DisplayName("유저의 히스토리를 조회")
    fun getUserPointHistoryTest() {
        // given
        val history1 = pointHistoryTable.insert(2L, 300L, TransactionType.CHARGE, System.currentTimeMillis())
        val history2 = pointHistoryTable.insert(2L, 400L, TransactionType.CHARGE, System.currentTimeMillis())
        val history3 = pointHistoryTable.insert(2L, 500L, TransactionType.USE, System.currentTimeMillis())

        // when
        val userPointHistory = pointService.getUserPointHistory(2L)

        // then
        assertThat(userPointHistory).hasSize(3)
        assertThat(userPointHistory[0].userId).isEqualTo(2L)
        assertThat(userPointHistory[0].amount).isEqualTo(300L)
        assertThat(userPointHistory[0].type).isEqualTo(TransactionType.CHARGE)

        assertThat(userPointHistory[1].userId).isEqualTo(2L)
        assertThat(userPointHistory[1].amount).isEqualTo(400L)
        assertThat(userPointHistory[1].type).isEqualTo(TransactionType.CHARGE)

        assertThat(userPointHistory[2].userId).isEqualTo(2L)
        assertThat(userPointHistory[2].amount).isEqualTo(500L)
        assertThat(userPointHistory[2].type).isEqualTo(TransactionType.USE)
    }

    @Test
    @DisplayName("포인트 저장 테스트")

    fun savePointTest() {
        // given
        val pointRequest = PointRequest(1L, 400L)

        // when
        val saveUserPoint = pointService.savePoint(pointRequest)

        // then
        assertThat(saveUserPoint.id).isEqualTo(pointRequest.userId)
        assertThat(saveUserPoint.point).isEqualTo(pointRequest.amount)
    }

    @Test
    @DisplayName("포인트 저장 시 기존 포인트에 누적되는지 검증하는 테스트")
    fun savePlusPointTest() {
        // given
        val pointRequest = PointRequest(1L, 400L)
        val saveUserPoint = pointService.savePoint(pointRequest)

        // when
        val curUserPoint = pointService.savePoint(PointRequest(saveUserPoint.id, amount = saveUserPoint.point + 100L))

        // then
        assertThat(curUserPoint.id).isEqualTo(pointRequest.userId)
        assertThat(curUserPoint.point).isEqualTo(2 * saveUserPoint.point + 100L)
    }

    @Test
    @DisplayName("포인트 저장 실패 테스트 - 백만원 보다 넘게 가지면 안되는 포인트 정책 위반 하는 경우")
    fun savePointOverMaxBalanceExceptionTest() {
        // given
        userPointTable.insertOrUpdate(1L, 999000L)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            pointService.savePoint(PointRequest(1L, 1001L))
        }.message

        assertThat(message).isEqualTo("최대 포인트는 1000000원입니다. 현재 포인트: 999000, 요청한 포인트: 1001")
    }

    @Test
    @DisplayName("포인트 사용 실패 테스트 - 가진 포인트 보다 많은 포인트를 사용하려고 하는 경우")
    fun usePointTest() {
        // given
        userPointTable.insertOrUpdate(1L, 500L)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            pointService.usePoint(PointRequest(1L, 1000L))
        }.message

        assertThat(message).isEqualTo("사용할 수 있는 포인트가 부족합니다. 현재 포인트: 500, 요청한 포인트: 1000")
    }

    @Test
    @DisplayName("포인트 충전과 사용이 순차적으로 처리되는지 테스트")
    fun executeSequentiallyOnConcurrentUsage() {
        // given
        val userId = 1L
        val initialAmount = 1000L
        val requestAmount1 = 500L
        val requestAmount2 = 300L

        // 포인트 초기 상태 설정
        pointService.savePoint(PointRequest(userId, initialAmount))

        val executor = Executors.newFixedThreadPool(2)
        val thread1 = CompletableFuture.supplyAsync({
            pointService.savePoint(PointRequest(userId, requestAmount1))
        }, executor)

        val thread2 = CompletableFuture.supplyAsync({
            pointService.usePoint(PointRequest(userId, requestAmount2))
        }, executor)

        // when
        CompletableFuture.allOf(thread1, thread2).join()

        // then
        val finalPoint = pointService.getUserPoint(userId)
        assertThat(finalPoint.point).isEqualTo(initialAmount + requestAmount1 - requestAmount2)

        val userPointHistory = pointService.getUserPointHistory(userId)
        assertThat(userPointHistory[0].amount).isEqualTo(1000L)
        assertThat(userPointHistory[0].type).isEqualTo(TransactionType.CHARGE)

        assertThat(userPointHistory[1].amount).isEqualTo(500L)
        assertThat(userPointHistory[1].type).isEqualTo(TransactionType.CHARGE)

        assertThat(userPointHistory[2].amount).isEqualTo(300L)
        assertThat(userPointHistory[2].type).isEqualTo(TransactionType.USE)

        // 실행 중인 스레드 풀 종료
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }
}
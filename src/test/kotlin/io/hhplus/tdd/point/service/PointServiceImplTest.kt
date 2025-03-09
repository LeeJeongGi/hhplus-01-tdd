package io.hhplus.tdd.point.service

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.handler.UserLockManager
import io.hhplus.tdd.point.service.domain.TransactionType
import io.hhplus.tdd.point.service.domain.UserPoint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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
    @DisplayName("유저의 포인트를 조회 성공 테스트 - 300 포인트를 가진 유저를 조회 했을 때 정상적으로 조회하는지 테스트")
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
    @DisplayName("유저의 포인트를 조회 성공 테스트 - 포인트 저장 이력이 없는 회원 조회시 초기화 값인 포인트 0원으로 조회되는지 테스트")
    fun getDoNotSaveUserPointTest() {
        // when
        val userPoint = pointService.getUserPoint(1L)

        // then
        assertThat(userPoint).isNotNull
        assertThat(userPoint.id).isEqualTo(1L)
        assertThat(userPoint.point).isEqualTo(0)
    }

    @Test
    @DisplayName("유저의 히스토리를 조회 성공 테스트 - 2가지의 충전과 1가지의 사용 했을 때 정상적으로 히스토리 보여지는지 확인 테스트")
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
    @DisplayName("포인트 저장 성공 테스트 - 400 포인트 저장 시 정상적으로 포인트 저장되는지 확인 테스트")
    fun savePointTest() {
        // given
        val userPoint = UserPoint.of(1L, 400L)

        // when
        val saveUserPoint = pointService.savePoint(userPoint)

        // then
        assertThat(saveUserPoint.id).isEqualTo(userPoint.id)
        assertThat(saveUserPoint.point).isEqualTo(userPoint.point)
    }

    @Test
    @DisplayName("포인트 저장 성공 테스트 - 포인트 저장 시 기존 400 포인트에서 100 포인트 누적되는지 검증하는 테스트")
    fun savePlusPointTest() {
        // given
        val userPoint = UserPoint.of(1L, 400L)
        val saveUserPoint = pointService.savePoint(userPoint)
        val updateUserPoint = UserPoint.of(1L, 100L)

        // when
        val curUserPoint = pointService.savePoint(updateUserPoint)

        // then
        assertThat(curUserPoint.id).isEqualTo(updateUserPoint.id)
        assertThat(curUserPoint.point).isEqualTo(userPoint.point + updateUserPoint.point)
    }

    @Test
    @DisplayName("포인트 충전 && 사용 동시성 테스트 - 포인트 충전과 사용이 순차적으로 처리되는지 테스트")
    fun executeSequentiallyOnConcurrentUsage() {
        // given
        val userId = 1L
        val initialAmount = 1000L
        val requestAmount1 = 500L
        val requestAmount2 = 1200L

        // 포인트 초기 상태 설정
        pointService.savePoint(UserPoint.of(userId, initialAmount))

        val executor = Executors.newFixedThreadPool(2)
        val thread1 = CompletableFuture.supplyAsync({
            pointService.savePoint(UserPoint.of(userId, requestAmount1))
        }, executor)

        val thread2 = CompletableFuture.supplyAsync({
            pointService.usePoint(UserPoint.of(userId, requestAmount2))
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

        assertThat(userPointHistory[2].amount).isEqualTo(1200L)
        assertThat(userPointHistory[2].type).isEqualTo(TransactionType.USE)

        // 실행 중인 스레드 풀 종료
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)
    }
    
    @Test
    fun test() {
        val future = CompletableFuture.supplyAsync {
            Thread.sleep(1000) // 1초 동안 대기 (실제 비동기 작업을 가정)
            "Hello, CompletableFuture!"
        }

        println(future.get()) // get()을 호출하면 결과가 반환될 때까지 대기
    }

    @Test
    @DisplayName("잔액 충전 동시성 테스트 - 동시에 10번 충전 요청이 왔을 때 정상적으로 10번 충전 되는지 확인.")
    fun executeSequentiallyOnConcurrentUsage2() {
        // given
        val userId = 1L
        val initialAmount = 1000L
        val requestAmount1 = 100L

        // 포인트 초기 상태 설정
        pointService.savePoint(UserPoint.of(userId, initialAmount))

        val executor = Executors.newFixedThreadPool(1000)
        val successCount = AtomicInteger()
        val failCount = AtomicInteger()

        val startTime = System.nanoTime()

        val latch1 = CountDownLatch(10)
        try {
            repeat(10) {
                executor.submit {
                    try {
                        pointService.savePoint(UserPoint.of(userId, requestAmount1))
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                        failCount.incrementAndGet()
                    } finally {
                        latch1.countDown()
                    }
                }
            }
        } finally {
            executor.shutdown()
        }

        latch1.await() // 첫 번째 요청이 끝날 때까지 대기

        val endTime = System.nanoTime()
        val duration = Duration.ofNanos(endTime - startTime)

        // then
        val finalPoint = pointService.getUserPoint(userId)
        assertThat(finalPoint.point).isEqualTo(initialAmount + requestAmount1 * 10)

        println("🕒 테스트 실행 시간: ${duration.toMillis()} 밀리초")
        println("✅ 성공한 요청 횟수: $successCount")
        println("❌ 실패한 요청 횟수: $failCount")
        println("💰 최종 잔액: ${finalPoint.point}")
    }

}
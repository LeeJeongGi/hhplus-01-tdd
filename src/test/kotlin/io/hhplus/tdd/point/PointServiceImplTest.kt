package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
class PointServiceImplTest {

    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointServiceImpl: PointServiceImpl

    @BeforeEach
    fun setUp() {
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()

        pointServiceImpl = PointServiceImpl(pointHistoryTable, userPointTable)
    }

    @Test
    @DisplayName("유저의 포인트를 조회")
    fun getUserPointTest() {
        // given
        userPointTable.insertOrUpdate(1L, 300L)

        // when
        val userPoint = pointServiceImpl.getUserPoint(1L)

        // then
        assertThat(userPoint).isNotNull
        assertThat(userPoint.id).isEqualTo(1L)
        assertThat(userPoint.point).isEqualTo(300L)
    }

    @Test
    @DisplayName("저장이 아직 안된 유저의 포인트를 조회")
    fun getDoNotSaveUserPointTest() {
        // when
        val userPoint = pointServiceImpl.getUserPoint(1L)

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
        val userPointHistory = pointServiceImpl.getUserPointHistory(2L)

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
        val id = 1L
        val savePoint = 400L

        // when
        val saveUserPoint = pointServiceImpl.savePoint(id, amount = savePoint)

        // then
        assertThat(saveUserPoint.id).isEqualTo(id)
        assertThat(saveUserPoint.point).isEqualTo(savePoint)
    }

    @Test
    @DisplayName("포인트 저장 시 기존 포인트에 누적되는지 검증하는 테스트")
    fun savePlusPointTest() {
        // given
        val id = 1L
        val savePoint = 400L
        val saveUserPoint = pointServiceImpl.savePoint(id, amount = savePoint)

        // when
        val curUserPoint = pointServiceImpl.savePoint(saveUserPoint.id, amount = saveUserPoint.point + 100L)

        // then
        assertThat(curUserPoint.id).isEqualTo(id)
        assertThat(curUserPoint.point).isEqualTo(2 * saveUserPoint.point + 100L)
    }

    @Test
    @DisplayName("포인트 사용 실패 테스트 - 가진 포인트 보다 많은 포인트를 사용하려고 하는 경우")
    fun usePointTest() {
        // given
        userPointTable.insertOrUpdate(1L, 500L)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            pointServiceImpl.usePoint(1L, 1000L)
        }.message

        assertThat(message).isEqualTo("사용할 수 있는 포인트가 부족합니다. 현재 포인트: 500, 요청한 포인트: 1000")
    }

    @Test
    @DisplayName("포인트 충전과 사용이 순차적으로 처리되는지 테스트")
    fun executeSequentiallyOnConcurrentUsage() {

        // given
        val threadCount = 2
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        // when
        repeat(threadCount) { i ->
            executor.execute {
                try {
                    pointServiceImpl.savePoint(3L, 1000L)
                    pointServiceImpl.usePoint(3L, 500L)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        // then
        assertThat(pointHistoryTable.selectAllByUserId(3L).size).isEqualTo(threadCount * 2)

        val finalPoint = pointServiceImpl.getUserPoint(3L)
        assertThat(finalPoint.point).isEqualTo(1000L)
    }
}
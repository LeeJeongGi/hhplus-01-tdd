package io.hhplus.tdd.database

import io.hhplus.tdd.point.TransactionType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PointHistoryTableTest @Autowired constructor (
    val pointHistoryTable: PointHistoryTable,
) {

    /**
     * database 하위 클래스는 변경하지 않기 때문에 실패 테스트는 작성하지 않았습니다.
     * 외부 Api를 사용한다는 가정하에 성공 케이스만 작성 했습니다.
     */

    @Test
    @DisplayName("히스토리 저장 테스트 - 정상적으로 저장 되는지 확인하기 위해 테스트 코드 작성")
    fun insertTest() {
        // given
        val userId = 1L
        val amount = 100L
        val transactionType = TransactionType.USE
        val timestamp = System.currentTimeMillis()

        // when
        val history = pointHistoryTable.insert(userId, amount, transactionType, timestamp)

        // then
        assertThat(history).isNotNull
        assertThat(history.userId).isEqualTo(userId)
        assertThat(history.amount).isEqualTo(amount)
        assertThat(history.type).isEqualTo(transactionType)
    }

    @Test
    @DisplayName("히스토리 조회 테스트 - 히스토리가 정상적으로 조회되는지 확인하기 위해 테스트 코드 작성")
    fun getUserPointHistory() {
        // given
        saveSampleData()

        // when
        val userHistory = pointHistoryTable.selectAllByUserId(1L)

        // then
        assertThat(userHistory).hasSize(2)
        assertThat(userHistory[0].userId).isEqualTo(1L)
        assertThat(userHistory[0].type).isEqualTo(TransactionType.USE)
        assertThat(userHistory[0].amount).isEqualTo(100L)

        assertThat(userHistory[1].userId).isEqualTo(1L)
        assertThat(userHistory[1].type).isEqualTo(TransactionType.CHARGE)
        assertThat(userHistory[1].amount).isEqualTo(200L)
    }

    private fun saveSampleData() {
        val firstHistory = pointHistoryTable.insert(
            id = 1L,
            amount = 100L,
            transactionType = TransactionType.USE,
            updateMillis = System.currentTimeMillis(),
        )

        val secondHistory = pointHistoryTable.insert(
            id = 1L,
            amount = 200L,
            transactionType = TransactionType.CHARGE,
            updateMillis = System.currentTimeMillis(),
        )
    }
}
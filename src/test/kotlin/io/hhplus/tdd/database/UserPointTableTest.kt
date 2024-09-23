package io.hhplus.tdd.database

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserPointTableTest @Autowired constructor(
    val userPointTable: UserPointTable,
) {

    /**
     * database 하위 클래스는 변경하지 않기 때문에 실패 테스트는 작성하지 않았습니다.
     * 외부 Api를 사용한다는 가정하에 성공 케이스만 작성 했습니다.
     */

    @Test
    @DisplayName("유저 포인트 저장 테스트 - 정상적으로 유저 포인트가 저장되는지 확인하기 위해 테스트 코드 작성")
    fun userPointInsertTest() {
        // given
        val id = 1L
        val amount = 100L

        // when
        val insertUserPoint = userPointTable.insertOrUpdate(id, amount)

        // then
        assertThat(insertUserPoint.id).isEqualTo(id)
        assertThat(insertUserPoint.point).isEqualTo(amount)
    }

    @Test
    @DisplayName("유저 포인트 업데이트 테스트 - 정상적으로 유저 표인트 업데이트 되는지 확인하기 위해 테스트 코드 작성")
    fun userPointUpdateTest() {
        // given
        userPointTable.insertOrUpdate(1L, 100L)

        val id = 1L
        val amount = 200L

        // when
        val insertUserPoint = userPointTable.insertOrUpdate(id, amount)

        // then
        assertThat(insertUserPoint.id).isEqualTo(id)
        assertThat(insertUserPoint.point).isEqualTo(amount)
    }

    @Test
    @DisplayName("유저 포인트 조회 - 정상적으로 유저 포인트 조회되는지 확인하기 위해 테스트 코드 작성")
    fun getUserPoint() {
        // given
        userPointTable.insertOrUpdate(1L, 100L)

        // when
        val userPoint = userPointTable.selectById(1L)

        // then
        assertThat(userPoint).isNotNull
        assertThat(userPoint.id).isEqualTo(1L)
        assertThat(userPoint.point).isEqualTo(100L)
    }
}
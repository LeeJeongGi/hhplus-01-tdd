package io.hhplus.tdd.point.service.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserPointTest {

    @Test
    @DisplayName("포인트 저장 성공 테스트 - 기존 1000 포인트에서 1200 포인트 추가했을 때 정상적으로 2200 포인트 저장되는지 확인 테스트")
    fun savePoint() {
        // given
        val userPoint = UserPoint.of(1L, 1000L)

        // when
        val saveUserPoint = userPoint.savePoint(1200L)

        // then
        assertThat(saveUserPoint.point).isEqualTo(2200L)
    }

    @Test
    @DisplayName("포인트 저장 실패 테스트 - 포인트 충전 시 0보다 작은 수 충전 했을 때 예외 발생 테스트")
    fun savePointException() {
        // given
        val userPoint = UserPoint.of(1L, 100L)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            userPoint.savePoint(-100L)
        }.message

        assertThat(message).isEqualTo("잘못된 충전 요청: 충전 금액은 0 이상이어야 합니다. 요청한 금액: -100")
    }

    @Test
    @DisplayName("포인트 저장 실패 테스트 - 백만원 보다 넘게 가지면 안되는 포인트 정책 위반 하는 경우")
    fun savePointOverMaxBalanceExceptionTest() {
        // given
        val userPoint = UserPoint.of(1L, 999000L)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            userPoint.savePoint(10000L)
        }.message

        assertThat(message).isEqualTo("최대 포인트는 1000000원입니다. 현재 포인트: 999000, 요청한 포인트: 10000")
    }

    @Test
    @DisplayName("포인트 사용 성공 테스트 - 기존 1000 포인트에서 500 포인트 사용했을 때 정상적으로 포인트 사용하는지 테스트")
    fun usePointTest() {
        // given
        val userPoint  = UserPoint.of(1L, 1000L)

        // when
        val useUserPoint = userPoint.usePoint(500L)

        // then
        assertThat(useUserPoint.point).isEqualTo(500L)
    }

    @Test
    @DisplayName("포인트 사용 실패 테스트 - 기존 500포인트만 있는데 1000 포인트 사용하려고 하면 예외 발생 테스트 ")
    fun usePointExceptionTest() {
        // given
        val userPoint = UserPoint.of(1L, 500L)

        // when & then
        val message = assertThrows<IllegalArgumentException> {
            userPoint.usePoint(1000L)
        }.message

        assertThat(message).isEqualTo("사용할 수 있는 포인트가 부족합니다. 현재 포인트: 500, 요청한 포인트: 1000")
    }
}
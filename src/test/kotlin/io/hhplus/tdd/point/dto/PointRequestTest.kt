package io.hhplus.tdd.point.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class PointRequestTest {

    @Test
    @DisplayName("0보다 작은 충전금액 오류 발생 검증 하는 테스트")
    fun shouldThrowExceptionWhenChargeAmountIsNegative() {
        // given
        val pointRequest = PointRequest(1L, 100L)

        // when & then
        val message = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            pointRequest.checkPointValidity(-100L)
        }.message

        assertThat(message).isEqualTo("잘못된 충전 요청: 충전 금액은 0 이상이어야 합니다. 요청한 금액: -100")
    }
}
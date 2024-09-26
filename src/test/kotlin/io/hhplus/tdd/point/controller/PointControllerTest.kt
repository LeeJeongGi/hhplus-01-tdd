package io.hhplus.tdd.point.controller

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("특정 유저의 포인트를 조회하는 API 기능 테스트")
    fun getUserPoint() {
        // when && then
        mockMvc.perform(
            MockMvcRequestBuilders.get("/point/{id}", 1L)
        ).andExpect(
            status().isOk
        ).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("특정 유저의 히스토리를 조회하는 API 기능 테스트")
    fun getUserHistory() {

        // when && then
        mockMvc.perform(
            MockMvcRequestBuilders.get("/point/{id}/histories", 1L)
        ).andExpect(
            status().isOk
        ).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("특정 유저의 포인트를 충전하는 API 기능 테스트")
    fun chargeUserPoints() {
        // when && then
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/point/{id}/charge", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("1000")  // 충전할 포인트 양
        ).andExpect(
            status().isOk
        ).andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("특정 유저의 포인트를 사용하는 API 기능 테스트")
    fun useUserPoints() {
        // when && then
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/point/{id}/use", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("0")  // 사용할 포인트 양
        ).andExpect(
            status().isOk
        ).andDo(MockMvcResultHandlers.print())
    }
}
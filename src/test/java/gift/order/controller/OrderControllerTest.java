package gift.order.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import gift.config.SecurityConfig;
import gift.order.dto.CartOrderCreateRequest;
import gift.order.dto.OrderResponse;
import gift.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = OrderController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
        })
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("주문 성공")
    void createOrderSuccess() throws Exception {

        OrderResponse orderResponse = new OrderResponse(1L, 1L, 30, LocalDateTime.now(), "메시지");

        given(orderService.saveCartOrder(any(),any()))
                .willReturn(orderResponse);

        CartOrderCreateRequest orderRequest = new CartOrderCreateRequest(1L, 30, "메시지");

        String content = objectMapper.writeValueAsString(orderRequest);


        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderResponse.id()))
                .andExpect(jsonPath("$.optionId").value(orderResponse.optionId()))
                .andExpect(jsonPath("$.quantity").value(orderResponse.quantity()))
                .andExpect(jsonPath("$.message").value(orderResponse.message()));
    }

    @Test
    @DisplayName("주문 실패 - 주문 수량은 1개 이상")
    void createOrderFail() throws Exception {
        CartOrderCreateRequest orderRequest = new CartOrderCreateRequest(null, 0, "메시지");

        String content = objectMapper.writeValueAsString(orderRequest);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldErrors.quantity").exists())
                .andExpect(jsonPath("$.fieldErrors.wishProductId").exists())
                .andExpect(jsonPath("$.fieldErrors.message").doesNotExist())
                .andExpect(result-> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
    }
}
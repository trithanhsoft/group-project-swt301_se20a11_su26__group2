package com.swp391.coding_platform.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.OrderCheckoutRequest;
import com.swp391.coding_platform.dto.response.OrderCheckoutResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.PurchaseHistoryResponse;
import com.swp391.coding_platform.service.payment.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)

public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void checkout_ReturnsOrderCheckoutResponse() throws Exception {
        OrderCheckoutRequest request = new OrderCheckoutRequest();
        request.setCourseIds(List.of(1L, 2L));

        OrderCheckoutResponse response = new OrderCheckoutResponse();
        when(orderService.createCheckout(eq(1), any(OrderCheckoutRequest.class))).thenReturn(response);

        mockMvc.perform(post("/orders/checkout")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Order checkout completed successfully"));
    }

    @Test
    void getPurchaseHistory_ReturnsPageResponse() throws Exception {
        PageResponse<PurchaseHistoryResponse> pageResponse = new PageResponse<>();
        when(orderService.getPurchaseHistory(eq(1), eq(0), eq(10))).thenReturn(pageResponse);

        mockMvc.perform(get("/orders/purchase-history")
                        .param("page", "0")
                        .param("size", "10")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched purchase history successfully"));
    }
}

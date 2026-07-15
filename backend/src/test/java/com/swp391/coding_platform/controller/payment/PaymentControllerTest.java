package com.swp391.coding_platform.controller.payment;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.swp391.coding_platform.dto.request.PaymentDepositRequest;
import com.swp391.coding_platform.dto.response.PaymentDepositResponse;
import com.swp391.coding_platform.service.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getBalance_ReturnsBalance() throws Exception {
        when(paymentService.getUserBalance(1)).thenReturn(new BigDecimal("100.50"));

        mockMvc.perform(get("/payment/balance")
                .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result").value(100.50));

        verify(paymentService, times(1)).getUserBalance(1);
    }

    @Test
    void createDeposit_ReturnsPaymentDepositResponse() throws Exception {
        PaymentDepositRequest request = new PaymentDepositRequest();
        request.setAmount(new BigDecimal("50000"));

        PaymentDepositResponse response = PaymentDepositResponse.builder()
                .checkoutUrl("http://checkout.url")
                .transactionCode("123456")
                .build();

        when(paymentService.createDepositPayment(eq(1), any(PaymentDepositRequest.class))).thenReturn(response);

        mockMvc.perform(post("/payment/deposit")
                .with(jwt().jwt(builder -> builder.claim("userId", 1)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.checkoutUrl").value("http://checkout.url"))
                .andExpect(jsonPath("$.result.transactionCode").value("123456"));

        verify(paymentService, times(1)).createDepositPayment(eq(1), any(PaymentDepositRequest.class));
    }

    @Test
    void cancelPayment_ReturnsSuccess() throws Exception {
        doNothing().when(paymentService).cancelPayment(1, "123456");

        mockMvc.perform(post("/payment/cancel/123456")
                .with(jwt().jwt(builder -> builder.claim("userId", 1)))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Payment cancelled successfully"));

        verify(paymentService, times(1)).cancelPayment(1, "123456");
    }

    @Test
    void handleWebhook_ReturnsSuccess() throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("code", "00");

        doNothing().when(paymentService).handlePayOSWebhook(any(ObjectNode.class));

        mockMvc.perform(post("/payment/webhook")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(paymentService, times(1)).handlePayOSWebhook(any(ObjectNode.class));
    }
}

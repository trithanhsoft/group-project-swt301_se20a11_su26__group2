package com.swp391.coding_platform.controller.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.PaymentTransactionStatisticResponse;
import com.swp391.coding_platform.dto.response.TransactionStatisticResponse;
import com.swp391.coding_platform.entity.enums.PaymentType;
import com.swp391.coding_platform.entity.enums.TransactionType;
import com.swp391.coding_platform.service.payment.TransactionStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionStatisticsController.class)

public class TransactionStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionStatisticsService transactionStatisticsService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void getWalletTransactions_ReturnsPageResponse() throws Exception {
        PageResponse<TransactionStatisticResponse> pageResponse = new PageResponse<>();
        when(transactionStatisticsService.getTransactionStatistics(eq(1), isNull(), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/wallet/transactions")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get wallet transaction statistics successfully"));
    }

    @Test
    void getPaymentTransactions_ReturnsPageResponse() throws Exception {
        PageResponse<PaymentTransactionStatisticResponse> pageResponse = new PageResponse<>();
        when(transactionStatisticsService.getPaymentTransactionStatistics(eq(1), isNull(), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/payment/transactions")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get payment transaction statistics successfully"));
    }
}

package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.PaymentTransactionStatisticResponse;
import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentTransactionMapperTest {

    private final PaymentTransactionMapper mapper = new PaymentTransactionMapperImpl();

    @Test
    void toPaymentTransactionStatisticResponse() {
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setAmount(BigDecimal.valueOf(50000.0));

        PaymentTransactionStatisticResponse response = mapper.toPaymentTransactionStatisticResponse(entity);

        assertEquals(now, response.getDate());
        assertEquals(BigDecimal.valueOf(50000.0), response.getAmount());
    }
}

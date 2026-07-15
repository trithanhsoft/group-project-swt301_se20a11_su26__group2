package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.TransactionStatisticResponse;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletTransactionMapperTest {

    private final WalletTransactionMapper mapper = new WalletTransactionMapperImpl();

    @Test
    void toTransactionStatisticResponse() {
        WalletTransactionEntity entity = new WalletTransactionEntity();
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setAmount(BigDecimal.valueOf(100.0));

        TransactionStatisticResponse response = mapper.toTransactionStatisticResponse(entity);

        assertEquals(now, response.getDate());
        assertEquals(BigDecimal.valueOf(100.0), response.getAmount());
    }
}

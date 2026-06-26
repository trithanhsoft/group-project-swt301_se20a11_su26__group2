package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.PaymentTransactionStatisticResponse;
import com.swp391.coding_platform.entity.payment.PaymentTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {

    @Mapping(target = "date", source = "createdAt")
    PaymentTransactionStatisticResponse toPaymentTransactionStatisticResponse(PaymentTransactionEntity paymentTransactionEntity);
}

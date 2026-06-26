package com.swp391.coding_platform.mapper;

import com.swp391.coding_platform.dto.response.TransactionStatisticResponse;
import com.swp391.coding_platform.entity.payment.WalletTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(target = "date", source = "createdAt")
    TransactionStatisticResponse toTransactionStatisticResponse(WalletTransactionEntity walletTransactionEntity);
}

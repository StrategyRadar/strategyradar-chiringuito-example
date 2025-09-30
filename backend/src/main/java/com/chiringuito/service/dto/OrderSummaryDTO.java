package com.chiringuito.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDTO {

    private UUID orderId;
    private String status;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private List<OrderLineDTO> orderLines;
}
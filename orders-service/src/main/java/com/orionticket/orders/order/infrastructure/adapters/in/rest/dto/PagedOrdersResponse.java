package com.orionticket.orders.order.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PagedOrdersResponse {
    private List<OrderSummaryResponse> orders;
    private int page;
    private int totalPages;
    private long totalElements;
}

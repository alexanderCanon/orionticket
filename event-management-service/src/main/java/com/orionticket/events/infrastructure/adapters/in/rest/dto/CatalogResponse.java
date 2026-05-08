package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CatalogResponse<T> {
    private List<T> events;
    private int page;
    private int totalPages;
}

package com.orionticket.seating.seat.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateSeatingMapRequest {

    @NotNull
    private UUID batchId;

    // Solo uno de los dos debe venir: generalAdmission o mappedSections.
    // El controller decide cuál flujo ejecutar según cuál es no-nulo.
    private GeneralAdmissionConfig generalAdmission;
    private List<SectionConfig> mappedSections;

    @Data
    public static class GeneralAdmissionConfig {
        @Min(1)
        private int capacity;
        private String accessPolicy;
    }

    @Data
    public static class SectionConfig {
        private String zone;
        private String section;
        private List<RowConfig> rows;
        private String accessPolicy;
    }

    @Data
    public static class RowConfig {
        private String rowLabel;
        private List<String> seatNumbers;
    }
}

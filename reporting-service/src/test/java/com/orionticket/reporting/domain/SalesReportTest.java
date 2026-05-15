package com.orionticket.reporting.domain;

import com.orionticket.reporting.domain.model.SalesReport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportTest {

    @Test
    void shouldCreateSalesReportWithAllFields() {
        UUID reportId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();
        Instant generatedAt = Instant.now();

        SalesReport report = new SalesReport(
                reportId, organizerId, eventId, dateId,
                100, new BigDecimal("1000.00"), new BigDecimal("100.00"),
                new BigDecimal("900.00"), generatedAt);

        assertEquals(reportId, report.getReportId());
        assertEquals(organizerId, report.getOrganizerId());
        assertEquals(eventId, report.getEventId());
        assertEquals(dateId, report.getDateId());
        assertEquals(100, report.getTotalTicketsSold());
        assertEquals(new BigDecimal("1000.00"), report.getTotalRevenue());
        assertEquals(new BigDecimal("100.00"), report.getTotalServiceFees());
        assertEquals(new BigDecimal("900.00"), report.getTotalPayouts());
        assertEquals(generatedAt, report.getGeneratedAt());
    }
}
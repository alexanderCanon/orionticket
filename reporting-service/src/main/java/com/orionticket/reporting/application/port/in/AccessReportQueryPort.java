package com.orionticket.reporting.application.port.in;

import com.orionticket.reporting.domain.model.AccessReport;
import java.util.UUID;

public interface AccessReportQueryPort {
    AccessReport getAccessReport(UUID eventId, UUID dateId);
}

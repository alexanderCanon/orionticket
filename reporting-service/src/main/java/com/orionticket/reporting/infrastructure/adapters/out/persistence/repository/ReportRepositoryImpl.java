package com.orionticket.reporting.infrastructure.adapters.out.persistence.repository;

import com.orionticket.reporting.domain.model.AccessReport;
import com.orionticket.reporting.domain.model.CommissionReport;
import com.orionticket.reporting.domain.model.SalesReport;
import com.orionticket.reporting.domain.port.out.ReportRepository;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.mapper.ReportEntityMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportRepositoryImpl implements ReportRepository {

    private final SalesReportRepository salesReportRepository;
    private final CommissionReportRepository commissionReportRepository;
    private final AccessReportRepository accessReportRepository;
    private final ReportEntityMapper mapper;

    public ReportRepositoryImpl(SalesReportRepository salesReportRepository,
                                CommissionReportRepository commissionReportRepository,
                                AccessReportRepository accessReportRepository,
                                ReportEntityMapper mapper) {
        this.salesReportRepository = salesReportRepository;
        this.commissionReportRepository = commissionReportRepository;
        this.accessReportRepository = accessReportRepository;
        this.mapper = mapper;
    }

    @Override
    public List<SalesReport> findSalesReports(UUID organizerId, UUID eventId, UUID dateId) {
        return salesReportRepository.findByFilters(organizerId, eventId, dateId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommissionReport> findCommissionReports(UUID organizerId, Instant periodStart, Instant periodEnd) {
        return commissionReportRepository.findByFilters(organizerId, periodStart, periodEnd)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AccessReport> findAccessReport(UUID eventId, UUID dateId) {
        return accessReportRepository.findByEventIdAndDateId(eventId, dateId)
                .map(mapper::toDomain);
    }

    @Override
    public void save(SalesReport salesReport) {
        salesReportRepository.save(mapper.toEntity(salesReport));
    }

    @Override
    public void save(CommissionReport commissionReport) {
        commissionReportRepository.save(mapper.toEntity(commissionReport));
    }

    @Override
    public void save(AccessReport accessReport) {
        accessReportRepository.save(mapper.toEntity(accessReport));
    }
}
package com.orionticket.reporting.infrastructure.config;

import com.orionticket.reporting.application.port.in.ReportQueryPort;
import com.orionticket.reporting.application.service.AccessReportQueryService;
import com.orionticket.reporting.application.service.CommissionReportQueryService;
import com.orionticket.reporting.application.service.SalesReportQueryService;
import com.orionticket.reporting.domain.port.out.ReportRepository;
import com.orionticket.reporting.infrastructure.adapters.in.rest.mapper.ReportMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public ReportMapper reportMapper() {
        return new ReportMapper();
    }

    @Bean
    public SalesReportQueryService salesReportQueryService(ReportRepository reportRepository) {
        return new SalesReportQueryService(reportRepository);
    }

    @Bean
    public CommissionReportQueryService commissionReportQueryService(ReportRepository reportRepository) {
        return new CommissionReportQueryService(reportRepository);
    }

    @Bean
    public AccessReportQueryService accessReportQueryService(ReportRepository reportRepository) {
        return new AccessReportQueryService(reportRepository);
    }
}
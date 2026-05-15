package com.orionticket.reporting.infrastructure.config;

import com.orionticket.reporting.application.port.in.AccessReportQueryPort;
import com.orionticket.reporting.application.port.in.CommissionReportQueryPort;
import com.orionticket.reporting.application.port.in.SalesReportQueryPort;
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
    public SalesReportQueryPort salesReportQueryPort(ReportRepository reportRepository) {
        return new SalesReportQueryService(reportRepository);
    }

    @Bean
    public CommissionReportQueryPort commissionReportQueryPort(ReportRepository reportRepository) {
        return new CommissionReportQueryService(reportRepository);
    }

    @Bean
    public AccessReportQueryPort accessReportQueryPort(ReportRepository reportRepository) {
        return new AccessReportQueryService(reportRepository);
    }
}
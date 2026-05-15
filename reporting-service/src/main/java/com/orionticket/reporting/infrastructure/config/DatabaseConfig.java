package com.orionticket.reporting.infrastructure.config;

import com.orionticket.reporting.infrastructure.adapters.out.persistence.mapper.ReportEntityMapper;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.repository.AccessReportRepository;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.repository.CommissionReportRepository;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.repository.ReportRepositoryImpl;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.repository.SalesReportRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.orionticket.reporting.infrastructure.adapters.out.persistence.repository")
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public ReportEntityMapper reportEntityMapper() {
        return new ReportEntityMapper();
    }

    @Bean
    public ReportRepositoryImpl reportRepositoryImpl(
            SalesReportRepository salesReportRepository,
            CommissionReportRepository commissionReportRepository,
            AccessReportRepository accessReportRepository,
            ReportEntityMapper mapper) {
        return new ReportRepositoryImpl(salesReportRepository, commissionReportRepository, accessReportRepository, mapper);
    }
}
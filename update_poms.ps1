$services = @('identity-service','event-management-service','seating-inventory-service','orders-service','payments-service','ticket-issuance-service','access-control-service','notifications-service','reporting-service')

$deps = @(
    @{ id='springdoc-openapi-starter-webmvc-ui'; xml="`n        <!-- Shared Dependencies -->`n        <dependency>`n            <groupId>org.springdoc</groupId>`n            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>`n            <version>2.5.0</version>`n        </dependency>" },
    @{ id='micrometer-registry-prometheus'; xml="`n        <dependency>`n            <groupId>io.micrometer</groupId>`n            <artifactId>micrometer-registry-prometheus</artifactId>`n            <scope>runtime</scope>`n        </dependency>" },
    @{ id='spring-boot-starter-validation'; xml="`n        <dependency>`n            <groupId>org.springframework.boot</groupId>`n            <artifactId>spring-boot-starter-validation</artifactId>`n        </dependency>" },
    @{ id='spring-boot-starter-actuator'; xml="`n        <dependency>`n            <groupId>org.springframework.boot</groupId>`n            <artifactId>spring-boot-starter-actuator</artifactId>`n        </dependency>" },
    @{ id='flyway-core'; xml="`n        <dependency>`n            <groupId>org.flywaydb</groupId>`n            <artifactId>flyway-core</artifactId>`n        </dependency>" },
    @{ id='flyway-database-postgresql'; xml="`n        <dependency>`n            <groupId>org.flywaydb</groupId>`n            <artifactId>flyway-database-postgresql</artifactId>`n        </dependency>" },
    @{ id='spring-boot-starter-security'; xml="`n        <dependency>`n            <groupId>org.springframework.boot</groupId>`n            <artifactId>spring-boot-starter-security</artifactId>`n        </dependency>" }
)

foreach ($s in $services) {
    $path = "$s\pom.xml"
    if (Test-Path $path) {
        $content = Get-Content $path -Raw
        $modified = $false
        foreach ($d in $deps) {
            if (-not ($content -match "<artifactId>$($d.id)</artifactId>")) {
                $content = $content.Replace("</dependencies>", "$($d.xml)`n    </dependencies>")
                $modified = $true
                Write-Host "Added $($d.id) to $s"
            }
        }
        if ($modified) {
            Set-Content -Path $path -Value $content -Encoding UTF8
        }
    }
}

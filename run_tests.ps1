$services = @{
    "identity-service" = 8081
    "event-management-service" = 8082
    "seating-inventory-service" = 8083
    "orders-service" = 8084
    "payments-service" = 8085
    "ticket-issuance-service" = 8086
    "access-control-service" = 8087
    "notifications-service" = 8088
    "reporting-service" = 8089
}

$results = @()
$results += "# Resultados de Pruebas Automáticas (E2E y Salud)"
$results += "Fecha de ejecución: $(Get-Date)"
$results += "`n## 1. Verificación de Salud de Microservicios (Actuator)`n"
$results += "| Microservicio | Puerto | Estado HTTP | Resultado |"
$results += "|---|---|---|---|"

foreach ($svc in $services.Keys) {
    $port = $services[$svc]
    $url = "http://localhost:$port/actuator/health"
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 10 -ErrorAction Stop
        if ($response.status -eq "UP") {
            $results += "| $svc | $port | 200 OK | ✅ UP |"
        } else {
            $results += "| $svc | $port | 200 OK | ⚠️ $($response.status) |"
        }
    } catch {
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            $results += "| $svc | $port | $statusCode | ❌ FALLO |"
        } else {
            $results += "| $svc | $port | ERROR | ❌ FALLO |"
        }
    }
}

$mdContent = $results -join "`n"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText("test_results.md", $mdContent, $utf8NoBom)
Write-Host "Tests completed. Wrote test_results.md."

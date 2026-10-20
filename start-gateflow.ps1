
# GateFlow - API Gateway with Dynamic Routing
$ErrorActionPreference = "Continue"
$WorkspaceRoot = $PSScriptRoot

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "       GateFlow - API Gateway with Dynamic Routing" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

function Test-PortInUse([int]$Port) {
    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return ($null -ne $connection)
}

function Start-GateflowService([string]$ModuleName, [int]$Port) {
    if (Test-PortInUse $Port) {
        Write-Host "Port $Port is already in use. Skipping $ModuleName." -ForegroundColor Yellow
        Write-Host "If this is your running service, you can use it as-is." -ForegroundColor Gray
        return
    }

    Write-Host "Starting $ModuleName on port $Port..." -ForegroundColor Green

    $command = "Set-Location -LiteralPath '$WorkspaceRoot'; .\mvnw.cmd -pl $ModuleName spring-boot:run"

    Start-Process -FilePath "powershell.exe" `
        -WorkingDirectory $WorkspaceRoot `
        -ArgumentList @("-NoExit", "-Command", $command)

    Start-Sleep -Seconds 2
}

Write-Host "`n[1/6] Checking MySQL on port 3306..." -ForegroundColor Yellow

if (Test-PortInUse 3306) {
    Write-Host "MySQL is listening on port 3306." -ForegroundColor Green
} else {
    Write-Host "MySQL is not listening. Start MySQL Server first." -ForegroundColor Red
    Write-Host "Open MySQL Workbench and check your server connection." -ForegroundColor Yellow
    exit 1
}

Write-Host "`n[2/6] User Service" -ForegroundColor Yellow
Start-GateflowService "gateflow-user-service" 8081

Write-Host "`n[3/6] Product Service" -ForegroundColor Yellow
Start-GateflowService "gateflow-product-service" 8082

Write-Host "`n[4/6] Order Service" -ForegroundColor Yellow
Start-GateflowService "gateflow-order-service" 8083

Write-Host "`n[5/6] API Gateway" -ForegroundColor Yellow
Start-GateflowService "gateflow-gateway" 8080

Write-Host "`n[6/6] Dashboard" -ForegroundColor Yellow
Start-GateflowService "gateflow-dashboard" 8084

Write-Host "`n====================================================" -ForegroundColor Cyan
Write-Host "Launch commands sent. Check each service terminal." -ForegroundColor Green
Write-Host "Dashboard: http://localhost:8084/login" -ForegroundColor Cyan
Write-Host "Gateway:   http://localhost:8080" -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

Start-Sleep -Seconds 8

if (Test-PortInUse 8084) {
    Start-Process "http://localhost:8084/login"
} else {
    Write-Host "Dashboard has not started yet. Check its terminal." -ForegroundColor Yellow
}

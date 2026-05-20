Get-ChildItem -Path . -Filter "GlobalExceptionHandler.java" -Recurse | ForEach-Object {
    $content = [System.IO.File]::ReadAllText($_.FullName)
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($_.FullName, $content, $utf8NoBom)
    Write-Host "Fixed BOM in $($_.FullName)"
}

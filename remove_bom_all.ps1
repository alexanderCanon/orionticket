Get-ChildItem -Path . -Include *.java,*.xml -Recurse | ForEach-Object {
    $content = [System.IO.File]::ReadAllText($_.FullName)
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($_.FullName, $content, $utf8NoBom)
}
Write-Host "Removed BOM from all .java and .xml files."

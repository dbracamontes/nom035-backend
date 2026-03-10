param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Username = 'admin',
    [string]$Password = 'admin123',
    [string]$TemplateType = 'MORAL_4_1',
    [Parameter(Mandatory = $true)]
    [string]$Acta,
    [Parameter(Mandatory = $true)]
    [string]$Asamblea,
    [Parameter(Mandatory = $true)]
    [string]$Constancia
)

$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Net.Http

function New-FilePart {
    param(
        [string]$Path,
        [string]$FieldName
    )

    if (-not (Test-Path $Path)) {
        throw "File not found: $Path"
    }

    $stream = [System.IO.File]::OpenRead($Path)
    $content = New-Object System.Net.Http.StreamContent($stream)
    $content.Headers.ContentType = New-Object System.Net.Http.Headers.MediaTypeHeaderValue('application/pdf')

    return @{
        Stream = $stream
        Content = $content
        Field = $FieldName
        Name = [System.IO.Path]::GetFileName($Path)
    }
}

$client = New-Object System.Net.Http.HttpClient
$multipart = New-Object System.Net.Http.MultipartFormDataContent
$parts = @()

try {
    $authBytes = [System.Text.Encoding]::ASCII.GetBytes("$Username`:$Password")
    $token = [Convert]::ToBase64String($authBytes)
    $client.DefaultRequestHeaders.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Basic', $token)

    $parts += New-FilePart -Path $Acta -FieldName 'files'
    $parts += New-FilePart -Path $Asamblea -FieldName 'files'
    $parts += New-FilePart -Path $Constancia -FieldName 'files'

    foreach ($part in $parts) {
        $multipart.Add($part.Content, $part.Field, $part.Name)
    }

    $multipart.Add((New-Object System.Net.Http.StringContent($TemplateType)), 'templateType')

    $endpoint = "$BaseUrl/api/contracts/prepare"
    $response = $client.PostAsync($endpoint, $multipart).Result
    $body = $response.Content.ReadAsStringAsync().Result

    Write-Output ("HTTP_STATUS=" + [int]$response.StatusCode)

    if (-not $response.IsSuccessStatusCode) {
        Write-Output 'ERROR_BODY_BEGIN'
        Write-Output $body
        Write-Output 'ERROR_BODY_END'
        exit 1
    }

    $json = $body | ConvertFrom-Json
    $values = $json.suggestedValues

    Write-Output ("CIUDADANO=" + $values.CIUDADANO)
    Write-Output ("REPRESENTANTE_LEGAL=" + $values.REPRESENTANTE_LEGAL)
    Write-Output ("RAZON_SOCIAL=" + $values.RAZON_SOCIAL)
}
finally {
    foreach ($part in $parts) {
        if ($null -ne $part.Content) { $part.Content.Dispose() }
        if ($null -ne $part.Stream) { $part.Stream.Dispose() }
    }

    if ($null -ne $multipart) { $multipart.Dispose() }
    if ($null -ne $client) { $client.Dispose() }
}

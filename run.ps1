param(
    [ValidateSet('test', 'all')][string]$Task = 'all'
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force 'build/classes' | Out-Null
    $sources = @(Get-ChildItem 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
    & javac -encoding UTF-8 --release 17 -d 'build/classes' @sources
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed' }
    & java -cp 'build/classes' ru.urfu.algorithms.SortsTest
    if ($LASTEXITCODE -ne 0) { throw 'Tests failed' }
    if ($Task -ne 'test') {
        & java -cp 'build/classes' ru.urfu.algorithms.Benchmark
        if ($LASTEXITCODE -ne 0) { throw 'Benchmark failed' }
        & python 'scripts/plot_results.py'
        if ($LASTEXITCODE -ne 0) { throw 'Report generation failed' }
    }
} finally {
    Pop-Location
}

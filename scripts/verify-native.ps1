param(
    [ValidatePattern('^emulator-[0-9]+$')][string]$Serial = 'emulator-5554',
    [string]$Sdk = 'C:/Users/cluiz/AppData/Local/Android/Sdk',
    [switch]$SkipBuild,
    [switch]$ResetSyntheticData
)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$adb = Join-Path $Sdk 'platform-tools/adb.exe'
function Invoke-TrackerAdb([string[]]$Arguments) {
    & $adb -s $Serial @Arguments
    if ($LASTEXITCODE -ne 0) { throw "adb failed: $($Arguments -join ' ')" }
}
if ((& $adb -s $Serial shell getprop ro.kernel.qemu).Trim() -ne '1') { throw 'An emulator is required. Physical devices are excluded.' }
if (!$ResetSyntheticData) { throw 'Pass -ResetSyntheticData to clear only Expense Tracker on the selected synthetic-data emulator.' }
Push-Location $repo
try {
    if (!$SkipBuild) {
        & ./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --no-daemon --max-workers=2
        if ($LASTEXITCODE -ne 0) { throw 'Build or unit tests failed.' }
    }
    Invoke-TrackerAdb -Arguments @('install','-r','app/build/outputs/apk/debug/app-debug.apk')
    Invoke-TrackerAdb -Arguments @('install','-r','-t','app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')
    Invoke-TrackerAdb -Arguments @('shell','pm','clear','com.expensetracker')
    $evidence = Join-Path $repo 'docs/verification/evidence'
    $screens = Join-Path $repo 'docs/verification/native'
    New-Item -ItemType Directory -Force $evidence,$screens | Out-Null
    $result = & $adb -s $Serial shell am instrument -w -r com.expensetracker.test/androidx.test.runner.AndroidJUnitRunner 2>&1
    $result | Set-Content (Join-Path $evidence 'instrumentation.txt') -Encoding utf8
    $result | Write-Output
    & $adb -s $Serial pull /sdcard/Android/data/com.expensetracker/files/verification/. $screens
    Copy-Item app/build/test-results/testDebugUnitTest/TEST-*.xml $evidence
    if (($result -join "`n") -notmatch 'OK \(4 tests\)') { throw 'Instrumentation did not report four passing tests. Partial evidence is retained.' }
} finally { Pop-Location }

./pwsHelper/enablePwsColors.ps1
Start-Process PowerShell -ArgumentList "-noexit", "-command ./pwsHelper/chain.ps1 $args"

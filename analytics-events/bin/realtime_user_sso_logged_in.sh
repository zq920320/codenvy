source defaults.sh
$pigDir/pig $runMode -param log=$log -param lastMinutes=$lastMinutes $scriptDir/realtime_user_sso_logged_in.pig
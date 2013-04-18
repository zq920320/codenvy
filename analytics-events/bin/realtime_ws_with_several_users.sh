source defaults.sh
$pigDir/pig $runMode -param log=$log -param sessionsCount=$sessionsCount -param lastMinutes=$lastMinutes $scriptDir/realtime_ws_with_several_users.pig
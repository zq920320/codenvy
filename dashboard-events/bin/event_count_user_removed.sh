source defaults.sh
$pigDir/pig $runMode -param log=$log -param fromDate=$fromDate -param toDate=$toDate $scriptDir/event_count_user_removed.pig
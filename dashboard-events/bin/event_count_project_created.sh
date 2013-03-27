source default.sh
$pigDir/pig $runMode -param log=$log -param fromDate=$fromDate -param toDate=$toDate $scriptDir/event_count_project_created.pig
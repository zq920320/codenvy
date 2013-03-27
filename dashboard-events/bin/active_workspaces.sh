source default.sh
$pigDir/pig $runMode -param log=$log -param fromDate=$fromDate -param toDate=$toDate $scriptDir/active_workspaces.pig
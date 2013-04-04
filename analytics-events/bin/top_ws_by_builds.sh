source defaults.sh
$pigDir/pig $runMode -param log=$log -param top=$top -param fromDate=$fromDate -param toDate=$toDate $scriptDir/top_ws_by_builds.pig
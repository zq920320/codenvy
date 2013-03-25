source commons.sh
$pigDir/pig $runMode -param log=$log -param top=$top -param fromDate=$fromDate -param toDate=$toDate $scriptDir/top-ws-by-invitations.pig
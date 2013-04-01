source commons.sh
$pigDir/pig $runMode -param log=$log -param fromDate=$fromDate -param toDate=$toDate $scriptDir/details-jrebel-usage.pig
source commons.sh
$pigDir/pig $runMode -param log=$log -param inactiveInterval=$inactiveInterval -param fromDate=$fromDate -param toDate=$toDate $scriptDir/product-usage-time.pig
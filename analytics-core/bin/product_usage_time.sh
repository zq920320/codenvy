source defaults.sh
$pigDir/pig $runMode -param log=$log -param -param fromDate=$fromDate -param toDate=$toDate $scriptDir/product_usage_time.pig
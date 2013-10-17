rm wso2carbon.pid 
JAVA_OPTS="$JAVA_OPTS -Ddisable.cassandra.server.startup=true -Danalytics.pig.executor.service.congif=./repository/conf/analitics/pig-executor/pig-script-executor-config.xml"
sh ./bin/wso2server.sh  start

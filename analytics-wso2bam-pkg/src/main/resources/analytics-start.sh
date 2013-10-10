rm wso2carbon.pid 
JAVA_OPTS="$JAVA_OPTS -Ddisable.cassandra.server.startup=true"
sh ./bin/wso2server.sh  start

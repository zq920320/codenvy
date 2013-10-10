JAVA_OPTS="$JAVA_OPTS -Ddisable.cassandra.server.startup=true"
sh ./wso2server.sh  "$@"

# Environment Variable Prerequisites

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx2048m -XX:MaxPermSize=256m"

# Sets some variables
SECURITY_OPTS="-Djava.security.auth.login.config=${CATALINA_HOME}/conf/jaas.conf"

ANALYTICS_OPTS="-Danalytics.logs.directory=${CATALINA_HOME}/../logs-production \
                -Danalytics.scripts.directory=${CATALINA_HOME}/scripts \
                -Danalytics.result.directory=${CATALINA_HOME}/data/results \
                -Danalytics.metrics.initial.values=${CATALINA_HOME}/analytics-conf/initial-values.xml \
                -Dcom.codenvy.analytics.logpath=${CATALINA_HOME}/logs"


QUARTZ_OPTS="-Dorg.terracotta.quartz.skipUpdateCheck=true"

JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=true \
          -Dcom.sun.management.jmxremote.password.file=${CATALINA_HOME}/conf/jmxremote.password \
          -Dcom.sun.management.jmxremote.access.file=${CATALINA_HOME}/conf/jmxremote.access \
          -Dcom.sun.management.jmxremote.ssl=false"

#uncomment if you want to debug app server
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
REMOTE_DEBUG=""

export JAVA_OPTS="$JAVA_OPTS $SECURITY_OPTS $ANALYTICS_OPTS $JMX_OPTS $REMOTE_DEBUG $QUARTZ_OPTS"
export CLASSPATH="${CATALINA_HOME}/conf/:${CATALINA_HOME}/lib/jul-to-slf4j.jar:\
${CATALINA_HOME}/lib/slf4j-api.jar:${CATALINA_HOME}/lib/logback-classic.jar:${CATALINA_HOME}/lib/logback-core.jar:\
${CATALINA_HOME}/lib/mail.jar"

echo "======="
echo $JAVA_OPTS
echo "======="
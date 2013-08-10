#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2013] Codenvy, S.A.
# All Rights Reserved.
# NOTICE: All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any. The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

# Environment Variable Prerequisites

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx1536m -XX:MaxPermSize=256m -XX:+UseCompressedOops"

# Sets some variables
SECURITY_OPTS="-Djava.security.auth.login.config=${CATALINA_HOME}/conf/jaas.conf"

ANALYTICS_OPTS="-Danalytics.logs.directory=../logs-production \
                -Danalytics.csv.reports.directory=${CATALINA_HOME}/webapps/reports \
                -Danalytics.csv.reports.backup.directory=${CATALINA_HOME}/reports \
                -Danalytics.scripts.directory=${CATALINA_HOME}/scripts \
                -Danalytics.result.directory=${CATALINA_HOME}/data/results \
                -Danalytics.metrics.initial.values=${CATALINA_HOME}/analytics-conf/initial-values.xml \
                -Danalytics.job.acton.properties=${CATALINA_HOME}/analytics-conf/job-acton.properties \
                -Danalytics.job.jrebel.properties=${CATALINA_HOME}/analytics-conf/job-jrebel.properties \
                -Danalytics.job.checklogs.properties=${CATALINA_HOME}/analytics-conf/job-checklogs.properties \
                -Dcom.codenvy.analytics.logpath=${CATALINA_HOME}/logs"

QUARTZ_OPTS="-Dorg.terracotta.quartz.skipUpdateCheck=true"

JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=true \
          -Dcom.sun.management.jmxremote.password.file=${CATALINA_HOME}/conf/jmxremote.password \
          -Dcom.sun.management.jmxremote.access.file=${CATALINA_HOME}/conf/jmxremote.access \
          -Dcom.sun.management.jmxremote.ssl=false"

#uncomment if you want to debug app server
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"

export JAVA_OPTS="$JAVA_OPTS $SECURITY_OPTS $ANALYTICS_OPTS $JMX_OPTS $REMOTE_DEBUG $QUARTZ_OPTS"
export CLASSPATH="${CATALINA_HOME}/conf/:${CATALINA_HOME}/lib/jul-to-slf4j.jar:\
${CATALINA_HOME}/lib/slf4j-api.jar:${CATALINA_HOME}/lib/logback-classic.jar:${CATALINA_HOME}/lib/logback-core.jar:\
${CATALINA_HOME}/lib/mail.jar"

echo "======="
echo $JAVA_OPTS
echo "======="

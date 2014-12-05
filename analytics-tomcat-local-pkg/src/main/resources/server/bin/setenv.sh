#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2014] Codenvy, S.A.
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
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx2G -XX:MaxPermSize=256m -XX:+UseCompressedOops"

# Set path to organization service server
[ -z "${ORGANIZATION_SERVICE_APPLICATION_SERVER_URL}" ] && ORGANIZATION_SERVICE_APPLICATION_SERVER_URL="http://localhost:8080/organization/"
[ -z "${CODENVY_LOCAL_CONF_DIR}" ] && CODENVY_LOCAL_CONF_DIR="$CATALINA_HOME/data/conf"


ANALYTICS_OPTS="-Dcodenvy.local.conf.dir=${CODENVY_LOCAL_CONF_DIR} \
                -Dorganization.application.server.url=${ORGANIZATION_SERVICE_APPLICATION_SERVER_URL} \
                -Danalytics.logback.smtp-appender.configuration=${CODENVY_LOCAL_CONF_DIR}/logback-smtp-appender.xml \
                -Dcom.codenvy.analytics.logpath=${CATALINA_HOME}/logs"
JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=true \
          -Dcom.sun.management.jmxremote.password.file=${CATALINA_HOME}/conf/jmxremote.password \
          -Dcom.sun.management.jmxremote.access.file=${CATALINA_HOME}/conf/jmxremote.access \
          -Dcom.sun.management.jmxremote.ssl=false"
QUARTZ_OPTS="-Dorg.terracotta.quartz.skipUpdateCheck=true"
REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
SECURITY_OPTS="-Djava.security.auth.login.config=${CATALINA_HOME}/conf/jaas.conf"

export CATALINA_HOME
export CATALINA_TMPDIR
export CODENVY_LOCAL_CONF_DIR
export JAVA_OPTS="$JAVA_OPTS $SECURITY_OPTS $ANALYTICS_OPTS $JMX_OPTS $REMOTE_DEBUG $QUARTZ_OPTS $JMX_OPTS"
export CLASSPATH="${CATALINA_HOME}/conf/:${CATALINA_HOME}/endorsed/jul-to-slf4j.jar:\
${CATALINA_HOME}/endorsed/slf4j-api.jar:${CATALINA_HOME}/endorsed/logback-classic.jar:${CATALINA_HOME}/endorsed/logback-core.jar:\
${CATALINA_HOME}/endorsed/mail.jar"

echo "Using LOCAL_CONF_DIR:  $CODENVY_LOCAL_CONF_DIR"

#
# CODENVY CONFIDENTIAL
# __________________
#
#  [2012] - [2013] Codenvy, S.A.
#  All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any.  The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

# Environment Variable Prerequisites
#
# JAVA_OPTS override jvm options for example: Xmx, Xms etc.

if [ -z "${CODENVY_LOCAL_CONF_DIR}" ]; then
    echo "Need to set CODENVY_LOCAL_CONF_DIR"
    exit 1
fi

# Set global cloud names
[ -z "${CODENVY_DATA_DIR}" ]  && CODENVY_DATA_DIR="${CATALINA_HOME}/data"

[ -z "${CODENVY_LOGS_DIR}" ]  && CODENVY_LOGS_DIR="${CATALINA_HOME}/logs/cloud"

# Sets some variables
SECURITY_OPTS="-Djava.security.auth.login.config=${CATALINA_HOME}/conf/jaas.conf"

CODENVY_GENERAL="    -Dcodenvy.data.dir=${CODENVY_DATA_DIR} \
                     -Dcodenvy.local.conf.dir=${CODENVY_LOCAL_CONF_DIR} \
                     -Dcodenvy.logs.dir=${CODENVY_LOGS_DIR} "

JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=true \
          -Dcom.sun.management.jmxremote.password.file=${CATALINA_HOME}/conf/jmxremote.password \
          -Dcom.sun.management.jmxremote.access.file=${CATALINA_HOME}/conf/jmxremote.access \
          -Dcom.sun.management.jmxremote.ssl=false"
                              

#uncomment if you want to debug app server
#REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
REMOTE_DEBUG=""


export JAVA_OPTS="$JAVA_OPTS $CODENVY_GENERAL $REMOTE_DEBUG $JMX_OPTS $SECURITY_OPTS"
export CLASSPATH="${CATALINA_HOME}/conf/"


echo "======="
echo $JAVA_OPTS
echo "======="


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
#
#
# TENANT_MASTERHOST - name of the host visible for user without www and port.
#                     Example: localhost, cloudide.exoplatform.com
# 
# TENANT_MASTERPORT - port of server deployment
#
# TENANT_REPOSITORY - name of the repository of default tenant.  

if [ -z "${CODENVY_LOCAL_CONF_DIR}" ]; then
    echo "Need to set CODENVY_LOCAL_CONF_DIR"
    exit 1
fi

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx2048m -XX:MaxPermSize=256m -server"

# binding, jcr index, backup dir
[ -z "${CODENVY_DATA_DIR}" ]  && CODENVY_DATA_DIR="${CATALINA_HOME}/data"

#Logs dir
[ -z "${CODENVY_LOGS_DIR}" ]  && CODENVY_LOGS_DIR="${CATALINA_HOME}/data/logs"

# Path to IDE publish repository service
[ -z "${BUILDER_PUBLISH_REPOSITORY_URL}" ] && BUILDER_PUBLISH_REPOSITORY_URL="http://${TENANT_MASTERHOST}:${TENANT_MASTERPORT}/releases"

[ -z "${BUILDER_PUBLISH_REPOSITORY_PATH}" ] && BUILDER_PUBLISH_REPOSITORY_PATH="${CODENVY_DATA_DIR}/releases"

defineGeneralEnvironment(){
  CODENVY_GENERAL="   -Dcodenvy.data.dir=${CODENVY_DATA_DIR} \
                      -Dcodenvy.local.conf.dir=${CODENVY_LOCAL_CONF_DIR} \
                      -Dcodenvy.logs.dir=${CODENVY_LOGS_DIR} "

  EXO_OPTS="          -Dexo.product.developing=false"

  LOG_OPTS="          -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog \
                      -Dtenant.logs.dir=${CODENVY_LOGS_DIR} \
                      -Dcom.codenvy.logreader.logpath=${CODENVY_LOGS_DIR} "

  IDE_OPTS="          -Dorg.exoplatform.mimetypes=conf/mimetypes.properties \
                      -Dcom.codenvy.vfs.rootdir=${CODENVY_DATA_DIR}/fs \
                      -Dappengine.sdk.root=${GAE_JAVA_SDK_HOME} "

  JMX_OPTS="          -Dcom.sun.management.jmxremote.authenticate=true \
                      -Dcom.sun.management.jmxremote.password.file=${CATALINA_HOME}/conf/jmxremote.password \
                      -Dcom.sun.management.jmxremote.access.file=${CATALINA_HOME}/conf/jmxremote.access \
                      -Dcom.sun.management.jmxremote.ssl=false"

  MAVEN_BUILDER_OPTS="-Dbuilder.publish-repository=${BUILDER_PUBLISH_REPOSITORY_PATH} \
                      -Dbuilder.publish-repository-url=${BUILDER_PUBLISH_REPOSITORY_URL}"

  #uncomment if you want to debug app server
  #REMOTE_DEBUG=" -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
  REMOTE_DEBUG=""

  export CODENVY_LOCAL_CONF_DIR

  CODENVY_SECURITY_OPTS="-Djava.security.properties=${CATALINA_BASE}/conf/codenvy-security.properties"
  #CODENVY_SECURITY_OPTS=""

  export JAVA_OPTS="${JAVA_OPTS} ${CODENVY_GENERAL} ${EXO_OPTS} ${LOG_OPTS} ${IDE_OPTS} ${MAVEN_BUILDER_OPTS} ${JMX_OPTS} ${REMOTE_DEBUG} ${CODENVY_SECURITY_OPTS}"

}

# Environment specific variables for standalone bundle
defineStandaloneEnvironment(){
     SECURITY_OPTS="-Djava.security.auth.login.config=${CATALINA_HOME}/conf/jaas.conf "

     CODEASSISTANT_OPTS="-Dlocal.conf.dir=${CODENVY_LOCAL_CONF_DIR} \
                         -Dcodeassistant.log.dir=${CODENVY_LOGS_DIR} \
                         -Dcodeassistant.storage-path=${CODENVY_DATA_DIR}/ide-codeassistant-lucene-index"

     export JAVA_OPTS="${JAVA_OPTS} ${SECURITY_OPTS} ${CODEASSISTANT_OPTS}"
}

defineClassPath() {
  export CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar:${CATALINA_HOME}/lib/codenvy-ssl-socket-factory.jar:${CODENVY_DATA_DIR}/jdbc_drivers/*"
}


echo '  _____            _                            '
echo ' / ____|          | |                           '
echo '| |      ___    __| |  ___  _ __  __   __ _   _ '
echo '| |     / _ \  / _` | / _ \| "_  \\ \ / /| | | |'
echo '| |____| (_) || (_| ||  __/| | | | \ V / | |_| |'
echo ' \_____|\___/  \__,_| \___||_| |_|  \_/   \__, |'
echo '                                          __/ / '
echo '                                         |___/  '
echo '  _____  _       _____  _____  _____   ______   '
echo ' / ____|| |     |  __ \|_   _||  __ \ |  ____|  '
echo '| |     | |     | |  | | | |  | |  | || |__     '
echo '| |     | |     | |  | | | |  | |  | ||  __|    '
echo '| |____ | |____ | |__| |_| |_ | |__| || |____   '
echo ' \_____||______||_____/|_____||_____/ |______|  '


defineGeneralEnvironment
defineStandaloneEnvironment
defineClassPath

echo "======="
echo ${JAVA_OPTS}
echo "======="

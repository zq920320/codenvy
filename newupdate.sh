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

# To run server update type
# ./newupdate.sh [TARGET_CLOUD] [UPDATE_SCOPE]
# where TARGET_CLOUD is identifier of server to update
# and UPDATE_SCOPE is server with application to update
# If server use standalone build (site, ide, api and other applications in one tomcat)
# second parameter is not needed
#
# TARGET_CLOUD parameter possible values
#   Servers with standalone build : pre-prod, a1, a2, a3, a4, a5
#   Servers with common build     :  t2, qa, t3, stg
# UPDATE_SCOPE parameter possible values
#   site, ide, api, builder, codeassistant, git, all
# ##################################################################################################
# To update staging or production with AS image you need to set-up such environment:
# 1. SSH keys for access to the server: as1-cldide_cl-server.skey and keycldidestg.pem in ~/.ssh folder;
# 2. ec2-api tools in home folder;
# 3. ~/.staging file with following content:
#
# export AWS_ACCESS_KEY="AWS_ACCESS_KEY_HERE"
# export AWS_SECRET_KEY="aws_secret_key_here"
# export EC2_HOME="/home/user/ec2-tools"  #Path to ec2 tools here
# export PATH=$EC2_HOME/bin:$PATH



case "$1" in
   "prod" | "stg" | "qa")
      echo "Selected '$1' as cloud for update"
      TARGET_CLOUD=$1
   ;;
   *)
      echo "Need to specify alias of the cloud what should be updated as the first argument. Possible values: qa, stg, prod."
      exit 1
   ;;
esac

case "${TARGET_CLOUD}" in
   "pre-prod" | "a1" | "a2" | "a3" | "a4" | "a5")
      echo "Selected 'allinone' as scope for update"
      UPDATE_SCOPE='allinone'
      shift
   ;;
   *)
      case "$2" in
         "site" | "ide" | "api" | "all" | "builder" | "codeassistant" | "git" | "next-builder" | "next-runner" | "datasource" | "next-codeassistant")
            echo "Selected '$2' as scope for update"
            UPDATE_SCOPE=$2
            shift
            shift
         ;;
         *)
            echo "Need to specify scope of update as the second argument if standalone build is not used. Possible values of second argument site, ide, api, git, builder, codeassistant, next-builder, next-runner, datasource, next-codeassistant all"
            exit 1
         ;;
      esac
      ;;
esac

filename=`date '+%y%m%d-%H%M'`
SSH_USER_NAME='cl-server'
NO_BUILD=false
MAVEN_PARAMS=""

set -e

for i in "$@"
do
  case "$i" in
    --notests)
      MAVEN_PARAMS=${MAVEN_PARAMS}"  -Dmaven.test.skip=true"
      ;;
    --nobuild)
      NO_BUILD=true
      ;;
    -U)
     MAVEN_PARAMS=${MAVEN_PARAMS}"  -U"
      ;;
    *)
      echo "Unknown parameter $i."
      exit 1
      ;;
  esac
done

JPDA="jpda"
LOG_SYMLINK="false"
if [ "${TARGET_CLOUD}" == "prod" ]; then
  JPDA=""
  LOG_SYMLINK="true"
fi


setUpVariablesDependsOnCloud() {
   echo "Set up variables for cloud $1 ....."
   case "$1" in
      "prod")
         SERVER_JVM_HOME='/usr/local/jdk1.7.0_17'
         API_IP='apic.codenvycorp.com'
         SITE_IP='proxy.codenvycorp.com'
         IDE_IP='idec.codenvycorp.com'
         CODEASSISTANT_IP='storage.codenvycorp.com'
         CODEASSISTANT_NEXT_IP='codeassistant.codenvycorp.com'
         MAVEN_BUILDER_IP='builder.codenvycorp.com'
         DATASOURCE_SERVER_IP='datasource.codenvycorp.com'
         GIT_SERVER_IP='gitc.codenvycorp.com'
         SSH_KEY_NAME='cl-server-prod-20130219'
         SSH_UPDATE_KEY_NAME='keycldidestg.pem'
         TENANT_MASTERHOST='codenvy.com'
         TENANT_MASTERHOST_PROTOCOL='https'
         SERVER_TYPE="cloud-agent"
      ;;
      "stg")
         SERVER_JVM_HOME='/usr/local/jdk1.7.0_17'
         API_IP='apic.codenvy-stg.com'
         SITE_IP='codenvy-stg.com'
         IDE_IP='idec.codenvy-stg.com'
         CODEASSISTANT_IP='storage.codenvy-stg.com'
         CODEASSISTANT_NEXT_IP='codeassistant.codenvy-stg.com'
         MAVEN_BUILDER_IP='builder.codenvy-stg.com'
         DATASOURCE_SERVER_IP='datasource-ide3.codenvy-stg.com'
         NEXT_BUILDER_IP='builder-ide3.codenvy-stg.com'
#         RUNNER1='runner0.codenvy-stg.com'
#         RUNNER2='runner1.codenvy-stg.com'
#         RUNNER3='runner15-0.codenvy-stg.com'
#         RUNNER4='runner15-3.codenvy-stg.com'
         GIT_SERVER_IP='gitc.codenvy-stg.com'
         SSH_KEY_NAME='as1-cldide_cl-server.skey'
         SSH_UPDATE_KEY_NAME='keycldidestg.pem'
         TENANT_MASTERHOST='codenvy-stg.com'
         TENANT_MASTERHOST_PROTOCOL='https'
         SERVER_TYPE="cloud-agent"
      ;;
      "qa")
         SERVER_JVM_HOME='/usr/local/jdk1.7.0_17'
         API_IP='vt0-13.ua.codenvy-dev.com'
         SITE_IP='vt0-12.ua.codenvy-dev.com'
         IDE_IP='vt0-14.ua.codenvy-dev.com'
         CODEASSISTANT_IP='vt0-16.ua.codenvy-dev.com'
         CODEASSISTANT_NEXT_IP='vt0-06.ua.codenvy-dev.com'
         MAVEN_BUILDER_IP='vt0-05.ua.codenvy-dev.com'
         GIT_SERVER_IP='vt0-15.ua.codenvy-dev.com'
         DATASOURCE_SERVER_IP='vt0-03.ua.codenvy-dev.com'
         NEXT_BUILDER_IP='vt0-01.ua.codenvy-dev.com'
         NEXT_RUNNER_IP='vt0-02.ua.codenvy-dev.com'
         SSH_KEY_NAME='admin.key'
         TENANT_MASTERHOST='codenvy-dev.com'
         TENANT_MASTERHOST_PROTOCOL='http'
      ;;
      *)
         echo "Need to specify alias of the cloud what should be updated as the first argument. Possible values qa, stg, prod"
         exit 1
      ;;
   esac
}

deleteFileIfExists() {
   if [ -f $1 ]; then
      echo $1
      rm -rf $1
   fi
}

checkEnv() {
  var='$'$3
  result=$(ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no $1@$2  "echo ${var} ")

  if [ -z "${result}" ] ; then
    echo "Environment variable $3 is not set on $2, exiting.";
    exit 1
  fi

  if [ -n "$4" ] && [ "$4" != "${result}" ] ; then
    echo "Value of variable $3 <${result}> does not match expected <$4>"
    exit 1
  fi
}

doUpdateIde() {
   echo "Updating IDE in the cloud $1 ....."

#   checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
   #checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "TENANT_MASTERHOST" "${TENANT_MASTERHOST}"
   #checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "TENANT_MASTERHOST_PROTOCOL" "${TENANT_MASTERHOST_PROTOCOL}"

   #checkEnv "$SSH_USER_NAME" "$AS_IP" "JMX0"
   #checkEnv "$SSH_USER_NAME" "$AS_IP" "JMX1"

   checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "CODENVY_LOCAL_CONF_DIR"
   checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "CODENVY_DATA_DIR"
   checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "CODENVY_LOGS_DIR"

   #checkEnv "${SSH_USER_NAME}" "${IDE_IP}" "MAILSENDER_APPLICATION_SERVER_URL" "http://${API_IP}:8080/mail/"
   
   cd cloud-ide-packaging-tomcat-ide-codenvy/target

   echo "upload new tomcat... to ${IDE_IP}"
   scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-ide-codenvy*.zip ${SSH_USER_NAME}@${IDE_IP}:tomcat-ide-${filename}.zip
   echo "stop existed tomcat.."
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "mkdir -p codenvy-tomcat/tomcat-ide/bin/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "cd codenvy-tomcat/tomcat-ide/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#   echo "backup local configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "cp -rf codenvy-tomcat/tomcat-ide/conf/server.xml ."
   echo "delete existed tomcat.."
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "rm -rf codenvy-tomcat/tomcat-ide"
   echo "unpack new tomcat..."
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "mv tomcat-ide-${filename}.zip codenvy-tomcat/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "unzip codenvy-tomcat/tomcat-ide-${filename}.zip -d  codenvy-tomcat/tomcat-ide/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "rm -rf codenvy-tomcat/tomcat-ide-${filename}.zip"
#   echo "restore backup configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "mv -f server.xml codenvy-tomcat/tomcat-ide/conf"
   echo "start new tomcat... on ${IDE_IP}"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP} "cd codenvy-tomcat/tomcat-ide/bin;./catalina.sh ${JPDA} start"
   #echo "wait 60 sec before enabling application server on admin"
   #sleep 60
   SERVER_STATE='Starting'
   COUNTER=0
   testfile=/tmp/catalin.out
   while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}

      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${IDE_IP}:codenvy-tomcat/tomcat-ide/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of IDE started"
         SERVER_STATE=Started
      fi

      echo "IDE state = ${SERVER_STATE}  Attempt ${COUNTER}"
      sleep 5
      let COUNTER=COUNTER+1
      deleteFileIfExists ${testfile}
   done
}

doUpdateSite() {
   echo "Updating SITE in the cloud $1 ....."

#   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
#   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "TENANT_MASTERHOST" "${TENANT_MASTERHOST}"
#   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "TENANT_MASTERHOST_PROTOCOL" "${TENANT_MASTERHOST_PROTOCOL}"

   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "CODENVY_DATA_DIR"
   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "CODENVY_LOGS_DIR"
   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "CODENVY_LOCAL_CONF_DIR"

#   checkEnv "${SSH_USER_NAME}" "${SITE_IP}" "MAILSENDER_APPLICATION_SERVER_URL" "http://${API_IP}:8080/mail/"

   cd cloud-ide-packaging-tomcat-site/target
   scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-site*.zip ${SSH_USER_NAME}@${SITE_IP}:tomcat-site-${filename}.zip
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "mkdir -p codenvy-tomcat/tomcat-site/bin/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "cd codenvy-tomcat/tomcat-site/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#   echo "backup local configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "cp -rf codenvy-tomcat/tomcat-site/conf/server.xml ."
   echo "delete existed tomcat.."
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "rm -rf codenvy-tomcat/tomcat-site"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "mkdir codenvy-tomcat/tomcat-site"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "mv tomcat-site-${filename}.zip codenvy-tomcat/tomcat-site"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "unzip codenvy-tomcat/tomcat-site/tomcat-site-${filename}.zip -d codenvy-tomcat/tomcat-site/"
   if [ "${LOG_SYMLINK}" != "false" ]; then
     ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "rm -rf codenvy-tomcat/tomcat-site/logs"
     ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "ln -s  ~/logs  ~/codenvy-tomcat/tomcat-site/logs"
     ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "mv ~/logs/catalina.out ~/logs/catalina.out_`date +%Y-%m-%d--%H:%M:%S`"
   fi
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "rm -f \$CODENVY_DATA_DIR/ticket-manager-data"
#   echo "restore backup configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "mv -f server.xml codenvy-tomcat/tomcat-site/conf"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP} "cd codenvy-tomcat/tomcat-site/bin/; ./catalina.sh ${JPDA} start"

   SERVER_STATE='Starting'
   COUNTER=0
   testfile=/tmp/catalin.out
   while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}

      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${SITE_IP}:codenvy-tomcat/tomcat-site/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during site tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat site started"
         SERVER_STATE=Started
      fi

      echo "Site state = ${SERVER_STATE}  Attempt ${COUNTER}"
      sleep 5
      let COUNTER=COUNTER+1
      deleteFileIfExists ${testfile}
   done

}

doUpdateApi() {
   echo "Updating api tomcat in the cloud $1 ....."

#   checkEnv "${SSH_USER_NAME}" "${API_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
   #checkEnv "$SSH_USER_NAME" "$API_IP" "JMX0"
   #checkEnv "$SSH_USER_NAME" "$API_IP" "JMX1"

   checkEnv "${SSH_USER_NAME}" "${API_IP}" "CODENVY_LOCAL_CONF_DIR"
   checkEnv "${SSH_USER_NAME}" "${API_IP}" "CODENVY_DATA_DIR"
   checkEnv "${SSH_USER_NAME}" "${API_IP}" "CODENVY_LOGS_DIR"

   cd cloud-ide-packaging-tomcat-api/target
   scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-api-*.zip ${SSH_USER_NAME}@${API_IP}:tomcat-api-${filename}.zip

   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "mkdir -p codenvy-tomcat/tomcat-api/bin/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "cd codenvy-tomcat/tomcat-api/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
 #  echo "backup local configuration..."
 #  ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "cp -rf codenvy-tomcat/tomcat-api/conf/server.xml ."
   echo "delete existed tomcat.."
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "rm -rf codenvy-tomcat/tomcat-api"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "mkdir codenvy-tomcat/tomcat-api"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "mv tomcat-api-${filename}.zip  codenvy-tomcat/tomcat-api"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "unzip codenvy-tomcat/tomcat-api/tomcat-api-${filename}.zip -d codenvy-tomcat/tomcat-api/"
#   echo "restore backup configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "mv -f server.xml codenvy-tomcat/tomcat-api/conf"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP} "cd codenvy-tomcat/tomcat-api/bin/; ./catalina.sh ${JPDA} start"

   SERVER_STATE='Starting'
   COUNTER=0
   testfile=/tmp/catalin.out
   while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}

      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${API_IP}:codenvy-tomcat/tomcat-api/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occur during api tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat api started"
         SERVER_STATE=Started
      fi

      echo "api state = ${SERVER_STATE}  Attempt ${COUNTER}"
     sleep 5
      let COUNTER=COUNTER+1
      deleteFileIfExists ${testfile}
   done
}

doUpdateGit() {
   echo "Updating git server tomcat in the cloud $1 ....."


 #  checkEnv "${SSH_USER_NAME}" "${GIT_SERVER_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
   #checkEnv "$SSH_USER_NAME" "$API_IP" "JMX0"
   #checkEnv "$SSH_USER_NAME" "$API_IP" "JMX1"

   checkEnv "${SSH_USER_NAME}" "${GIT_SERVER_IP}" "CODENVY_LOCAL_CONF_DIR"
   checkEnv "${SSH_USER_NAME}" "${GIT_SERVER_IP}" "CODENVY_DATA_DIR"
   checkEnv "${SSH_USER_NAME}" "${GIT_SERVER_IP}" "CODENVY_LOGS_DIR"
   #checkEnv "${SSH_USER_NAME}" "${GIT_SERVER_IP}" "ORGANIZATION_SERVICE_APPLICATION_SERVER_URL" "http://${API_IP}:8080/organization/"

   cd cloud-ide-packaging-tomcat-git/target
   scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-git-*.zip ${SSH_USER_NAME}@${GIT_SERVER_IP}:tomcat-git-${filename}.zip

   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "mkdir -p codenvy-tomcat/tomcat-git/bin/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "cd codenvy-tomcat/tomcat-git/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#   echo "backup local configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "cp -rf codenvy-tomcat/tomcat-git/conf/server.xml ."
   echo "delete existed tomcat.."
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "rm -rf codenvy-tomcat/tomcat-git"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "mkdir codenvy-tomcat/tomcat-git"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "mv tomcat-git-${filename}.zip  codenvy-tomcat/tomcat-git"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "unzip codenvy-tomcat/tomcat-git/tomcat-git-${filename}.zip -d codenvy-tomcat/tomcat-git/"
 #  echo "restore backup configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "mv -f server.xml codenvy-tomcat/tomcat-git/conf"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP} "cd codenvy-tomcat/tomcat-git/bin/; ./catalina.sh ${JPDA} start"

   SERVER_STATE='Starting'
   COUNTER=0
   testfile=/tmp/catalin.out
   while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}

      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${GIT_SERVER_IP}:codenvy-tomcat/tomcat-git/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occur during git server tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi

      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of git server started"
         SERVER_STATE='Started'
      fi

      echo "git server state = ${SERVER_STATE}  Attempt ${COUNTER}"
      sleep 5
      let COUNTER=COUNTER+1
      deleteFileIfExists ${testfile}
   done

}

doUpdateCodeAssistant() {
   echo "Updating codeassistant tomcat in the cloud $1 ....."

 #  checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
   checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_IP}" "CODENVY_LOCAL_CONF_DIR"
   checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_IP}" "CODENVY_LOGS_DIR"
   checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_IP}" "CODENVY_DATA_DIR"
   
   cd cloud-ide-packaging-tomcat-codeassistant/target
   scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-codeassistant-*.zip ${SSH_USER_NAME}@${CODEASSISTANT_IP}:tomcat-ide-codeassistant-storage-${filename}.zip
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "mkdir -p codenvy-tomcat/tomcat-ide-codeassistant-storage/bin/"
   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "cd codenvy-tomcat/tomcat-ide-codeassistant-storage/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#   echo "backup local configuration..."
#   ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "cp -rf codenvy-tomcat/tomcat-ide-codeassistant-storage/conf/server.xml ."
    echo "delete existed tomcat.."
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "rm -rf codenvy-tomcat/tomcat-ide-codeassistant-storage"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "mkdir codenvy-tomcat/tomcat-ide-codeassistant-storage"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "mv tomcat-ide-codeassistant-storage-${filename}.zip  codenvy-tomcat/tomcat-ide-codeassistant-storage"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "unzip codenvy-tomcat/tomcat-ide-codeassistant-storage/tomcat-ide-codeassistant-storage-${filename}.zip -d codenvy-tomcat/tomcat-ide-codeassistant-storage/"
    echo "restore backup configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "mv -f server.xml codenvy-tomcat/tomcat-ide-codeassistant-storage/conf"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP} "cd codenvy-tomcat/tomcat-ide-codeassistant-storage/bin/; ./catalina.sh ${JPDA} start"
    
    SERVER_STATE='Starting'
    COUNTER=0
    testfile=/tmp/catalin.out
    while [[ "${SERVER_STATE}" != "Started" ]]; do
       deleteFileIfExists ${testfile}

       scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_IP}:codenvy-tomcat/tomcat-ide-codeassistant-storage/logs/catalina.out ${testfile}

       if grep -Fq "[ERROR]" ${testfile}
       then
         echo "Exeption occured during codeassistant tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
       fi


       if grep -Fq "Server startup" ${testfile}
       then
         echo "Tomcat of Codeassistant started"
         SERVER_STATE=Started
       fi

       echo "Codeassistant ${AS_ID} state = ${SERVER_STATE}  Attempt ${COUNTER}"
       sleep 5
       let COUNTER=COUNTER+1
       deleteFileIfExists ${testfile}
  done
}

doUpdateMavenBuilder() {
    echo "Updating builder tomcat in the cloud $1 ....."

#    checkEnv "${SSH_USER_NAME}" "${MAVEN_BUILDER_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
    checkEnv "${SSH_USER_NAME}" "${MAVEN_BUILDER_IP}" "CODENVY_LOCAL_CONF_DIR"
    checkEnv "${SSH_USER_NAME}" "${MAVEN_BUILDER_IP}" "CODENVY_LOGS_DIR"
    checkEnv "${SSH_USER_NAME}" "${MAVEN_BUILDER_IP}" "CODENVY_DATA_DIR"
    
    cd cloud-ide-packaging-tomcat-maven-builder/target

    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-maven-builder-*.zip ${SSH_USER_NAME}@${MAVEN_BUILDER_IP}:tomcat-ide-builder-${filename}.zip
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "mkdir -p codenvy-tomcat/tomcat-ide-builder/bin/"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "cd codenvy-tomcat/tomcat-ide-builder/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#    echo "backup local configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "cp -rf codenvy-tomcat/tomcat-ide-builder/conf/server.xml ."
    echo "delete existed tomcat.."
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "rm -rf codenvy-tomcat/tomcat-ide-builder"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "mkdir codenvy-tomcat/tomcat-ide-builder"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "mv tomcat-ide-builder-${filename}.zip  codenvy-tomcat/tomcat-ide-builder"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "unzip codenvy-tomcat/tomcat-ide-builder/tomcat-ide-builder-${filename}.zip -d codenvy-tomcat/tomcat-ide-builder/"
#    echo "restore backup configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "mv -f server.xml codenvy-tomcat/tomcat-ide-builder/conf"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP} "cd codenvy-tomcat/tomcat-ide-builder/bin/; ./catalina.sh ${JPDA} start"
    
    SERVER_STATE='Starting'
    COUNTER=0
    testfile=/tmp/catalin.out
    while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}
      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${MAVEN_BUILDER_IP}:codenvy-tomcat/tomcat-ide-builder/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during builder tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of Maven-Builder started"
         SERVER_STATE=Started
     fi

     echo "Builder state = ${SERVER_STATE}  Attempt ${COUNTER}"
     sleep 5
     let COUNTER=COUNTER+1
     deleteFileIfExists ${testfile}
   done
}

doUpdateNextBuilder() {
    echo "Updating next builder tomcat in the cloud $1 ....."

#    checkEnv "${SSH_USER_NAME}" "${NEXT_BUILDER_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
    checkEnv "${SSH_USER_NAME}" "${NEXT_BUILDER_IP}" "CODENVY_LOCAL_CONF_DIR"
    checkEnv "${SSH_USER_NAME}" "${NEXT_BUILDER_IP}" "CODENVY_LOGS_DIR"
    checkEnv "${SSH_USER_NAME}" "${NEXT_BUILDER_IP}" "CODENVY_DATA_DIR"
    
    cd cloud-ide-packaging-tomcat-next-builder/target

    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-next-builder-*.zip ${SSH_USER_NAME}@${NEXT_BUILDER_IP}:tomcat-next-builder-${filename}.zip
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "mkdir -p codenvy-tomcat/tomcat-next-builder/bin/"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "cd codenvy-tomcat/tomcat-next-builder/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#    echo "backup local configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "cp -rf codenvy-tomcat/tomcat-next-builder/conf/server.xml ."
    echo "delete existed tomcat.."
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "rm -rf codenvy-tomcat/tomcat-next-builder"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "mkdir codenvy-tomcat/tomcat-next-builder"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "mv tomcat-next-builder-${filename}.zip  codenvy-tomcat/tomcat-next-builder"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "unzip codenvy-tomcat/tomcat-next-builder/tomcat-next-builder-${filename}.zip -d codenvy-tomcat/tomcat-next-builder/"
#    echo "restore backup configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "mv -f server.xml codenvy-tomcat/tomcat-next-builder/conf"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP} "cd codenvy-tomcat/tomcat-next-builder/bin/; ./catalina.sh ${JPDA} start"
    
    SERVER_STATE='Starting'
    COUNTER=0
    testfile=/tmp/catalin.out
    while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}
      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${NEXT_BUILDER_IP}:codenvy-tomcat/tomcat-next-builder/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during next builder tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of Next-Builder started"
         SERVER_STATE=Started
     fi

     echo "Next Builder state = ${SERVER_STATE}  Attempt ${COUNTER}"
     sleep 5
     let COUNTER=COUNTER+1
     deleteFileIfExists ${testfile}
   done
}

doUpdateNextRunner() {
    echo "Updating next runner tomcat in the cloud $1 ....."
    RUNNER_DNS=$2
#    checkEnv "${SSH_USER_NAME}" "${RUNNER_DNS}" "JAVA_HOME" "${SERVER_JVM_HOME}"
    checkEnv "${SSH_USER_NAME}" "${RUNNER_DNS}" "CODENVY_LOCAL_CONF_DIR"
    checkEnv "${SSH_USER_NAME}" "${RUNNER_DNS}" "CODENVY_LOGS_DIR"
    checkEnv "${SSH_USER_NAME}" "${RUNNER_DNS}" "CODENVY_DATA_DIR"
    
    cd cloud-ide-packaging-tomcat-next-runner/target

    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-next-runner-*.zip ${SSH_USER_NAME}@${RUNNER_DNS}:tomcat-next-runner-${filename}.zip
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "mkdir -p codenvy-tomcat/tomcat-next-runner/bin/"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "cd codenvy-tomcat/tomcat-next-runner/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#    echo "backup local configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "cp -rf codenvy-tomcat/tomcat-next-runner/conf/server.xml ."
    echo "delete existed tomcat.."
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "rm -rf codenvy-tomcat/tomcat-next-runner"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "mkdir codenvy-tomcat/tomcat-next-runner"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "mv tomcat-next-runner-${filename}.zip  codenvy-tomcat/tomcat-next-runner"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "unzip codenvy-tomcat/tomcat-next-runner/tomcat-next-runner-${filename}.zip -d codenvy-tomcat/tomcat-next-runner/"
#    echo "restore backup configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "mv -f server.xml codenvy-tomcat/tomcat-next-runner/conf"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS} "cd codenvy-tomcat/tomcat-next-runner/bin/; ./catalina.sh ${JPDA} start"
    
    SERVER_STATE='Starting'
    COUNTER=0
    testfile=/tmp/catalin.out
    while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}
      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${RUNNER_DNS}:codenvy-tomcat/tomcat-next-runner/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during next runner tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of next-runner started"
         SERVER_STATE=Started
     fi

     echo "Next runner state = ${SERVER_STATE}  Attempt ${COUNTER}"
     sleep 5
     let COUNTER=COUNTER+1
     deleteFileIfExists ${testfile}
   done
}

doUpdateDatasource() {
    echo "Updating datasource tomcat in the cloud $1 ....."

#    checkEnv "${SSH_USER_NAME}" "${DATASOURCE_SERVER_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
    checkEnv "${SSH_USER_NAME}" "${DATASOURCE_SERVER_IP}" "CODENVY_LOCAL_CONF_DIR"
    checkEnv "${SSH_USER_NAME}" "${DATASOURCE_SERVER_IP}" "CODENVY_LOGS_DIR"
    checkEnv "${SSH_USER_NAME}" "${DATASOURCE_SERVER_IP}" "CODENVY_DATA_DIR"
    
    cd cloud-ide-packaging-tomcat-datasource-plugin/target

    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-datasource-*.zip ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP}:tomcat-datasource-${filename}.zip
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "mkdir -p codenvy-tomcat/tomcat-datasource/bin/"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "cd codenvy-tomcat/tomcat-datasource/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#    echo "backup local configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "cp -rf codenvy-tomcat/tomcat-datasource/conf/server.xml ."
    echo "delete existed tomcat.."
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "rm -rf codenvy-tomcat/tomcat-datasource"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "mkdir codenvy-tomcat/tomcat-datasource"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "mv tomcat-datasource-${filename}.zip  codenvy-tomcat/tomcat-datasource"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "unzip codenvy-tomcat/tomcat-datasource/tomcat-datasource-${filename}.zip -d codenvy-tomcat/tomcat-datasource/"
#    echo "restore backup configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "mv -f server.xml codenvy-tomcat/tomcat-datasource/conf"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP} "cd codenvy-tomcat/tomcat-datasource/bin/; ./catalina.sh ${JPDA} start"
    
    SERVER_STATE='Starting'
    COUNTER=0
    testfile=/tmp/catalin.out
    while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}
      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${DATASOURCE_SERVER_IP}:codenvy-tomcat/tomcat-datasource/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during datasource tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of Datasource started"
         SERVER_STATE=Started
     fi

     echo "Datasource state = ${SERVER_STATE}  Attempt ${COUNTER}"
     sleep 5
     let COUNTER=COUNTER+1
     deleteFileIfExists ${testfile}
   done
}

###
doUpdateCodeassistantNext() {
    echo "Updating Codeassistant-next tomcat in the cloud $1 ....."

#    checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_NEXT_IP}" "JAVA_HOME" "${SERVER_JVM_HOME}"
    checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_NEXT_IP}" "CODENVY_LOCAL_CONF_DIR"
    checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_NEXT_IP}" "CODENVY_LOGS_DIR"
    checkEnv "${SSH_USER_NAME}" "${CODEASSISTANT_NEXT_IP}" "CODENVY_DATA_DIR"
    
    cd cloud-ide-packaging-tomcat-next-codeassistant/target

    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no cloud-ide-packaging-tomcat-next-codeassistant-*.zip ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP}:tomcat-codeassistant-next-${filename}.zip
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "mkdir -p codenvy-tomcat/tomcat-codeassistant-next/bin/"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "cd codenvy-tomcat/tomcat-codeassistant-next/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop -force; fi"
#    echo "backup local configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "cp -rf codenvy-tomcat/tomcat-codeassistant-next/conf/server.xml ."
    echo "delete existed tomcat.."
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "rm -rf codenvy-tomcat/tomcat-codeassistant-next"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "mkdir codenvy-tomcat/tomcat-codeassistant-next"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "mv tomcat-codeassistant-next-${filename}.zip  codenvy-tomcat/tomcat-codeassistant-next"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "unzip codenvy-tomcat/tomcat-codeassistant-next/tomcat-codeassistant-next-${filename}.zip -d codenvy-tomcat/tomcat-codeassistant-next/"
#    echo "restore backup configuration..."
#    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "mv -f server.xml codenvy-tomcat/tomcat-codeassistant-next/conf"
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP} "cd codenvy-tomcat/tomcat-codeassistant-next/bin/; ./catalina.sh ${JPDA} start"
    
    SERVER_STATE='Starting'
    COUNTER=0
    testfile=/tmp/catalin.out
    while [[ "${SERVER_STATE}" != "Started" ]]; do
      deleteFileIfExists ${testfile}
      scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${CODEASSISTANT_NEXT_IP}:codenvy-tomcat/tomcat-codeassistant-next/logs/catalina.out ${testfile}

      if grep -Fq "[ERROR]" ${testfile}
      then
         echo "Exeption occured during datasource tomcat start. Take a look ${testfile}. Exiting"
         cat ${testfile}
         exit 1
      fi


      if grep -Fq "Server startup" ${testfile}
      then
         echo "Tomcat of Datasource started"
         SERVER_STATE=Started
     fi

     echo "Datasource state = ${SERVER_STATE}  Attempt ${COUNTER}"
     sleep 5
     let COUNTER=COUNTER+1
     deleteFileIfExists ${testfile}
   done
}
#Set up global variables depends on cloud
setUpVariablesDependsOnCloud ${TARGET_CLOUD}
echo "Values of local variables are: SITE_IP=${SITE_IP} and SSH_KEY_NAME=${SSH_KEY_NAME}"

# Any subsequent commands which fail will cause the shell script to exit immediately
#rebuild all project
if [[ "${TARGET_CLOUD}" != "prod" && "${NO_BUILD}" == false ]]; then
  mvn clean install ${MAVEN_PARAMS}
fi


#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "api" ]]; then
#   doUpdateApi ${TARGET_CLOUD}
#   cd ../..
#fi

# site #
if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "site" ]]; then
   doUpdateSite ${TARGET_CLOUD}
   cd ../..
fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "ide" ]]; then
#   doUpdateIde ${TARGET_CLOUD}
#   cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "codeassistant" ]]; then
#   doUpdateCodeAssistant ${TARGET_CLOUD}
#   cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "builder" ]]; then
#    doUpdateMavenBuilder ${TARGET_CLOUD}
#    cd ../..
#fi


#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "datasource" ]]; then
#    doUpdateDatasource ${TARGET_CLOUD}
#    cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "next-builder" ]]; then
#    doUpdateNextBuilder ${TARGET_CLOUD}
#    cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "next-runner" ]]; then
#    doUpdateNextRunner ${TARGET_CLOUD} ${RUNNER1}
#    cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "next-runner" ]]; then
#    doUpdateNextRunner ${TARGET_CLOUD} ${RUNNER2}
#    cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "next-runner" ]]; then
#    doUpdateNextRunner ${TARGET_CLOUD} ${RUNNER3}
#    cd ../..
#fi

#if [[ "${UPDATE_SCOPE}" == "all" || "${UPDATE_SCOPE}" == "next-runner" ]]; then
#    doUpdateNextRunner ${TARGET_CLOUD} ${RUNNER4}
#    cd ../..
#fi

echo '================================================'
echo '                Update completed'
echo '================================================'


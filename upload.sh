#!/bin/bash
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
detect_user_for_upload() {
    case "${SERVER}" in
      "a1" | "a2" | "a3" | "a4" | "a5" | "dev" | "stg" | "t1" | "t2" | "t3" | "cf" | "demo" | "nightly" )
          SSH_KEY_NAME=upl-dev
          SSH_USER_NAME=upl-dev
      ;;
      "prod")
          SSH_KEY_NAME=upl-all
          SSH_USER_NAME=upl-all
      ;;
    esac
}

doUploadTomcat() {
echo 'Uploading:'
    FILE_LOCATION=$1
    UPLOAD_DESTINATION=$2
    FILE_NAME=`echo ${FILE_LOCATION} | cut -d "/" -f 3`
    FILE_NAME_WITH_POSTFIX=${FILE_NAME}_${DATE}
    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${CLOUD_IDE_HOME}/${FILE_LOCATION} ${SSH_USER_NAME}@${PUPPET_DNS}:/tmp/${FILE_NAME_WITH_POSTFIX}

    # override package name without version for dev servers
    if [ ${SERVER} != "prod" ]; then
        FILE_NAME=`echo ${FILE_NAME} | sed 's/-[0-9].*\.zip/.zip/g'`
    fi
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${PUPPET_DNS} "mv -f /tmp/${FILE_NAME_WITH_POSTFIX} /mnt/u03/puppet/files/servers/${UPLOAD_DESTINATION}/${FILE_NAME}"
}

selectTomcatToUpload() {
   case "$SCOPE" in
      "aio")
         AIO=`ls cloud-ide-packaging-tomcat-codenvy-allinone/target/cloud-ide-packaging-tomcat-codenvy-allinone-*.zip`
         doUploadTomcat ${AIO} ${SERVER}/${SCOPE}
      ;;
     "api")
         API=`ls cloud-ide-packaging-tomcat-api/target/cloud-ide-packaging-tomcat-api-*.zip`
         doUploadTomcat ${API} ${SERVER}/${SCOPE}
      ;;
     "runner")
         RUNNER=`ls cloud-ide-packaging-tomcat-next-runner/target/cloud-ide-packaging-tomcat-next-runner-*.zip`
         doUploadTomcat ${RUNNER} ${SERVER}/${SCOPE}
      ;;
     "builder")
         BUILDER=`ls cloud-ide-packaging-tomcat-next-builder/target/cloud-ide-packaging-tomcat-next-builder-*.zip`
         doUploadTomcat ${BUILDER} ${SERVER}/${SCOPE}
      ;;
     "site")
         SITE=`ls cloud-ide-packaging-tomcat-site/target/cloud-ide-packaging-tomcat-site-*.zip`
         doUploadTomcat ${SITE} ${SERVER}/${SCOPE}
      ;;
     "datasource")
         DATASOURCE=`ls cloud-ide-packaging-tomcat-datasource-plugin/target/cloud-ide-packaging-tomcat-datasource-plugin-*.zip`
         doUploadTomcat ${DATASOURCE} ${SERVER}/${SCOPE}
      ;;
     "codeassistant")
         CODEASSISTANT=`ls cloud-ide-packaging-tomcat-next-codeassistant/target/cloud-ide-packaging-tomcat-next-codeassistant-*.zip`
         doUploadTomcat ${CODEASSISTANT} ${SERVER}/${SCOPE}
      ;;
     "all")
         API=`ls cloud-ide-packaging-tomcat-api/target/cloud-ide-packaging-tomcat-api-*.zip`
         SITE=`ls cloud-ide-packaging-tomcat-site/target/cloud-ide-packaging-tomcat-site-*.zip`
         RUNNER=`ls cloud-ide-packaging-tomcat-next-runner/target/cloud-ide-packaging-tomcat-next-runner-*.zip`
         BUILDER=`ls cloud-ide-packaging-tomcat-next-builder/target/cloud-ide-packaging-tomcat-next-builder-*.zip`
         DATASOURCE=`ls cloud-ide-packaging-tomcat-datasource-plugin/target/cloud-ide-packaging-tomcat-datasource-plugin-*.zip`
         CODEASSISTANT=`ls cloud-ide-packaging-tomcat-next-codeassistant/target/cloud-ide-packaging-tomcat-next-codeassistant-*.zip`
         doUploadTomcat ${API} ${SERVER}/api
         doUploadTomcat ${SITE} ${SERVER}/site
         doUploadTomcat ${RUNNER} ${SERVER}/runner
         doUploadTomcat ${BUILDER} ${SERVER}/builder
         doUploadTomcat ${DATASOURCE} ${SERVER}/datasource
         doUploadTomcat ${CODEASSISTANT} ${SERVER}/codeassistant
      ;;
   esac
}

doBuild() {
    if [ "${NO_BUILD}" == false ]; then
        if [ "${TOMCATS_ONLY}" == true ]; then
            if [ "${SCOPE}" == "aio" ]; then
                cd cloud-ide-packaging-tomcat-codenvy-allinone
                mvn clean install ${MAVEN_PARAMS}
                cd ..
            else
                dirlist=(`ls | grep packaging-tomcat`)
                for i in ${dirlist[@]}; do
                    cd ${i}
                    mvn clean install ${MAVEN_PARAMS}
                    cd ..
                done
            fi
        else
            mvn clean install ${MAVEN_PARAMS}
        fi
    fi
}

PUPPET_DNS=puppet-master.codenvycorp.com
DATE=`date '+%y%m%d-%H%M%S'`
CLOUD_IDE_HOME=`pwd`
SCOPE_HELP="\033[31mNeed to select target to upload as first argument.\npossible values: aio, api, runner, builder, site, datasource, codeassistant, all\e[0m"
SERVER_HELP="\033[31mNeed to select server where to upload as second argument.\npossible values: a1, a2, a3, a4, a5, demo, cf, t1, t2, t3, nightly, dev, stg, prod\e[0m"
#checking possible scope values
case "$1" in
   "prod" | "stg" | "t3" |"t2" | "t1" | "cf" | "a1" | "a2" | "a3" | "a4" | "a5" | "demo" | "nightly" )
      echo "Selected '$1' as cloud for update"
      SERVER=$1
   ;;
   *)
      echo -e ${SERVER_HELP}
      exit 1
   ;;
esac

case "${SERVER}" in
    "a1" | "a2" | "a3" | "a4" | "a5")
      echo "Selected 'aio' as scope for update"
      SCOPE='aio'
      shift
   ;;
   *)
      case "$2" in
         "site" | "api" | "all" | "builder" | "runner" | "datasource" | "codeassistant" | "aio")
            if [[ "$2" != "aio" || "$SERVER" == "prod" ]]; then
                echo "Selected '$2' as scope for update"
                SCOPE=$2
                shift
                shift
            else
                echo "aio scope is not supported for $1 server"
                exit 1
            fi
         ;;
         *)
            echo  -e ${SCOPE_HELP}
            exit 1
         ;;
      esac
      ;;
esac

NO_BUILD=false
TOMCATS_ONLY=false
MAVEN_PARAMS=""

for i in "$@"
do
  case "$i" in
    --notests | --t)
      MAVEN_PARAMS=${MAVEN_PARAMS}"  -Dmaven.test.skip=true"
      ;;
    --nobuild | --b)
      NO_BUILD=true
      ;;
    --U)
      MAVEN_PARAMS=${MAVEN_PARAMS}"  --U"
      ;;
    --tomcatsonly | --to)
      TOMCATS_ONLY=true
      ;;
    *)
      echo "Unknown parameter $i."
      exit 1
      ;;
  esac
done

# Any subsequent commands which fail will cause the shell script to exit immediately
set -e

doBuild

detect_user_for_upload

selectTomcatToUpload

echo '================================================'
echo '                Uploading completed.'
echo '================================================'

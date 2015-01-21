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

# To use vagrant you should have next lines in /etc/hosts file:
# 192.168.56.99 dev.box.com


getUserHelp() {
  echo "usage: ./vagrant.sh  [--nobuild|--b] [--nogwt|--g] [--notests|--t] [--U] [--o] [-h|--h|-help|--help] [--destroy|--d] [--multi] [--dp] [--centos7|--c7]"
}

getOptionsHelp() {
    echo "Options:"
    echo "         --destroy, --d"
    echo "             Destroy an old vagrant before starting update."
    echo ""
    echo "         --nogwt, --g"
    echo "             Do not build gwt client."
    echo ""
    echo "         --multi"
    echo "             Use multiserver instead of single server environment."
    echo ""
    echo "         --dp"
    echo "             Do not make pull in deployment project."
    echo ""
    echo "         --notests, --t"
    echo "             Do not run tests."
    echo ""
    echo "         --o"
    echo "             Use maven offline option."
    echo ""
    echo "         --U"
    echo "             Add -U to maven command."
    echo ""
    echo "         --nobuild, --b"
    echo "             Do not build cloud-ide project."
    echo ""
    echo "         --centos7, --c7"
    echo "             Provision Centos7"
    echo ""
    echo "         -h, --h, -help, --help"
    echo "             Show user help"
}

deleteFileIfExists() {
  if [ -f $1 ]; then
    echo $1
    rm -rf $1
  fi
}

cloneDeploymentProject() {
  # if deployment project does not exist, clone it from github
  if [ ! -d "../deployment" ]; then
    git clone git@github.com:codenvy/deployment.git ../deployment
  fi
}

pullDeploymentProject() {
  cd ../deployment
  git pull
  cd ../cloud-ide
}

parseParameters() {
  for i in "$@"
  do
    case "$i" in
      --help | -h | -help | --h)
        getUserHelp
        getOptionsHelp
        exit 0
        ;;
      --notests | --t)
        MAVEN_PARAMS=${MAVEN_PARAMS}"  -Dmaven.test.skip=true"
        ;;
      --nobuild | --b)
        SKIP_BUILD=true
        ;;
      --U)
        MAVEN_PARAMS=${MAVEN_PARAMS}"  -U"
        ;;
      --o)
        MAVEN_PARAMS=${MAVEN_PARAMS}"  -o"
        ;;
      --destroy | --d)
        DESTROY_VM=true
        ;;
      --centos7 | --c7)
        OS_CENTOS7=true
        ;;
      --multi)
        MULTI_SERVER=true
        ;;
      --dp)
        MAKE_PULL_IN_DEPLOYMENT=false
        ;;
      --nogwt)
        mvn_version=`mvn -v | grep "Apache Maven" | sed 's/Apache Maven //g' | sed 's/ .*//g'`
        if [[ ${mvn_version} < "3.2.1" ]]; then
          echo "'--nogwt' is supported for maven 3.2.1 or later"
        else
          MAVEN_PARAMS=${MAVEN_PARAMS}" -pl \"!cloud-ide-compiling-war-next-ide-codenvy\""
        fi
        ;;
      *)
        echo "Unknown parameter $i."
        exit 1
        ;;
    esac
  done
}

# Check tomcat logs for errors
checkThatTomcatStartsWithoutErrors() {
  echo -e "\n########  Check tomcat logs for errors | " $1 "\t  ########"
  SERVER_STATE='Starting'
  COUNTER=0
  testfile=/tmp/catalin.out
  while [[ ${SERVER_STATE} != "Started" ]]; do
    deleteFileIfExists ${testfile}

    vagrant ssh $1 -c "sudo cat ../codenvy/codenvy-tomcat/logs/catalina.out" > ${testfile}

    if grep -Fq "[ERROR]" ${testfile}
    then
      # set error occurs to true and do not exit because other server should be checked in multi server setup
      TOMCAT_ERRORS=true
      echo "Exeption occured during tomcat start. Take a look ${testfile}. Exiting"
      cat ${testfile}
      # Set state started to exit from loop
      SERVER_STATE=Started
    else
      if grep -Fq "Server startup" ${testfile}
      then
        echo "Tomcat of the server started"
        SERVER_STATE=Started
      fi

      echo "standalone build state = ${SERVER_STATE}  Attempt ${COUNTER}"
      sleep 5
      let COUNTER=COUNTER+1
    fi 
  done
}

startNotificationsHandling() {
    # send notification if build fails
    onAbort() {
        if (($? != 0)); then
            type notify-send >/dev/null 2>&1 && notify-send -t 100000 --urgency=normal -i "error" "Update failed"
            exit 1
        fi
        exit 0
    }

    # catch stopping over exit command
    trap 'onAbort' 0
}

endNotificationsHandling() {
    # unset catch stopping over exit command
    trap : 0
}

startNotificationsHandling $@

# set mode that halt script execution on error status codes from commands
set -e

# set default parameters of script
MAVEN_PARAMS=""
SKIP_BUILD=false
DESTROY_VM=false
MULTI_SERVER=false
MAKE_PULL_IN_DEPLOYMENT=true
OS_CENTOS7=false
#

# parse parameters specified by user
parseParameters "$@"

cloneDeploymentProject

if [ ${MAKE_PULL_IN_DEPLOYMENT} == true ]; then
  pullDeploymentProject
fi

# Build project
if [ ${SKIP_BUILD} == false ]; then
  mvn clean install ${MAVEN_PARAMS}
fi

# Use Centos7 OS
if [ ${OS_CENTOS7} == true ]; then
  export OS_CENTOS7="true"
fi

if [ ${MULTI_SERVER} == false ]; then
  # Copy all-in-one tomcat zip to puppet folder for subsequent update
  cp -f cloud-ide-packaging-tomcat-codenvy-allinone/target/*.zip ../deployment/puppet/modules/all_in_one/files/cloud-ide-packaging-tomcat-codenvy-allinone.zip
  # Open folder for AIO env
  cd ../deployment/puppet
else
  # Copy tomcats for multi-server environment
  cp -f cloud-ide-packaging-tomcat-api/target/*.zip ../deployment/puppet/modules/multi_server/files/cloud-ide-packaging-tomcat-api.zip
  cp -f cloud-ide-packaging-tomcat-site/target/*.zip ../deployment/puppet/modules/multi_server/files/cloud-ide-packaging-tomcat-site.zip
  cp -f cloud-ide-packaging-tomcat-next-runner/target/*.zip ../deployment/puppet/modules/multi_server/files/cloud-ide-packaging-tomcat-next-runner.zip
  cp -f cloud-ide-packaging-tomcat-next-builder/target/*.zip ../deployment/puppet/modules/multi_server/files/cloud-ide-packaging-tomcat-next-builder.zip
  cp -f cloud-ide-packaging-tomcat-datasource-plugin/target/*.zip ../deployment/puppet/modules/multi_server/files/cloud-ide-packaging-tomcat-datasource-plugin.zip
  cp -f cloud-ide-packaging-tomcat-next-codeassistant/target/*.zip ../deployment/puppet/modules/multi_server/files/cloud-ide-packaging-tomcat-next-codeassistant.zip
  cp -f ../analytics/analytics-tomcat-pkg/target/*.zip ../deployment/puppet/modules/multi_server/files/analytics-tomcat.zip

  # Open folder for multi server env
  cd ../deployment/puppet/vagrant-multi-vm-env
fi

# Destroy existing VM if user set corresponding parameter
if [[ $DESTROY_VM == true && ${MULTI_SERVER} == false ]]; then
  vagrant destroy -f
fi

# Run vagrant. Don't do it if vagrant is runing already
vagrant up --no-provision #to avoid duplicated provisioning at next command
# Update changed configuration
vagrant provision

TOMCAT_ERRORS=false
# Check tomcat logs for errors
if [ ${MULTI_SERVER} == true ]; then
  checkThatTomcatStartsWithoutErrors "analytics"
  checkThatTomcatStartsWithoutErrors "api"
  checkThatTomcatStartsWithoutErrors "runner"
  checkThatTomcatStartsWithoutErrors "builder"
  checkThatTomcatStartsWithoutErrors "datasource"
else
  checkThatTomcatStartsWithoutErrors ""
fi

if [ ${TOMCAT_ERRORS} == true ]; then
  exit 1
fi

type notify-send >/dev/null 2>&1 && notify-send -t 100000 --urgency=low -i "terminal" "Updated successfully"

endNotificationsHandling

echo '================================================'
echo '                Update completed'
echo '================================================'

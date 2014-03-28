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

#!/bin/bash
filename=`ls analytics-tomcat-pkg/target | grep analytics-tomcat-pkg`
#SSH_KEY_NAME=cl-server-prod-20130219
SSH_KEY_NAME=git_nopass.key
SSH_AS_USER_NAME=logreader
AS_IP=syslog.codenvycorp.com
home=/home/logreader/analytics-tomcat

deleteFileIfExists() {
    if [ -f $1 ]; then
        echo $1
        rm -rf $1
    fi
}

    echo "==== Step [1/7] =======================> [Uploading a new Tomcat]"
    scp -i ~/.ssh/${SSH_KEY_NAME} analytics-tomcat-pkg/target/${filename} ${SSH_AS_USER_NAME}@${AS_IP}:${filename}
    echo "==== Step [2/7] =======================> [Stoping Tomcat]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/bin/;if [ -f catalina.sh ]; then ./catalina.sh stop; fi"
    echo "==== Step [3/7] =======================> [Server is stopped]"

    echo "==== Step [4/7] =======================> [Cleaning up]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "rm -rf ${home}"
    echo "==== Step [5/7] =======================> [Unpacking resources]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "unzip ${filename} -d analytics-tomcat"
    echo "==== Step [6/7] =======================> [Starting up on ${AS_IP}]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/bin;./catalina.sh start"

    AS_STATE='Starting'
    testfile=/tmp/catalina.log
    while [ "${AS_STATE}" != "Started" ]; do

    deleteFileIfExists ${testfile}

    scp -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP}:${home}/logs/catalina.out ${testfile}

      if grep -Fq "Server startup in" ${testfile}
        then
         echo "==== Step [7/7] ======================> [Analytics is started]"
         AS_STATE=Started
      fi
         sleep 5
    done
    echo ""
    echo ""
    echo "============================================================================"
    echo "====================== WELLCOME TO ANALYTICS ==============================="
    echo "============================================================================"
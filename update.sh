#!/bin/bash
filename=`ls analytics-tomcat-pkg/target | grep analytics-tomcat`
SSH_KEY_NAME=cl-server-prod-20130219
SSH_AS_USER_NAME=logreader
AS_IP=syslog.codenvycorp.com
home=/home/logreader/analytics-tomcat

deleteFileIfExists() {
    if [ -f $1 ]; then
        echo $1
        rm -rf $1
    fi
}

    echo "==== Step [1/7] =======================> [upload a new Tomcat]"
    scp -i ~/.ssh/${SSH_KEY_NAME} analytics-tomcat-pkg/target/${filename} ${SSH_AS_USER_NAME}@${AS_IP}:${filename}
    echo "==== Step [2/7] =======================> [stop Tomcat]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cp ${home}/wso2carbon.pid ${home}/pid"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/bin/;if [ -f wso2server.sh ]; then ./wso2server.sh stop; fi"

    AS_STATE='Running'
    testfile=/tmp/wso2carbon.state
    while [ "${AS_STATE}" != "Stopped" ]; do
        deleteFileIfExists ${testfile}
        ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "ps x | grep `ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} cat ${home}/pid` > ${home}/wso2carbon.state"
        scp -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP}:${home}/wso2carbon.state ${testfile}
        if [ "`cat ${testfile} | wc -l`" == "2" ]; then
            echo "==== Step [3/7] =======================> [Tomcat of application server has stopped]"
            AS_STATE=Stopped
        fi

        sleep 5
    done

    echo "==== Step [4/7] =======================> [clean up]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "rm -rf ${home}"
    echo "==== Step [5/7] =======================> [unpack new tomcat]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "unzip ${filename} -d wso2bam-analytics"
    echo "==== Step [6/7] =======================> [start new Tomcat on ${AS_IP}]"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/bin;./wso2server.sh start"

    AS_STATE='Starting'
    testfile=/tmp/wso2carbon.log
    while [ "${AS_STATE}" != "Started" ]; do

    deleteFileIfExists ${testfile}

    scp -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP}:${home}/repository/logs/wso2carbon.log ${testfile}

      if grep -Fq "WSO2 Carbon started in" ${testfile}
        then
         echo "==== Step [7/7] ======================> [Tomcat of application server has started]"
         AS_STATE=Started
      fi

         sleep 5
    done
    echo ""
    echo ""
    echo "============================================================================"
    echo "====================== WELLCOME TO ANALYTICS ==============================="
    echo "============================================================================"
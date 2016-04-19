#!/bin/bash
#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2015] Codenvy, S.A.
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

. ./lib.sh

printAndLog "TEST CASE: Install Codenvy 4.x single-node behind the proxy"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

# install Codenvy 4.x behind the proxy
executeSshCommand "echo '$PROXY_IP $PROXY_SERVER' | sudo tee --append /etc/hosts > /dev/null"

installCodenvy ${LATEST_CODENVY4_VERSION} --http-proxy=$HTTP_PROXY --https-proxy=$HTTPS_PROXY
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}
authWithoutRealmAndServerDns "admin" "password"

# create user "cdec.im.test@gmail.com"
doPost "application/json" "{\"name\":\"cdec\",\"email\":\"cdec.im.test@gmail.com\",\"password\":\"pwd123ABC\"}" "http://${HOST_URL}/api/user/create?token=${TOKEN}"
fetchJsonParameter "id"
USER_ID=${OUTPUT}

authWithoutRealmAndServerDns "cdec" "pwd123ABC"

# create workspace "workspace-1"
doPost "application/json" "{\"environments\":[{\"name\":\"workspace-1\",\"machineConfigs\":[{\"links\":[],\"limits\":{\"ram\":1000},\"name\":\"ws-machine\",\"type\":\"docker\",\"source\":{\"location\":\"http://${HOST_URL}/api/recipe/recipe_ubuntu/script\",\"type\":\"recipe\"},\"dev\":true}]}],\"defaultEnv\":\"workspace-1\",\"projects\":[],\"name\":\"workspace-1\",\"attributes\":{},\"temporary\":false}" "http://${HOST_URL}/api/workspace/?token=${TOKEN}"
fetchJsonParameter "id"
WORKSPACE_ID=${OUTPUT}

# run workspace "workspace-1"
doPost "application/json" "{}" "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}/runtime?token=${TOKEN}"

# verify is workspace running
doSleep "6m"  "Wait until workspace starts to avoid 'java.lang.NullPointerException' error on verifying workspace state"
doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*\"status\":\"RUNNING\".*"

printAndLog "RESULT: PASSED"
vagrantDestroy

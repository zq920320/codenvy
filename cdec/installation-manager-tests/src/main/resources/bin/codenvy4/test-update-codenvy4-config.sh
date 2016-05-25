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

[ -f "./lib.sh" ] && . ./lib.sh
[ -f "../lib.sh" ] && . ../lib.sh

printAndLog "TEST CASE: View and update codenvy4 config"

vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installCodenvy ${LATEST_CODENVY4_VERSION}
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}

PROPERTY_TO_TEST=zabbix_admin_email
VALUE_TO_TEST=root@localhost
VALUE_TO_UPDATE=user@localhost

executeIMCommand "im-config"
validateExpectedString ".*admin_ldap_password=*****.*installation_manager_update_server_endpoint=$UPDATE_SERVICE.*version=$LATEST_CODENVY4_VERSION.*$PROPERTY_TO_TEST=$VALUE_TO_TEST.*"

executeIMCommand "im-config $PROPERTY_TO_TEST"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_TEST.*"

executeSshCommand "echo y | codenvy im-config $PROPERTY_TO_TEST $VALUE_TO_UPDATE"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_UPDATE.*"

executeIMCommand "im-config $PROPERTY_TO_TEST"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_UPDATE.*"

printAndLog "RESULT: PASSED"

vagrantDestroy

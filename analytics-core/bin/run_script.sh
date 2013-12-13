#!/bin/sh

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


# analytic jars
for jar in $(find repository/deployment/server/webapps -name 'analytics-core*.jar');  do
   ANALYTICS_JAR=$jar
done

for jar in $(find repository/deployment/server/webapps -name 'mongo-java-driver*.jar');  do
   MONGO_JAR=$jar
done

CLASSPATH=$ANALYTICS_JAR:$MONGO_JAR

export PIG_CLASSPATH=$CLASSPATH
export PIG_HOME=pig

echo "PIG_HOME environment variable is set to $PIG_HOME"
echo "PIG_CLASSPATH environment variable is set to $PIG_CLASSPATH"

pig/bin/pig $*

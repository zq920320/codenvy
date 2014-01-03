/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

IMPORT 'macros.pig';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = filterByEvent(l, 'run-started');
a = FOREACH a1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('runs', 1);
STORE a INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

b1 = filterByEvent(l, 'debug-started');
b = FOREACH b1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('debugs', 1);
STORE b INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

c1 = filterByEvent(l, 'project-built,application-created,project-deployed');
c = FOREACH c1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('builds', 1);
STORE c INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

d1 = filterByEvent(l, 'application-created,project-deployed');
d = FOREACH d1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('deploys', 1);
STORE d INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

e1 = filterByEvent(l, 'factory-created');
e = FOREACH e1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('factories', 1);
STORE e INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

n1 = filterByEvent(l, 'project-created');
n = FOREACH n1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('projects', 1);
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

m1 = filterByEvent(l, 'project-destroyed');
m = FOREACH m1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('projects', -1);
STORE m INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

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
a = FOREACH a1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('runs', 1), TOTUPLE('ide', ide);
STORE a INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

b1 = filterByEvent(l, 'debug-started');
b = FOREACH b1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('debugs', 1), TOTUPLE('ide', ide);
STORE b INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

c1 = filterByEvent(l, 'project-built');
c = FOREACH c1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('builds', 1), TOTUPLE('ide', ide);
STORE c INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

d1 = filterByEvent(l, 'application-created,project-deployed');
d = FOREACH d1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('deploys', 1), TOTUPLE('ide', ide);
STORE d INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

e1 = filterByEvent(l, 'factory-created');
e = FOREACH e1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('factories', 1), TOTUPLE('ide', ide);
STORE e INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

f1 = filterByEvent(l, 'user-added-to-ws');
f = FOREACH f1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('joined_users', 1), TOTUPLE('ide', ide);
STORE f INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

n1 = filterByEvent(l, 'project-created');
n = FOREACH n1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('projects', 1), TOTUPLE('ide', ide);
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

m1 = filterByEvent(l, 'project-destroyed');
m = FOREACH m1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('projects', -1), TOTUPLE('ide', ide);
STORE m INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

k1 = filterByEvent(l, 'user-invite');
k = FOREACH k1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('invites', 1), TOTUPLE('ide', ide);
STORE k INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

p1 = filterByEvent(l, 'user-sso-logged-in');
p = FOREACH p1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('logins', 1), TOTUPLE('ide', ide);
STORE p INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

q2 = calculateTime(l, 'run-started', 'run-finished');
q3 = FOREACH q2 GENERATE dt, ws, user, delta, ide;
q = FOREACH q3 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('run_time', delta), TOTUPLE('ide', ide);
STORE q INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

r2 = calculateTime(l, 'build-started', 'build-finished');
r3 = FOREACH r2 GENERATE dt, ws, user, delta, ide;
r = FOREACH r3 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('build_time', delta), TOTUPLE('ide', ide);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

s1 = filterByEvent(l, 'application-created');
s = FOREACH s1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('paas_deploys', 1), TOTUPLE('ide', ide);
STORE s INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
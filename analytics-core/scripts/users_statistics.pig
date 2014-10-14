/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

c1 = filterByEvent(l, 'build-started');
c = FOREACH c1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('builds', 1);
STORE c INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

d1 = filterByEvent(l, 'application-created');
d = FOREACH d1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('deploys', 1);
STORE d INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

e1 = filterByEvent(l, 'factory-created');
e = FOREACH e1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('factories', 1);
STORE e INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

f1 = filterByEvent(l, 'user-added-to-ws');
f = FOREACH f1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('joined_users', 1);
STORE f INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

n1 = filterByEvent(l, 'project-created');
n = FOREACH n1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('projects', 1);
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

m1 = filterByEvent(l, 'project-destroyed');
m = FOREACH m1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('projects', -1);
STORE m INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

k1 = filterByEvent(l, 'user-invite');
k = FOREACH k1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('invites', 1);
STORE k INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

p1 = filterByEvent(l, 'user-sso-logged-in');
p = FOREACH p1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('logins', 1);
STORE p INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

b1 = filterByEvent(l, 'debug-started');
b = FOREACH b1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('debugs', 1);
STORE b INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

a1 = filterByEvent(l, 'run-started');
a = FOREACH a1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('runs', 1);
STORE a INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

q1 = filterByEvent(l, 'run-finished');
q2 = extractParam(q1, 'USAGE-TIME', time);
q3 = FOREACH q2 GENERATE dt, ws, user, (long)time;
q = FOREACH q3 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('run_time', time);
STORE q INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

r1 = filterByEvent(l, 'build-finished');
r2 = extractParam(r1, 'USAGE-TIME', time);
r3 = FOREACH r2 GENERATE dt, ws, user, (long)time;
r = FOREACH r3 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('build_time', time);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

s1 = filterByEvent(l, 'debug-finished');
s2 = extractParam(s1, 'USAGE-TIME', time);
s3 = FOREACH s2 GENERATE dt, ws, user, (long)time;
s = FOREACH s3 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('debug_time', time);
STORE s INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

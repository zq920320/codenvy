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

f1 = extractParam(l, 'PROJECT', project);
f2 = extractParam(f1, 'TYPE', project_type);
f3 = removeEmptyField(f2, 'project');
f4 = removeEmptyField(f3, 'project_type');
f5 = removeEmptyField(f4, 'user');
f = removeEmptyField(f5, 'ws');

d1 = filterByEvent(f, 'build-started');
d = FOREACH d1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('builds', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE d INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

e1 = filterByEvent(f, 'run-started');
e = FOREACH e1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('runs', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE e INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

g1 = filterByEvent(f, 'debug-started');
g = FOREACH g1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('debugs', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE g INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

h1 = filterByEvent(f, 'application-created');
h = FOREACH h1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('deploys', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE h INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

n1 = filterByEvent(f, 'project-created');
n = FOREACH n1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('project_creates', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

o1 = filterByEvent(f, 'project-destroyed');
o = FOREACH o1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('project_destroys', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE o INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

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

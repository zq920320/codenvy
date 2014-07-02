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

b1 = filterByEvent(f, 'user-code-refactor');
b = FOREACH b1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('code_refactories', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE b INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

c1 = filterByEvent(f, 'user-code-complete');
c = FOREACH c1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('code_completes', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE c INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

d1 = filterByEvent(f, 'project-built');
d = FOREACH d1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('builds', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE d INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

e1 = filterByEvent(f, 'run-started');
e = FOREACH e1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('runs', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE e INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

g1 = filterByEvent(f, 'debug-started');
g = FOREACH g1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('debugs', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE g INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

h1 = filterByEvent(f, 'application-created,project-deployed');
h = FOREACH h1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('deploys', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE h INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

l1 = filterByEvent(f, 'build-interrupted');
l = FOREACH l1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('build_interrupts', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE l INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

m1 = filterByEvent(f, 'artifact-deployed');
m = FOREACH m1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('artifact_deploys', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE m INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

n1 = filterByEvent(f, 'project-created');
n = FOREACH n1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('project_creates', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

o1 = filterByEvent(f, 'project-destroyed');
o = FOREACH o1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('project_destroys', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE o INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

y1 = filterByEvent(f, 'application-created');
y = FOREACH y1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('paas_deploys', 1), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('project', project), TOTUPLE('project_type', LOWER(project_type)), TOTUPLE('project_id', CreateProjectId(user, ws, project));
STORE y INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

s1 = calculateTime(f, 'run-started', 'run-finished');
s2 = JOIN s1 BY (dt,user,ws), f BY (dt,user,ws);
s = FOREACH s2 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(s1::dt)), TOTUPLE('run_time', s1::delta), TOTUPLE('user', s1::user), TOTUPLE('ws', s1::ws), TOTUPLE('project', f::project), TOTUPLE('project_type', LOWER(f::project_type)), TOTUPLE('project_id', CreateProjectId(s1::user, s1::ws, f::project));
STORE s INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

t1 = calculateTime(f, 'build-started', 'build-finished');
t2 = JOIN t1 BY (dt,user,ws), f BY (dt,user,ws);
t = FOREACH t2 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(t1::dt)), TOTUPLE('build_time', t1::delta), TOTUPLE('user', t1::user), TOTUPLE('ws', t1::ws), TOTUPLE('project', f::project), TOTUPLE('project_type', LOWER(f::project_type)), TOTUPLE('project_id', CreateProjectId(t1::user, t1::ws, f::project));
STORE t INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

u1 = calculateTime(f, 'debug-started', 'debug-finished');
u2 = JOIN u1 BY (dt,user,ws), f BY (dt,user,ws);
u = FOREACH u2 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(u1::dt)), TOTUPLE('debug_time', u1::delta), TOTUPLE('user', u1::user), TOTUPLE('ws', u1::ws), TOTUPLE('project', f::project), TOTUPLE('project_type', LOWER(f::project_type)), TOTUPLE('project_id', CreateProjectId(u1::user, u1::ws, f::project));
STORE u INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

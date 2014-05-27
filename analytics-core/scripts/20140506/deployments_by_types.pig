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

l = loadResources('$STORAGE_URL', '$STORAGE_TABLE_USERS_PROFILES', '$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

-- find project-created events
a1 = filterByEvent(l, 'project-created');
a2 = extractParam(a1, 'PROJECT', project);
a3 = extractParam(a2, 'PAAS', paas);
a4 = extractParam(a3, 'TYPE', project_type);
a = FOREACH a4 GENERATE dt, ws, user, project, project_type;

-- find project-deployed events
b1 = filterByEvent(l, 'project-deployed,application-created');
b2 = extractParam(b1, 'PROJECT', project);
b3 = extractParam(b2, 'PAAS', paas);
b4 = extractParam(b3, 'TYPE', project_type);
b = FOREACH b4 GENERATE dt, ws, user, project, project_type, paas, ide;

-- try to guess which project deployment relates to which project creation
-- in a word, find the closest project-deployment event to project-created one
c1 = JOIN a BY (ws, project), b BY (ws, project);
c2 = FILTER c1 BY a::dt < b::dt;
c = FOREACH c2 GENERATE a::dt AS dt, a::ws AS ws, a::user AS user, a::project AS project, a::project_type AS project_type, b::paas AS paas, b::dt AS depDt, b::ide AS ide;

d1 = GROUP c BY (dt, ws, project);
d2 = FOREACH d1 GENERATE FLATTEN(group), FLATTEN(c), MIN(c.depDt) AS closestTime;
d3 = FILTER d2 BY c::depDt == closestTime;
d = FOREACH d3 GENERATE group::dt AS dt, group::ws AS ws, c::user AS user, LOWER(c::paas) AS paas, c::project AS project, c::project_type AS project_type, c::ide AS ide;

r1 = FOREACH d GENERATE dt, ws, user, paas, project, project_type, ide;
r = removeEmptyField(r1, 'paas');

result = FOREACH r GENERATE UUID(),
                            TOTUPLE('date', ToMilliSeconds(dt)),
                            TOTUPLE('ws', ws),
                            TOTUPLE('user', user),
                            TOTUPLE('project_paas', LOWER(paas)),
                            TOTUPLE('project', project),
                            TOTUPLE('project_type', LOWER(project_type)),
                            TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                            TOTUPLE('ide', ide);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

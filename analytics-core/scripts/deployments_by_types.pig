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

-- find project-created events
a1 = filterByEvent(l, 'project-created');
a2 = extractParam(a1, 'PROJECT', project);
a = FOREACH a2 GENERATE dt, ws, user, project;

-- find project-deployed events
b1 = filterByEvent(l, 'project-deployed,application-created');
b2 = extractParam(b1, 'PROJECT', project);
b3 = extractParam(b2, 'PAAS', paas);
b = FOREACH b3 GENERATE dt, ws, user, project, paas, ide;

-- try to guess which project deployment relates to which project creation
-- in a word, find the closest project-deployment event to project-created one
c1 = JOIN a BY (ws, project), b BY (ws, project);
c2 = FILTER c1 BY a::dt < b::dt;
c = FOREACH c2 GENERATE a::dt AS dt, a::ws AS ws, a::user AS user, a::project AS project, b::paas AS paas, b::dt AS depDt, b::ide AS ide;

d1 = GROUP c BY (dt, ws, project);
d2 = FOREACH d1 GENERATE FLATTEN(group), FLATTEN(c), MIN(c.depDt) AS closestTime;
d3 = FILTER d2 BY c::depDt == closestTime;
d = FOREACH d3 GENERATE group::dt AS dt, group::ws AS ws, group::project AS project, c::user AS user, LOWER(c::paas) AS paas, c::ide AS ide;

r1 = FOREACH d GENERATE dt, ws, project, user, paas, ide;
r = removeEmptyField(r1, 'paas');

result = FOREACH r GENERATE UUID(), 
                            TOTUPLE('date', ToMilliSeconds(dt)), 
                            TOTUPLE('ws', ws), 
                            TOTUPLE('project_id', CreateProjectId(user, ws, project)),
                            TOTUPLE('user', user),
                            TOTUPLE(paas, 1L),  
                            TOTUPLE('ide', ide);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

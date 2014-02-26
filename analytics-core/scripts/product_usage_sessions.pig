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

DEFINE MongoStorage com.codenvy.analytics.pig.udf.MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');
DEFINE MongoLoader com.codenvy.analytics.pig.udf.MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray,user_company: chararray');
DEFINE UUID com.codenvy.analytics.pig.udf.UUID;

IMPORT 'macros.pig';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
u = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_PROFILES' USING MongoLoader;

s1 = combineSmallSessions(l, 'session-started', 'session-finished');
s2 = removeEmptyField(s1, 'user');

-- add factory sessions with delta == 0
a1 = createdTemporaryWorkspaces(l);
a = FOREACH a1 GENERATE ws, user;

b1 = filterByEvent(l, 'factory-url-accepted');
b = FOREACH b1 GENERATE dt, ws, ide;

c1 = JOIN b BY ws FULL, a BY ws;
c = FOREACH c1 GENERATE b::dt AS dt, b::ws AS ws, a::user AS user, b::ide AS ide;

d1 = filterByEvent(l, 'session-started');
d = FOREACH d1 GENERATE ws, user;

e1 = JOIN c BY ws LEFT, d BY ws;
e2 = FILTER e1 BY d::ws IS NULL;
e = FOREACH e2 GENERATE c::ws AS ws, c::user AS user, c::dt AS dt, 0 AS delta, '' AS id, c::ide AS ide;

-- combine all sessions together
s = UNION s2, e;

t1 = JOIN s by user LEFT, u BY id;
t2 = FOREACH t1 GENERATE s::dt AS dt, s::ws AS ws, s::user AS user, s::id AS id, s::delta AS delta,
        (u::user_company IS NULL ? '' : u::user_company) AS company, s::ide AS ide;
t3 = FOREACH t2 GENERATE dt, ws, user, id, delta, company, REGEX_EXTRACT(user, '.*@(.*)', 1) AS domain, ide;
t = FOREACH t3 GENERATE dt, ws, user, id, delta, company, (domain IS NULL ? '' : domain) AS domain, ide;

result = FOREACH t GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('ws', ws), TOTUPLE('user', user),
            TOTUPLE('session_id', id), TOTUPLE('ide', ide),
            TOTUPLE('time', delta), TOTUPLE('domain', domain), TOTUPLE('user_company', company);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

---------------------------------------
-- REGISTERED USERS: The total time of the sessions
---------------------------------------
x1 = FILTER t BY INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) < 0;
x = FOREACH x1 GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('time', delta),
        TOTUPLE('sessions', 1), TOTUPLE('ide', ide);

STORE x INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage;

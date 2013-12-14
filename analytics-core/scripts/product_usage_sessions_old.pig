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

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
f = productUsageTimeList(t, '10');

result = FOREACH f GENERATE ToMilliSeconds(dt), TOTUPLE('user', user), TOTUPLE('value', delta);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

r1 = FOREACH f GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, delta;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('value', delta);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE-raw' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

---------------------------------------
-- USERS: The total time of the sessions
---------------------------------------
s1 = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray, time: Long');
s = FOREACH s1 GENERATE id, (time IS NULL ? 0 : time) AS time;

-- calculate total user's time being in product
t1 = GROUP f BY user;
t2 = FOREACH t1 GENERATE group AS id, SUM(f.delta) AS time;
t = FILTER t2 BY INDEXOF(UPPER(id), 'ANONYMOUSUSER_', 0) != 0 AND id != 'default';

--combine and store result
x1 = JOIN t BY id LEFT, s BY id;
x2 = FOREACH x1 GENERATE t::id AS id, (t::time + (s::time IS NULL ? 0 : s::time)) AS time;
x = FOREACH x2 GENERATE id, TOTUPLE('user_email', id), TOTUPLE('time', time);
STORE x INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

---------------------------------------
-- USERS: The number of sessions
---------------------------------------
k1 = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray, sessions: Long');
k = FOREACH k1 GENERATE id, (sessions IS NULL ? 0 : sessions) AS sessions;

-- calculate total user's sessions
m1 = GROUP f BY user;
m2 = FOREACH m1 GENERATE group AS id, COUNT(f) AS sessions;
m = FILTER m2 BY INDEXOF(UPPER(id), 'ANONYMOUSUSER_', 0) != 0 AND id != 'default';

--combine and store result
n1 = JOIN m BY id LEFT, k BY id;
n2 = FOREACH n1 GENERATE k::id AS id, (m::sessions + (k::sessions IS NULL ? 0 : k::sessions)) AS sessions;
n = FOREACH n2 GENERATE id, TOTUPLE('user_email', id), TOTUPLE('sessions', sessions);
STORE n INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

---------------------------------------
-- WORKSPACES: The total time of the sessions
---------------------------------------
a1 = LOAD '$STORAGE_URL.$STORAGE_TABLE_WORKSPACES_STATISTICS' USING MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray, time: Long');
a = FOREACH a1 GENERATE id, (time IS NULL ? 0 : time) AS time;

-- calculate total time
b1 = GROUP f BY ws;
b2 = FOREACH b1 GENERATE group AS id, SUM(f.delta) AS time;
b = FILTER b2 BY INDEXOF(UPPER(id), 'TMP-', 0) != 0 AND id != 'default';

--combine and store result
c1 = JOIN b BY id LEFT, a BY id;
c2 = FOREACH c1 GENERATE b::id AS id, (b::time + (a::time IS NULL ? 0 : a::time)) AS time;
c = FOREACH c2 GENERATE id, TOTUPLE('ws_name', id), TOTUPLE('time', time);
STORE c INTO '$STORAGE_URL.$STORAGE_TABLE_WORKSPACES_STATISTICS' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

---------------------------------------
-- WORKSPACES: The number of sessions
---------------------------------------
d1 = LOAD '$STORAGE_URL.$STORAGE_TABLE_WORKSPACES_STATISTICS' USING MongoLoader('$STORAGE_USER', '$STORAGE_PASSWORD', 'id: chararray, sessions: Long');
d = FOREACH d1 GENERATE id, (sessions IS NULL ? 0 : sessions) AS sessions;

-- calculate total sessions
e1 = GROUP f BY ws;
e2 = FOREACH e1 GENERATE group AS id, COUNT(f) AS sessions;
e = FILTER e2 BY INDEXOF(UPPER(id), 'TMP-', 0) != 0 AND id != 'default';

--combine and store result
g1 = JOIN e BY id LEFT, d BY id;
g2 = FOREACH g1 GENERATE k::id AS id, (e::sessions + (d::sessions IS NULL ? 0 : d::sessions)) AS sessions;
g = FOREACH g2 GENERATE id, TOTUPLE('ws_name', id), TOTUPLE('sessions', sessions);
STORE g INTO '$STORAGE_URL.$STORAGE_TABLE_WORKSPACES_STATISTICS' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');


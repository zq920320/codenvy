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

%DEFAULT inactiveInterval '10';

---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------
DEFINE addLogoutInterval(X, L, inactiveIntervalParam) RETURNS Y {
  z1 = filterByEvent($L, 'user-sso-logged-out');
  z = FOREACH z1 GENERATE dt, user;

  -- finds out if logout event occurred just after session ends
  x1 = JOIN $X BY user LEFT, z BY user;
  x2 = FOREACH x1 GENERATE *, (z::user IS NULL ? 0 -- there is not logout event
                                             : (MilliSecondsBetween(z::dt, $X::dt) > 0 AND MilliSecondsBetween(z::dt, $X::dt) < $X::delta ? 0 -- logout during sessions
                                                                                                                                          : (MilliSecondsBetween(z::dt, $X::dt) > $X::delta AND MilliSecondsBetween(z::dt, $X::dt) <= $X::delta + (long) $inactiveIntervalParam*60*1000 ? MilliSecondsBetween(z::dt, $X::dt) - $X::delta -- logout event after the end of the session
                                                                                                                                                                                                                                                                                        : 0))) -- logout far from the end of the session
                              AS logout_interval;

  x3 = FOREACH x2 GENERATE $X::ws AS ws, $X::user AS user, $X::dt AS dt, $X::delta AS delta, $X::ide AS ide, $X::id AS id,
                    logout_interval AS logout_interval;

  -- if several events were occurred then keep only
  x4 = GROUP x3 BY (dt, id);
  x5 = FOREACH x4 {
        t = LIMIT x3 1;
        GENERATE FLATTEN(t);
    }

  $Y = FOREACH x5 GENERATE t::ws AS ws, t::user AS user, t::dt AS dt, (t::delta + t::logout_interval) AS delta,
                        t::id AS id, t::ide AS ide, t::logout_interval AS logout_interval;
};

---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------
l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
u = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_PROFILES' USING MongoLoaderUsersProfiles;

s1 = combineSmallSessions(l, 'session-started', 'session-finished');
s2 = removeEmptyField(s1, 'user');
s3 = addLogoutInterval(s2, l, '$inactiveInterval');
s4 = FOREACH s3 GENERATE ws, user, dt, delta, id, ide, logout_interval;

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
e = FOREACH e2 GENERATE c::ws AS ws, c::user AS user, c::dt AS dt, 0 AS delta, '' AS id, c::ide AS ide, 0 AS logout_interval;

-- combine all sessions together
s = UNION s4, e;

t1 = JOIN s by user LEFT, u BY id;
t2 = FOREACH t1 GENERATE s::dt AS dt, s::ws AS ws, s::user AS user, s::id AS id, s::delta AS delta,
        (u::user_company IS NULL ? '' : u::user_company) AS company, s::ide AS ide,
        s::logout_interval AS logout_interval;
t3 = FOREACH t2 GENERATE dt, ws, user, id, delta, company, REGEX_EXTRACT(user, '.*@(.*)', 1) AS domain, ide, logout_interval;
t = FOREACH t3 GENERATE dt, ws, user, id, delta, company, (domain IS NULL ? '' : domain) AS domain, ide, logout_interval;


result = FOREACH t GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('ws', ws), TOTUPLE('user', user),
            TOTUPLE('session_id', id), TOTUPLE('ide', ide), TOTUPLE('logout_interval', logout_interval),
            TOTUPLE('end_time', ToMilliSeconds(dt) + delta), TOTUPLE('time', delta), TOTUPLE('domain', domain),
            TOTUPLE('user_company', company);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

-- store sessions fro users' statistics
x = FOREACH t GENERATE UUID(),
                       TOTUPLE('date', ToMilliSeconds(dt)),
                       TOTUPLE('user', user),
                       TOTUPLE('ws', ws),
                       TOTUPLE('time', delta),
                       TOTUPLE('sessions', 1),
                       TOTUPLE('ide', ide);
STORE x INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage;

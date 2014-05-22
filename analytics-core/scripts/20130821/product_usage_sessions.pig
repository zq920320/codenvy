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

---------------------------------------------------------------------------------------------
-- Groups events occurred for specific user in specific workspace.
-- @return {ws: bytearray,user: bytearray,dt: datetime,
--          intervals: {(ws: bytearray,user: bytearray,dt: datetime,delta: long)}}
---------------------------------------------------------------------------------------------
DEFINE groupEvents(X) RETURNS Y {
  x0 = FILTER $X BY user != 'default' AND ws != 'default';
  x1 = FOREACH x0 GENERATE ws, user, dt;
  x2 = FOREACH x0 GENERATE ws, user, dt;

  x3 = JOIN x1 BY (ws, user), x2 BY (ws, user);

  ---------------------------------------------------------------------------------------------
  -- Calculates the seconds beetwen every events (delta: long)
  ---------------------------------------------------------------------------------------------
  x4 = FOREACH x3 GENERATE x1::ws AS ws, x1::user AS user, x1::dt AS dt, MilliSecondsBetween(x2::dt, x1::dt) AS delta;

  ---------------------------------------------------------------------------------------------
  -- For every event forms the list of its 'delta'
  ---------------------------------------------------------------------------------------------
  x5 = GROUP x4 BY (ws, user, dt);
  $Y = FOREACH x5 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, $1 AS intervals;
};

---------------------------------------------------------------------------------------------
-- The list of all users sessions in all workspaces
-- @return {ws: bytearray,user: bytearray,dt: datetime,delta: long}
---------------------------------------------------------------------------------------------
DEFINE productUsageTimeList(X, inactiveIntervalParam) RETURNS Y {
  tR = groupEvents($X);

  ---------------------------------------------------------------------------------------------
  -- For every event keeps only the closest surrounded 'delta'
  ---------------------------------------------------------------------------------------------
  k1 = FOREACH tR {
      negativeDelta = FILTER intervals BY delta < 0;
      positiveDelta = FILTER intervals BY delta > 0;
      GENERATE ws, user, dt, MAX(negativeDelta.delta) AS before, MIN(positiveDelta.delta) AS after;
  }

  ---------------------------------------------------------------------------------------------
  -- Marks the start and the end of every session
  ---------------------------------------------------------------------------------------------
  k2 = FOREACH k1 GENERATE ws, user, dt, (before IS NULL ? -999999999 : before) AS before, (after IS NULL ? 999999999 : after) AS after;
  k3 = FOREACH k2 GENERATE ws, user, dt, (before < -(long)$inactiveIntervalParam*60*1000 ? (after <= (long)$inactiveIntervalParam*60*1000 ? 'start'
                                                                : 'single')
                                         : (after <= (long)$inactiveIntervalParam*60*1000 ? 'none'
                                                            : 'end')) AS flag;
  kR = FILTER k3 BY flag == 'start' OR flag == 'end';

  k4 = FILTER k3 BY flag == 'single';
  kS = FOREACH k4 GENERATE ws, user, dt, 0 AS delta;

  ---------------------------------------------------------------------------------------------
  -- For every the start session event finds the corresponding the end session event
  ---------------------------------------------------------------------------------------------
  l1 = FOREACH kR GENERATE *;
  l2 = FOREACH kR GENERATE *;

  ---------------------------------------------------------------------------------------------
  -- Prepares pairs of all potential 'start-end' session events
  ---------------------------------------------------------------------------------------------
  l3 = JOIN l1 BY (ws, user), l2 BY (ws, user);
  l4 = FILTER l3 BY l1::flag == 'start' AND l2::flag == 'end';

  ---------------------------------------------------------------------------------------------
  -- The correct pair is with minimum positive time interval between them
  ---------------------------------------------------------------------------------------------
  l5 = FOREACH l4 GENERATE l1::ws AS ws, l1::user AS user, l1::dt AS dt, MilliSecondsBetween(l2::dt, l1::dt) AS delta;
  l6 = FILTER l5 BY delta > 0;
  l7 = GROUP l6 BY (ws, user, dt);
  l = FOREACH l7 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, MIN(l6.delta) AS delta;

  $Y = UNION kS, l;
};


l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
u = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_PROFILES' USING MongoLoaderUsersProfiles;

s1 = productUsageTimeList(l, '10');
s = FOREACH s1 GENERATE *, UUID() AS id;

t1 = JOIN s by user LEFT, u BY id;
t2 = FOREACH t1 GENERATE s::dt AS dt, s::ws AS ws, s::user AS user, s::id AS id, s::delta AS delta,
        (u::user_company IS NULL ? '' : u::user_company) AS company;
t3 = FOREACH t2 GENERATE dt, ws, user, id, delta, company, REGEX_EXTRACT(user, '.*@(.*)', 1) AS domain;
t = FOREACH t3 GENERATE dt, ws, user, id, delta, company, (domain IS NULL ? '' : domain) AS domain;

result = FOREACH t GENERATE UUID(),
                            TOTUPLE('date', ToMilliSeconds(dt)),
                            TOTUPLE('ws', ws), TOTUPLE('user', user),
                            TOTUPLE('session_id', id),
                            TOTUPLE('ide', 2),
                            TOTUPLE('end_time',
                            ToMilliSeconds(dt) + delta),
                            TOTUPLE('time', delta),
                            TOTUPLE('domain', domain),
                            TOTUPLE('user_company', company);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

---------------------------------------
-- REGISTERED USERS: The total time of the sessions
---------------------------------------
x = FOREACH t GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('user', user), TOTUPLE('ws', ws), TOTUPLE('time', delta),
    TOTUPLE('sessions', 1), TOTUPLE('ide', 2);
STORE x INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage;

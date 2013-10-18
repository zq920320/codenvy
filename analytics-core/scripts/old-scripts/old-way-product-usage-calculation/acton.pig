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
DEFINE productUsageTimeList(X, inactiveInterval) RETURNS Y {
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
  k3 = FOREACH k2 GENERATE ws, user, dt, (before < -(long)$inactiveInterval*60*1000 ? (after <= (long)$inactiveInterval*60*1000 ? 'start'
										          			    : 'single')
									     : (after <= (long)$inactiveInterval*60*1000 ? 'none'
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
  l = FOREACH l7 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, MIN(l6.delta)/1000 AS delta;

  $Y = UNION kS, l;
};


f = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
g = GROUP f BY user;

--------------------------------------------------------------------
-- Calculates projects created, built and deployed numbers
--------------------------------------------------------------------
a = FOREACH g {
    pCreated = FILTER f BY INDEXOF('project-created', event, 0) >= 0;
    pBuilt = FILTER f BY INDEXOF('project-built,application-created,project-deployed', event, 0) >= 0;
    pDeployed = FILTER f BY INDEXOF('application-created,project-deployed', event, 0) >= 0;

    GENERATE group AS user, COUNT(pCreated) AS pCreatedNum, COUNT(pBuilt) AS pBuiltNum, COUNT(pDeployed) AS pDeployedNum;
}

--------------------------------------------------------------------
-- Calculates time usage numbers
--------------------------------------------------------------------
--b1 = joinEventsWithSameId(f, 'session-started', 'session-finished');
b1 = productUsageTimeList(f, '10');
b2 = GROUP b1 BY user;
b = FOREACH b2 GENERATE group AS user, SUM(b1.delta) AS delta;

--------------------------------------------------------------------
-- Unions two results
--------------------------------------------------------------------
c1 = JOIN a BY user LEFT, b BY user;
c = FOREACH c1 GENERATE a::user AS user, a::pCreatedNum AS pCreatedNum, a::pBuiltNum AS pBuiltNum, a::pDeployedNum AS pDeployedNum, ((b::user IS NULL ? 0 : b::delta) / 60) AS time;

r1 = GROUP c BY user;
r2 = FOREACH r1 GENERATE group, FLATTEN(c);
result = FOREACH r2 GENERATE group, TOBAG(pCreatedNum, pBuiltNum, pDeployedNum, time);





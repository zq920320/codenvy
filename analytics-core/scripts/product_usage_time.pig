-----------------------------------------------------------------------------
-- Calculation total working time for all users in workspace.
-- If the user becomes inactive for 'inactiveInterval' amount of time, 
-- the 'coding session' is considered finished and a new coding session 
-- is started at the next interaction.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT inactiveInterval '10';  -- in minutes

---------------------------------------------------------------------------------------------
-- Groups an event with every potential event belonged to a one common user session.
-- @return {ws: bytearray,user: bytearray,dt: datetime,
--          intervals: {(ws: bytearray,user: bytearray,dt: datetime,delta: long)}}
---------------------------------------------------------------------------------------------
DEFINE groupEvents(X) RETURNS Y {
  x1 = FOREACH $X GENERATE ws, user, dt;
  x2 = FOREACH $X GENERATE ws, user, dt;

  x3 = JOIN x1 BY (ws, user), x2 BY (ws, user);

  ---------------------------------------------------------------------------------------------
  -- Calculates the seconds beetwen every events (delta: long)
  ---------------------------------------------------------------------------------------------
  x4 = FOREACH x3 GENERATE x1::ws AS ws, x1::user AS user, x1::dt AS dt, SecondsBetween(x2::dt, x1::dt) AS delta;

  ---------------------------------------------------------------------------------------------
  -- For every event forms the list of its 'delta'
  ---------------------------------------------------------------------------------------------
  x5 = GROUP x4 BY (ws, user, dt);
  $Y = FOREACH x5 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, $1 AS intervals;
};




f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

t1 = extractUser(fR);
t2 = extractWs(t1);
tR = groupEvents(t2);

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
k2 = FOREACH k1 GENERATE ws, user, dt, (before IS NULL ? -999999 : before) AS before, (after IS NULL ? 999999 : after) AS after;
k3 = FOREACH k2 GENERATE ws, user, dt, (before < -(long)$inactiveInterval*60 ? (after <= (long)$inactiveInterval*60 ? 'start'
										          			    : 'none')
									     : (after <= (long)$inactiveInterval*60 ? 'none'
														    : 'end')) AS flag;
kR = FILTER k3 BY flag != 'none';

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
-- The correc pair is with minimum positive time interval between them
---------------------------------------------------------------------------------------------
l5 = FOREACH l4 GENERATE l1::ws AS ws, l1::user AS user, l1::dt AS dt, SecondsBetween(l2::dt, l1::dt) AS delta;
l6 = FILTER l5 BY delta > 0;
l7 = GROUP l6 BY (ws, user, dt);

result = FOREACH l7 GENERATE TOTUPLE(TOTUPLE(group.ws), TOTUPLE(group.user), TOTUPLE(group.dt), TOTUPLE(MIN(l6.delta)));

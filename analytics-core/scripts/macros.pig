---------------------------------------------------------------------------
-- Loads resources.
-- @return {ip : bytearray, dt : datetime,  event : bytearray, message : chararray} 
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
---------------------------------------------------------------------------
DEFINE loadResources(resourceParam) RETURNS Y {
  l1 = LOAD '$resourceParam' using PigStorage() as (message : chararray);
  l2 = FOREACH l1 GENERATE REGEX_EXTRACT_ALL($0, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}).*EVENT\\#([^\\#]*)\\#.*') 
                          AS pattern, message;
  l3 = FILTER l2 BY pattern.$2 != '';
  l4 = FOREACH l3 GENERATE pattern.$0 AS ip, ToDate(pattern.$1, 'yyyy-MM-dd HH:mm:ss,SSS') AS dt, pattern.$2 AS event, message;
  $Y = DISTINCT l4;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param fromDateParam - date in format 'YYYYMMDD'
-- @param toDateParam  - date in format 'YYYYMMDD'
---------------------------------------------------------------------------
DEFINE filterByDate(X, fromDateParam, toDateParam) RETURNS Y {
  $Y = FILTER $X BY MilliSecondsBetween(ToDate('$fromDateParam', 'yyyyMMdd'), dt) <= 0 AND
                    MilliSecondsBetween(AddDuration(ToDate('$toDateParam', 'yyyyMMdd'), 'P1D'), dt) > 0;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param fromDateParam - date in format 'YYYYMMDD'
-- @param toDateParam  - date in format 'YYYYMMDD'
---------------------------------------------------------------------------
DEFINE filterByDateInterval(X, toDateParam, interval) RETURNS Y {
  $Y = FILTER $X BY MilliSecondsBetween(AddDuration(SubtractDuration(ToDate('$toDateParam', 'yyyyMMdd'), '$interval'), 'P1D'), dt) <= 0 AND
                    MilliSecondsBetween(AddDuration(ToDate('$toDateParam', 'yyyyMMdd'), 'P1D'), dt) > 0;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence. Keeps only in $lastMinutesParam.
-- @param lastMinutesParam - time interval in minutes
-- @return {..., curentDt : datetime}
---------------------------------------------------------------------------
DEFINE filterByLastMinutes(X, lastMinutesParam) RETURNS Y {
  x1 = FOREACH $X GENERATE *, CurrentTime() AS currentDt;
  $Y = FILTER x1 BY MinutesBetween(currentDt, dt) < (long) $lastMinutesParam;
};

---------------------------------------------------------------------------
-- Filters events by names. Keeps only events from passed list.
-- @param eventNamesParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE filterByEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF('$eventNamesParam', event, 0) >= 0;
};

---------------------------------------------------------------------------
-- Filters events by names. Keeps only events out of passed list.
-- @param eventsNameParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE removeEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY INDEXOF('$eventNamesParam', event, 0) < 0;
};

---------------------------------------------------------------------------
-- Extract workspace name out of message and adds as field to tuple.
-- @return  {..., ws : bytearray}
---------------------------------------------------------------------------
DEFINE extractWs(X) RETURNS Y {
  x1 = extractParam($X, 'WS', 'ws');
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws;
  x3 = UNION x1, x2;
  x4 = DISTINCT x3;
  $Y = FILTER x4 BY ws != '' AND ws != 'default';
};

---------------------------------------------------------------------------
-- Extract sessin id out of message and adds as field to tuple.
-- @return  {..., session : bytearray}
---------------------------------------------------------------------------
DEFINE extractSession(X) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[.*\\]\\[(.*)\\] - .*')) AS session;
  $Y = FILTER x1 BY session != '';
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUser(X) RETURNS Y {
  x1 = smartExtractParam($X, 'USER', 'user');
  x2 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
  x3 = UNION x1, x2;
  x4 = DISTINCT x3;
  $Y = FILTER x4 BY user != '';
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUserFromAliases(X) RETURNS Y {
  x1 = FOREACH $X GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#[\\[]?([^\\#^\\[^\\]]*)[\\]]?\\#.*')) AS user;
  $Y = FOREACH x1 GENERATE FLATTEN(TOKENIZE(user, ',')) AS user;
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  x1 = smartExtractParam($X, '$paramNameParam', '$paramFieldNameParam');
  $Y = FILTER x1 BY $paramFieldNameParam != '';
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE smartExtractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  $Y = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*$paramNameParam\\#([^\\#]*)\\#.*')) AS $paramFieldNameParam;
};

---------------------------------------------------------------------------
-- Calculates the difference between two relation.
-- The relations should be preprocessed using 'prepareSet'.
-- @return {{(bytearray)}}
---------------------------------------------------------------------------
DEFINE differSets(A, B) RETURNS Y {
  w1 = JOIN $A BY $0 LEFT, $B BY $0;
  w2 = FILTER w1 BY $1 IS NULL;
  $Y = FOREACH w2 GENERATE $0;
};

---------------------------------------------------------------------------
-- Calculates the intersection between two relation.
-- The relations should be preprocessed using 'prepareSet'.
-- @return {{(bytearray)}}
---------------------------------------------------------------------------
DEFINE intersectSets(A, B) RETURNS Y {
  w1 = JOIN $A BY $0, $B BY $0;
  w2 = FOREACH w1 GENERATE $0;
  w3 = GROUP w2 ALL;
  $Y = FOREACH w3 GENERATE $1 AS list;
};

---------------------------------------------------------------------------
-- Generates a relation with a single field whose values are unique.
-- @param fieldNameParam - the field name to process
-- @return {$fieldNameParam : chararray}
---------------------------------------------------------------------------
DEFINE prepareSet(X, fieldNameParam) RETURNS Y {
  w1 = FOREACH $X GENERATE $fieldNameParam;
  $Y = DISTINCT w1;
};

---------------------------------------------------------------------------
-- Finds last updates user profile
-- @return {user : chararray, firstName : chararray, lastName : chararray, company : chararry}
---------------------------------------------------------------------------
DEFINE lastUserProfileUpdate(X) RETURNS Y {
  -------------------------------------
  -- Finds the most last update
  -------------------------------------
  y1 = GROUP $X BY user;
  y2 = FOREACH y1 GENERATE *, MAX($X.delta) AS maxDelta;
  y3 = FOREACH y2 GENERATE group AS user, maxDelta, FLATTEN($X);
  y4 = FILTER y3 BY delta == maxDelta;
  $Y = FOREACH y4 GENERATE user, $X::firstName AS firstName, $X::lastName AS lastName, $X::company AS company;
};

---------------------------------------------------------------------------------------------
--                             USAGE TIME MACROSES                                         --
---------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------
-- Groups events occurred for specific user in specific workspace.
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

  $Y = FOREACH l7 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, MIN(l6.delta) AS delta;
};

---------------------------------------------------------------------------------------------
-- Counts number of users sessions and its length
-- @param X - {..., user : bytearray, delta : long}
-- @return {user: bytearray,count: long, time: long}
---------------------------------------------------------------------------------------------
DEFINE usersByTimeSpent(X) RETURNS Y {
  w1 = GROUP $X BY user;
  $Y = FOREACH w1 GENERATE FLATTEN(group) AS user, COUNT($X.delta) AS count, SUM($X.delta) AS time;
};

---------------------------------------------------------------------------------------------
-- Counts number of users sessions and its length
-- @param X - {..., user : bytearray, count : long, delta : long}
-- @return {user: bytearray,count: long, time: long}
---------------------------------------------------------------------------------------------
DEFINE domainsByTimeSpent(X) RETURNS Y {
  w1 = FOREACH $X GENERATE REGEX_EXTRACT(user, '.*@(.*)', 1) AS user, count, delta;
  w2 = GROUP w1 BY user;
  $Y = FOREACH w2 GENERATE FLATTEN(group) AS user, SUM(w1.count) AS count, SUM(w1.delta) AS time;
};


---------------------------------------------------------------------------------------------
-- Counts number of users sessions and its length
-- @param X - {..., user : bytearray, count : long, delta : long}
-- @return {user: bytearray,count: long, time: long}
---------------------------------------------------------------------------------------------
DEFINE companiesByTimeSpent(X, resultDirParam) RETURNS Y {
  w1 = LOAD '$resultDirParam/PROFILES' USING PigStorage() AS (user: chararray, firstName: chararray, lastName: chararray, company: chararray);
  w2 = JOIN $X BY user LEFT, w1 BY user;
  w3 = FILTER w2 BY w1::company IS NOT NULL;
  w4 = FOREACH w3 GENERATE w1::company AS user, $X::count AS count, $X::delta AS delta;

  w5 = GROUP w4 BY user;
  $Y = FOREACH w5 GENERATE FLATTEN(group) AS user, SUM(w4.count) AS count, SUM(w4.delta) AS time;
};


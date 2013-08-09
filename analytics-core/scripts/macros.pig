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
-- Removes events where user and was are both 'default'
---------------------------------------------------------------------------
DEFINE skipDefaults(X) RETURNS Y {
  $Y = FILTER $X BY user != 'default' AND ws != 'default';
};

---------------------------------------------------------------------------
-- Returns the unique sequence for every field
-- @return {fieldName1 : chararray, {(fieldName2 : chararray)}}
---------------------------------------------------------------------------
DEFINE setByField(X, fieldName1, fieldName2) RETURNS Y {
	x1 = GROUP $X BY $fieldName1;
	$Y = FOREACH x1 {
		t1 = FOREACH $X GENERATE $fieldName2;
		t = DISTINCT t1;
		GENERATE group, t;
	}
};

---------------------------------------------------------------------------
-- Return the number of tuples in the relation
-- @return {countAll : long}
---------------------------------------------------------------------------
DEFINE countAll(X) RETURNS Y {
	x1 = GROUP $X ALL;
	$Y = FOREACH x1 GENERATE COUNT($X.$0) AS countAll;
};

---------------------------------------------------------------------------
-- Return the number of tuples in the relation
-- @return {fieldNameParam : chararray, countAll : long}
---------------------------------------------------------------------------
DEFINE countByField(X, fieldNameParam) RETURNS Y {
	x1 = GROUP $X BY $fieldNameParam;
	$Y = FOREACH x1 GENERATE group AS $fieldNameParam, COUNT($X.$0) AS countAll;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param toDateParam  - date in format 'YYYYMMDD'
-- @interval
---------------------------------------------------------------------------
DEFINE filterByDateInterval(X, toDateParam, interval) RETURNS Y {
  $Y = FILTER $X BY MilliSecondsBetween(AddDuration(SubtractDuration(ToDate('$toDateParam', 'yyyyMMdd'), '$interval'), 'P1D'), dt) <= 0 AND
                    MilliSecondsBetween(AddDuration(ToDate('$toDateParam', 'yyyyMMdd'), 'P1D'), dt) > 0;
};

---------------------------------------------------------------------------
-- Filters events by date of occurrence.
-- @param toDateParam  - date in format 'YYYYMMDD'
-- @interval
---------------------------------------------------------------------------
DEFINE filterByDateIntervalAfter(X, toDateParam, interval) RETURNS Y {
  $Y = FILTER $X BY MilliSecondsBetween(AddDuration(SubtractDuration(ToDate('$toDateParam', 'yyyyMMdd'), '$interval'), 'P1D'), dt) > 0;
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
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws1, FLATTEN(REGEX_EXTRACT_ALL(message, '.*WS\\#([^\\#]*)\\#.*')) AS ws2;
  $Y = FOREACH x1 GENERATE *, (ws1 IS NOT NULL AND ws1 != '' ? ws1 : (ws2 IS NOT NULL AND ws2 != '' ? ws2 : 'default')) AS ws;
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
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*USER\\#([^\\#]*)\\#.*')) AS user1,
			      FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user2, 
			      FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#[\\[]?([^\\#^\\[^\\]]*)[\\]]?\\#.*')) AS user3;
  x2 = FOREACH x1 GENERATE *, (user1 IS NOT NULL AND user1 != '' ? user1 : (user2 IS NOT NULL AND user2 != '' ? user2 : (user3 IS NOT NULL AND user3 != '' ? user3 : 'default'))) AS newUser;
  $Y = FOREACH x2 GENERATE *, FLATTEN(TOKENIZE(newUser, ',')) AS user;
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
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
  $Y = FOREACH y4 GENERATE user, $X::firstName AS firstName, $X::lastName AS lastName, $X::company AS company, $X::phone AS phone, $X::job AS job;
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
  x0 = skipDefaults($X);
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
  kS = FOREACH k4 GENERATE ws, user, dt, ((long) $inactiveInterval*60) AS delta;

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
  l5 = FOREACH l4 GENERATE l1::ws AS ws, l1::user AS user, l1::dt AS dt, MilliSecondsBetween(l2::dt, l1::dt) AS delta;
  l6 = FILTER l5 BY delta > 0;
  l7 = GROUP l6 BY (ws, user, dt);
  l = FOREACH l7 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, MIN(l6.delta)/1000 AS delta;

  $Y = UNION kS, l;
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


---------------------------------------------------------------------------------------------
--                             USERS SEGMENT ANALYSIS                                      --
---------------------------------------------------------------------------------------------

--------------------------------------------------------------------
-- Caculates the group of records which satisfy given condition and
-- timeframe
--------------------------------------------------------------------
DEFINE calculateConditionAndDate(X, condition, toDateParam, interval) RETURNS Y {
    w1 = filterByDateIntervalAfter($X, '$toDateParam', '$interval');
    w2 = usersByTimeSpent(w1);
    w3 = FILTER w2 BY $condition;
    $Y = GROUP w3 ALL;
};

--------------------------------------------------------------------
-- Replaces NULL with 0 
--------------------------------------------------------------------
DEFINE fixNumbers(X) RETURNS Y {
    $Y = FOREACH $X GENERATE ($0 IS NULL ? 0 : $0), ($1 IS NULL ? 0 : $1), ($2 IS NULL ? 0 : $2), ($3 IS NULL ? 0 : $3), ($4 IS NULL ? 0 : $4), ($5 IS NULL ? 0 : $5);
};

--------------------------------------------------------------------
-- Caculates the group of records which satisfy given condition
--------------------------------------------------------------------
DEFINE calculateCondition(X, conditionParam, toDateParam, s) RETURNS Y {
    z1 = calculateConditionAndDate($X, '$conditionParam', '$toDateParam', 'P1D');
    z7 = calculateConditionAndDate($X, '$conditionParam', '$toDateParam', 'P7D');
    z30 = calculateConditionAndDate($X, '$conditionParam', '$toDateParam', 'P30D');
    z60 = calculateConditionAndDate($X, '$conditionParam', '$toDateParam', 'P60D');
    z90 = calculateConditionAndDate($X, '$conditionParam', '$toDateParam', 'P90D');
    z365 = calculateConditionAndDate($X, '$conditionParam', '$toDateParam', 'P365D');
    z = FOREACH s GENERATE COUNT(z1.$1), COUNT(z7.$1), COUNT(z30.$1), COUNT(z60.$1), COUNT(z90.$1), COUNT(z365.$1);
    $Y = fixNumbers(z);
};

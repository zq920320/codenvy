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
-- @return {ip : bytearray, dt : datetime,  event : bytearray, message : chararray, user : bytearray, ws : bytearray} 
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
---------------------------------------------------------------------------
DEFINE loadResources(resourceParam, from, to, userType, wsType) RETURNS Y {
  l1 = LOAD '$resourceParam' using PigStorage() as (message : chararray);
  l2 = FILTER l1 BY INDEXOF(message, 'EVENT#', 0) > 0;

  l3 = extractUser(l2, '$userType');
  l4 = extractWs(l3, '$wsType');
  l5 = FOREACH l4 GENERATE user,
                           ws,
                           (INDEXOF(message, '[ide3]', 0) >= 0 ? 3 : 2) AS ide,
                           message,
                           REGEX_EXTRACT_ALL(message, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}).*\\s-(\\s.*)') AS pattern;

  l6 = FOREACH l5 GENERATE user,
                           ws,
                           ide,
                           ToDate(pattern.$1, 'yyyy-MM-dd HH:mm:ss,SSS') AS dt,
                           pattern.$2 AS message;

  l7 = filterByDate(l6, '$from', '$to');
  l8 = extractParam(l7, 'EVENT', 'event');
  l9 = removeEmptyField(l8, 'event');

  l10 = DISTINCT l9;

  $Y = FOREACH l10 GENERATE dt, event, message, user, ws, ide;
};
---------------------------------------------------------------------------
-- Removes tuples with empty fields
---------------------------------------------------------------------------
DEFINE removeEmptyField(X, fieldParam) RETURNS Y {
  $Y = FILTER $X BY $fieldParam != '' AND $fieldParam != 'default' AND $fieldParam != 'null' AND $fieldParam IS NOT NULL;
};

---------------------------------------------------------------------------
-- Removes tuples without empty fields
---------------------------------------------------------------------------
DEFINE removeNotEmptyField(X, fieldParam) RETURNS Y {
  $Y = FILTER $X BY $fieldParam == '' OR $fieldParam == 'default' OR $fieldParam == 'null' OR $fieldParam IS NULL;
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
-- Filters events by names. Keeps only events from passed list.
-- @param eventNamesParam - comma separated list of event names
---------------------------------------------------------------------------
DEFINE filterByEvent(X, eventNamesParam) RETURNS Y {
  $Y = FILTER $X BY '$eventNamesParam' == '*' OR IsEventInSet(event, '$eventNamesParam');

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
DEFINE extractWs(X, wsType) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[.*\\]\\[(.*)\\]\\[.*\\] - .*')) AS ws2, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\sWS#([^\\s#][^#]*|)#.*')) AS ws1;
  x2 = FOREACH x1 GENERATE *, (ws1 IS NOT NULL AND ws1 != '' ? ws1 : (ws2 IS NOT NULL AND ws2 != '' ? ws2 : 'default')) AS ws3;
  x3 = FILTER x2 BY '$wsType' == 'ANY' OR  ws3 == 'default' OR
            ('$wsType' == 'TEMPORARY' AND INDEXOF(LOWER(ws3), 'tmp-', 0) == 0) OR
            ('$wsType' == 'PERSISTENT' AND INDEXOF(LOWER(ws3), 'tmp-', 0) < 0);
  $Y = FOREACH x3 GENERATE *, (INDEXOF(LOWER(ws3), 'tmp-', 0) == 0 ? LOWER(ws3) : ws3) AS ws;
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUser(X, userType) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\sUSER#([^\\s#][^#]*|)#.*')) AS user1,
                  FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#[\\[]?([^\\#^\\[^\\]]*)[\\]]?\\#.*')) AS user2,
                  FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user3;
  x2 = FOREACH x1 GENERATE *, (user1 IS NOT NULL AND user1 != '' ? user1 : (user2 IS NOT NULL AND user2 != '' ? user2 : (user3 IS NOT NULL AND user3 != '' ? user3 : 'default'))) AS probUser;
  x3 = FOREACH x2 GENERATE *, FLATTEN(TOKENIZE(probUser, ',')) AS user4;
  x4 = FILTER x3 BY '$userType' == 'ANY' OR user4 == 'default' OR
            ('$userType' == 'ANONYMOUS' AND INDEXOF(LOWER(user4), 'anonymoususer_', 0) == 0) OR
            ('$userType' == 'REGISTERED' AND INDEXOF(LOWER(user4), 'anonymoususer_', 0) < 0);
  $Y = FOREACH x4 GENERATE *, (INDEXOF(LOWER(user4), 'anonymoususer_', 0) == 0 ? LOWER(user4) : user4) AS user;
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  $Y = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\s$paramNameParam#([^\\s#][^#]*|)#.*')) AS $paramFieldNameParam;
};


---------------------------------------------------------------------------
-- Extract orgId and affiliateId either from parameter or from factory url.
-- Removes ending '}' character (known bug)
-- @return  {..., orgId : bytearray, affiliateId : bytearray}
---------------------------------------------------------------------------
DEFINE extractOrgAndAffiliateId(X) RETURNS Y {
  e1 = extractParam($X, 'ORG-ID', 'orgIdTest1');
  e2 = extractParam(e1, 'AFFILIATE-ID', 'affiliateIdTest1');
  e3 = extractUrlParam(e2, 'FACTORY-URL', 'factoryTest');
  e4 = FOREACH e3 GENERATE *, GetQueryValue(factoryTest, 'orgid') AS orgIdTest2, GetQueryValue(factoryTest, 'affiliateid') AS affiliateIdTest2;
  e5 = FOREACH e4 GENERATE *, (orgIdTest1 IS NULL OR orgIdTest1 == '' ? orgIdTest2 : orgIdTest1) AS orgIdTest3,
                        (affiliateIdTest1 IS NULL OR affiliateIdTest1 == '' ? affiliateIdTest2 : affiliateIdTest1) AS affiliateIdTest3;
  $Y = FOREACH e5 GENERATE *, (ENDSWITH(orgIdTest3, '}') ? SUBSTRING(orgIdTest3, 0, LAST_INDEX_OF(orgIdTest3, '}')) : orgIdTest3) AS orgId,
        (ENDSWITH(affiliateIdTest3, '}') ? SUBSTRING(affiliateIdTest3, 0, LAST_INDEX_OF(affiliateIdTest3, '}')) : affiliateIdTest3) AS affiliateId;
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractUrlParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  $Y = FOREACH $X GENERATE *, ('$paramNameParam' == 'FACTORY-URL' ? REPLACE(CutQueryParam(
                                                                                CutQueryParam(
                                                                                    URLDecode(
                                                                                        URLDecode(
                                                                                            REGEX_EXTRACT(message, '.*\\s$paramNameParam#([^\\s#][^#]*|)#.*', 1))),
                                                                                             'ptype'), 'openfile'), '\\/factory\\/\\?', '\\/factory\\?')
                                                                  :  URLDecode(
                                                                        URLDecode(
                                                                            REGEX_EXTRACT(message, '.*\\s$paramNameParam#([^\\s#][^#]*|)#.*', 1))))
                                AS $paramFieldNameParam;
};


---------------------------------------------------------------------------------------------
-- Extracts session id
-- @return {user : bytearray, ws: bytearray, id: bytearray, dt: datetime}
---------------------------------------------------------------------------------------------
DEFINE extractEventsWithSessionId(X, eventParam) RETURNS Y {
    x1 = filterByEvent($X, '$eventParam');
    x2 = extractParam(x1, 'SESSION-ID', id);
    $Y = FOREACH x2 GENERATE user, ws, id, dt, ide;
};

---------------------------------------------------------------------------------------------
-- The list of created temporary workspaces
-- @return {dt: datetime, user : bytearray, ws: bytearray, orgId : bytearray, affiliateId: bytearray, factory : bytearray, referrer: bytearray}
---------------------------------------------------------------------------------------------
DEFINE createdTemporaryWorkspaces(X) RETURNS Y {
    x1 = filterByEvent($X, 'factory-url-accepted');
    x2 = extractUrlParam(x1, 'REFERRER', 'referrer');
    x3 = extractUrlParam(x2, 'FACTORY-URL', 'factory');
    x4 = extractOrgAndAffiliateId(x3);
    x = FOREACH x4 GENERATE ws AS tmpWs, ExtractDomain(referrer) AS referrer, factory, orgId, affiliateId;

    -- created temporary workspaces
    w1 = filterByEvent($X, 'tenant-created,workspace-created');
    w = FOREACH w1 GENERATE dt, ws AS tmpWs, user, ide;

    y1 = JOIN w BY tmpWs, x BY tmpWs;
    $Y = FOREACH y1 GENERATE w::dt AS dt, w::tmpWs AS ws, w::user AS user, x::referrer AS referrer, x::factory AS factory,
                x::orgId AS orgId, x::affiliateId AS affiliateId, w::ide AS ide;
};

---------------------------------------------------------------------------------------------
-- The list of users created from factory
-- @return {dt: datetime, user : bytearray, ws: bytearray, orgId : bytearray, affiliateId: bytearray, factory : bytearray, referrer: bytearray}
---------------------------------------------------------------------------------------------
DEFINE usersCreatedFromFactory(X) RETURNS Y {
    u1 = filterByEvent($X, 'factory-url-accepted');
    u2 = extractUrlParam(u1, 'REFERRER', 'referrer');
    u3 = extractUrlParam(u2, 'FACTORY-URL', 'factory');
    u4 = extractOrgAndAffiliateId(u3);
    u = FOREACH u4 GENERATE ws AS tmpWs, ExtractDomain(referrer) AS referrer, factory, orgId, affiliateId;

    -- finds in which temporary workspaces anonymous users have worked
    x1 = filterByEvent($X, 'user-added-to-ws');
    x2 = FOREACH x1 GENERATE dt, ws AS tmpWs, user AS tmpUser;
    x = FILTER x2 BY INDEXOF(tmpUser, 'anonymoususer_', 0) == 0 AND INDEXOF(tmpWs, 'tmp-', 0) == 0;

    -- finds all anonymous users have become registered (created their accounts or just logged in)
    t1 = filterByEvent($X, 'user-changed-name');
    t2 = extractParam(t1, 'OLD-USER', 'old');
    t3 = extractParam(t2, 'NEW-USER', 'new');
    t4 = FILTER t3 BY INDEXOF(LOWER(old), 'anonymoususer_', 0) == 0 AND INDEXOF(LOWER(new), 'anonymoususer_', 0) < 0;
    t = FOREACH t4 GENERATE dt, LOWER(old) AS tmpUser, LOWER(new) AS user;

    -- finds created users
    k1 = filterByEvent($X, 'user-created');
    k2 = FILTER k1 BY INDEXOF(user, 'anonymoususer_', 0) < 0;
    k = FOREACH k2 GENERATE dt, user, ide;

    -- finds which created users worked as anonymous
    y1 = JOIN k BY user, t BY user;
    y = FOREACH y1 GENERATE k::dt AS dt, k::user AS user, t::tmpUser AS tmpUser, k::ide AS ide;

    -- finds in which temporary workspaces registered users have worked
    z1 = JOIN y BY tmpUser, x BY tmpUser;
    z2 = FILTER z1 BY MilliSecondsBetween(y::dt, x::dt) >= 0;
    z = FOREACH z2 GENERATE y::dt AS dt, y::user AS user, x::tmpWs AS tmpWs, y::tmpUser AS tmpUser, y::ide AS ide;

    r1 = JOIN z BY tmpWs, u BY tmpWs;
    $Y = FOREACH r1 GENERATE z::dt AS dt, z::user AS user, z::tmpWs AS ws, u::referrer AS referrer, u::factory AS factory,
        u::orgId AS orgId, u::affiliateId AS affiliateId, z::tmpUser AS tmpUser, z::ide AS ide;
};

---------------------------------------------------------------------------------------------
-- Combines small sessions into big one if time between them is less than $inactiveInterval
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE combineSmallSessions(X, startEvent, finishEvent) RETURNS Y {
    a1 = extractEventsWithSessionId($X, '$startEvent');

    -- avoids cases when there are several $finishEvent with same id, let's take the first one
    a2 = FOREACH a1 GENERATE ws, user, id, dt, ide;
    a3 = GROUP a2 BY id;
    a4 = FOREACH a3 GENERATE FLATTEN(group), MIN(a2.dt) AS minDt, FLATTEN(a2);
    a5 = FILTER a4 BY dt == minDt;
    a = FOREACH a5 GENERATE a2::ws AS ws, a2::user AS user, id AS id, a2::dt AS dt, a2::ide AS ide;

    b1 = extractEventsWithSessionId($X, '$finishEvent');

    -- avoids cases when there are several $finishEvent with same id, let's take the first one
    b2 = FOREACH b1 GENERATE ws, user, id, dt, ide;
    b3 = GROUP b2 BY id;
    b4 = FOREACH b3 GENERATE FLATTEN(group), MIN(b2.dt) AS minDt, FLATTEN(b2);
    b5 = FILTER b4 BY dt == minDt;
    b = FOREACH b5 GENERATE b2::ws AS ws, b2::user AS user, id AS id, b2::dt AS dt, b2::ide AS ide;

    -- joins $startEvent and $finishEvent by same id, removes events without corresponding pair
    c1 = JOIN a BY id LEFT, b BY id;
    c2 = FILTER c1 BY a::dt <= b::dt;
    c = removeEmptyField(c2, 'b::id');

    -- split them back
    d1 = FOREACH c GENERATE *, FLATTEN(TOKENIZE('$startEvent,$finishEvent', ',')) AS event;
    SPLIT d1 INTO d2 IF event == '$startEvent', d3 OTHERWISE;

    -- A: $startEvent
    A = FOREACH d2 GENERATE a::ws AS ws, a::user AS user, a::dt AS dt, a::id AS id, a::ide AS ide;

    -- B: $finishEvent
    B = FOREACH d3 GENERATE b::ws AS ws, b::user AS user, b::dt AS dt, b::id AS id, b::ide AS ide;

    -- joins $finishEvent and $startEvent, finds for every $finishEvent the closest
    -- $startEvent to decide whether the pause between them is less than $inactiveInterval
    e1 = JOIN B BY (ws, user) LEFT, A BY (ws, user);
    e2 = FILTER e1 BY A::ws IS NOT NULL;
    e3 = FOREACH e2 GENERATE B::id AS finishId, B::ide AS ide, A::id AS startId, MilliSecondsBetween(A::dt, B::dt) AS interval;
    e = FILTER e3 BY interval > 0 AND interval <= (long) 10 * 60 * 1000; -- $inactiveInterval = 10min

    -- removes $startEvents which are close to any $finishEvent
    d1 = JOIN A BY id LEFT, e BY startId;
    d2 = FILTER d1 BY e::startId IS NULL;
    S = FOREACH d2 GENERATE A::ws AS ws, A::user AS user, A::dt AS dt, '$startEvent' AS event, A::id AS id, A::ide AS ide;

    -- removes $finishEvent which are close to any $startEvent
    f1 = JOIN B BY id LEFT, e BY finishId;
    f2 = FILTER f1 BY e::finishId IS NULL;
    F = FOREACH f2 GENERATE B::ws AS ws, B::user AS user, B::dt AS dt, '$finishEvent' AS event, B::id AS id, B::ide AS ide;

    -- finally, combines closest events to get completed sessions
    u1 = UNION S, F;
    u2 = combineClosestEvents(u1, '$startEvent', '$finishEvent');

    -- considering sessions less than 1 min as 1 min columns
    $Y = FOREACH u2 GENERATE ws AS ws, user AS user, dt AS dt, (delta < 60 * 1000 ? 60 * 1000 : delta) AS delta, id AS id, ide AS ide;
};

---------------------------------------------------------------------------------------------
-- Calculates time between pairs of $startEvent and $finishEvent
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE combineClosestEvents(X, startEvent, finishEvent) RETURNS Y {
    x1 = removeEmptyField($X, 'ws');
    x = removeEmptyField(x1, 'user');

    a1 = filterByEvent(x, '$startEvent');
    a = FOREACH a1 GENERATE ws, user, event, dt, id, ide;
    
    b1 = filterByEvent(x, '$startEvent,$finishEvent');
    b = FOREACH b1 GENERATE ws, user, event, dt, id, ide;

    -- joins $startEvent with all other events to figure out which event is mostly close to '$startEvent'
    c1 = JOIN a BY (ws, user), b BY (ws, user);
    c2 = FOREACH c1 GENERATE a::ws AS ws, a::user AS user, a::event AS event, a::dt AS dt, b::event AS secondEvent, b::dt AS secondDt, a::id AS id, a::ide AS ide;

    -- @param delta: milliseconds between $startEvent and second event
    c3 = FOREACH c2 GENERATE *, MilliSecondsBetween(secondDt, dt) AS delta;

    -- removes cases when second event is preceded by $startEvent (before $startEvent in time line)
    c = FILTER c3 BY delta > 0;

    g1 = GROUP c BY (ws, user, event, dt, id, ide);
    g2 = FOREACH g1 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, group.id AS id, group.ide AS ide, FLATTEN(c), MIN(c.delta) AS minDelta;

    -- the desired closest event have to be $finishEvent anyway
    g = FILTER g2 BY delta == minDelta AND c::secondEvent == '$finishEvent';

    -- converts time into seconds
    $Y = FOREACH g GENERATE ws AS ws, user AS user, dt AS dt, delta AS delta, id AS id, ide AS ide;
};

---------------------------------------------------------------------------------------------
-- Calculates time between pairs of $startEvent and $finishEvent by ID
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE combineClosestEventsByID(X, startEvent, finishEvent) RETURNS Y {
    x1 = removeEmptyField($X, 'ws');
    x = removeEmptyField(x1, 'user');

    a1 = filterByEvent(x, '$startEvent');
    a2 = extractParam(a1, 'ID', event_id);
    a3 = removeEmptyField(a2, 'event_id');
    
    a = FOREACH a2 GENERATE ws, user, event, dt, id, event_id, ide;

    b1 = filterByEvent(x, '$finishEvent');
    b2 = extractParam(b1, 'ID', event_id);
    b3 = removeEmptyField(b2, 'event_id');
    b = FOREACH b3 GENERATE ws, user, event, dt, id, event_id, ide;
    
    -- joins $startEvent with all other events to figure out which event is mostly close to '$startEvent'
    c1 = JOIN a BY (event_id), b BY (event_id);
    c2 = FOREACH c1 GENERATE a::ws AS ws, a::user AS user, a::dt AS dt, b::dt AS secondDt, a::id AS id, a::ide AS ide;

    -- @param delta: milliseconds between $startEvent and second event
    c3 = FOREACH c2 GENERATE *, MilliSecondsBetween(secondDt, dt) AS delta;

    -- removes cases when second event is preceded by $startEvent (before $startEvent in time line)
    c4 = FILTER c3 BY delta > 0;
    $Y = FOREACH c4 GENERATE ws, user, dt, delta, id, ide;
};

---------------------------------------------------------------------------------------------
-- Calculates time between pairs of $startEvent and $finishEvent
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE calculateTime(X, startEvent, finishEvent) RETURNS Y {
    x1 = filterByEvent($X, '$startEvent,$finishEvent');
    x2 = extractParam(x1, 'ID', id_null_possible);
    x = FOREACH x2 GENERATE *, (id_null_possible IS NOT NULL ?  id_null_possible : '') AS id;

    a = combineClosestEventsByID(x, '$startEvent', '$finishEvent');

    b1 = removeNotEmptyField(x, 'id');
    b = combineClosestEvents(b1, '$startEvent', '$finishEvent');

    c = UNION a, b;

    $Y = FOREACH c GENERATE dt, ws, user, delta, ide;
};

---------------------------------------------------------------------------------------------
-- Adds field which is indicator if event has happened during session or hasn't
-- @return {*, $fieldParam: int}
---------------------------------------------------------------------------------------------
DEFINE addEventIndicator(W, X,  eventParam, fieldParam, inactiveIntervalParam) RETURNS Y {
  z1 = filterByEvent($X, '$eventParam');
  z = FOREACH z1 GENERATE ws, user, dt;

  -- finds out if event was inside session
  x1 = JOIN $W BY (ws, user) LEFT, z BY (ws, user);
  x2 = FOREACH x1 GENERATE *, (z::ws IS NULL ? 0
                                             : (MilliSecondsBetween(z::dt, $W::dt) > 0 AND MilliSecondsBetween(z::dt, $W::dt) <= $W::delta + (int) $inactiveIntervalParam*60*1000 ? 1 : 0 )) AS $fieldParam;
  -- if several events were occurred then keep only one
  x3 = GROUP x2 BY $W::dt;
  $Y = FOREACH x3 {
        t = LIMIT x2 1;
        GENERATE FLATTEN(t);
    }
};

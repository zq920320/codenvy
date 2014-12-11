/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
-- @return {dt : datetime,  event : bytearray, message : chararray, user : bytearray, ws : bytearray}
-- In details:
--   field 'date' contains date in format 'YYYYMMDD'
--   field 'time' contains seconds from midnight
---------------------------------------------------------------------------
DEFINE loadResources(resourceParam, from, to, userType, wsType) RETURNS Y {
  l1 = LOAD '$resourceParam' USING PigStorage() as (message : chararray);
  l2 = FILTER l1 BY INDEXOF(message, 'EVENT#', 0) > 0;

  l3 = extractUser(l2, '$userType');
  l4 = extractWs(l3, '$wsType');
  l5 = extractParam(l4, 'WS-ID', 'wsId');
  l6 = extractParam(l5, 'USER-ID', 'userId');
  l = FOREACH l6 GENERATE user,
                          userId,
                          ws,
                          wsId,
                          message,
                          REGEX_EXTRACT_ALL(message, '([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}) ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}).*\\s-(\\s.*)') AS pattern;

  k1 = FOREACH l GENERATE ReplaceUserWithId(user, userId) AS user,
                          ReplaceWsWithId(ws, wsId) AS ws,
                          ToDate(pattern.$1, 'yyyy-MM-dd HH:mm:ss,SSS') AS dt,
                          pattern.$2 AS message;

  k2 = filterByDate(k1, '$from', '$to');
  k3 = extractParam(k2, 'EVENT', 'event');
  k = removeEmptyField(k3, 'event');

  m1 = FILTER k BY '$wsType' == 'ANY' OR  ws == 'default' OR
                    ('$wsType' == 'TEMPORARY' AND IsTemporaryWorkspaceById(ws)) OR
                    ('$wsType' == 'PERSISTENT' AND NOT IsTemporaryWorkspaceById(ws));

  m2 = FILTER m1 BY '$userType' == 'ANY' OR user == 'default' OR
                      ('$userType' == 'ANONYMOUS' AND IsAnonymousUserById(user)) OR
                      ('$userType' == 'REGISTERED' AND NOT IsAnonymousUserById(user));

  m = DISTINCT m2;
  $Y = FOREACH m GENERATE dt,
                          user,
                          event,
                          message,
                          ws;
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
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\sWS#([^\\s#][^#]*|)#.*')) AS ws1;
  $Y = FOREACH x1 GENERATE *, (ws1 IS NOT NULL AND ws1 != '' ? LOWER(ws1) : 'default') AS ws;
};

---------------------------------------------------------------------------
-- Extract user name out of message and adds as field to tuple.
-- @return  {..., user : bytearray}
---------------------------------------------------------------------------
DEFINE extractUser(X, userType) RETURNS Y {
  x1 = FOREACH $X GENERATE *, FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\sUSER#([^\\s#][^#]*|)#.*')) AS user1;
  $Y = FOREACH x1 GENERATE *, (user1 IS NOT NULL AND user1 != '' ? user1 : 'default') AS user;
};

---------------------------------------------------------------------------
-- Extract parameter value out of message and adds as field to tuple.
-- @param paramNameParam - the parameter name
-- @param paramFieldNameParam - the name of filed in the tuple
-- @return  {..., $paramFieldNameParam : bytearray}
---------------------------------------------------------------------------
DEFINE extractParam(X, paramNameParam, paramFieldNameParam) RETURNS Y {
  $Y = FOREACH $X GENERATE *, FLATTEN(ExtractParam(message, '$paramNameParam')) AS $paramFieldNameParam;
};

---------------------------------------------------------------------------
-- @return last update
---------------------------------------------------------------------------
DEFINE lastUpdate(X, idField) RETURNS Y {
  y1 = GROUP $X BY $idField;
  y2 = FOREACH y1 GENERATE group AS $idField, MAX($X.dt) AS maxDt, FLATTEN($X);
  y3 = FILTER y2 BY dt == maxDt;
  $Y = FOREACH y3 GENERATE *;
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
    $Y = FOREACH x2 GENERATE user, ws, id, dt;
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
    x5 = extractFactoryId(x4);
    x = FOREACH x5 GENERATE ws AS tmpWs, ExtractDomain(referrer) AS referrer, factory, orgId, affiliateId, factoryId;

    -- created temporary workspaces
    w1 = filterByEvent($X, 'tenant-created,workspace-created');
    w = FOREACH w1 GENERATE dt, ws AS tmpWs, user;

    y1 = JOIN w BY tmpWs, x BY tmpWs;
    $Y = FOREACH y1 GENERATE w::dt AS dt, w::tmpWs AS ws, w::user AS user, x::referrer AS referrer, x::factory AS factory,
                x::orgId AS orgId, x::affiliateId AS affiliateId, x::factoryId AS factoryId;
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
    u5 = extractFactoryId(u4);
    u = FOREACH u5 GENERATE ws AS tmpWs, ExtractDomain(referrer) AS referrer, factory, orgId, affiliateId, factoryId;

    -- finds in which temporary workspaces anonymous users have worked
    x1 = filterByEvent($X, 'user-added-to-ws');
    x2 = FOREACH x1 GENERATE dt, ws AS tmpWs, user AS tmpUser;
    x = FILTER x2 BY IsAnonymousUserById(tmpUser) AND IsTemporaryWorkspaceById(tmpWs);

    -- finds all anonymous users have become registered (created their accounts or just logged in)
    t1 = filterByEvent($X, 'user-changed-name');
    t2 = extractParam(t1, 'OLD-USER', 'old');
    t3 = extractParam(t2, 'NEW-USER', 'new');
    t4 = FOREACH t3 GENERATE dt, ReplaceUserWithId(old, null) AS old, ReplaceUserWithId(new, null) AS new, old AS old1, new AS new1;
    t5 = FILTER t4 BY IsAnonymousUserById(old, old1, new1) AND NOT IsAnonymousUserById(new);
    t = FOREACH t5 GENERATE dt, old AS tmpUser, new AS user;

    -- finds created users
    k1 = filterByEvent($X, 'user-created');
    k2 = FILTER k1 BY NOT IsAnonymousUserById(user);
    k = FOREACH k2 GENERATE dt, user;

    -- finds which created users worked as anonymous
    y1 = JOIN k BY user, t BY user;
    y = FOREACH y1 GENERATE k::dt AS dt, k::user AS user, t::tmpUser AS tmpUser;

    -- finds in which temporary workspaces registered users have worked
    z1 = JOIN y BY tmpUser, x BY tmpUser;
    z2 = FILTER z1 BY MilliSecondsBetween(y::dt, x::dt) >= 0;
    z = FOREACH z2 GENERATE y::dt AS dt, y::user AS user, x::tmpWs AS tmpWs, y::tmpUser AS tmpUser;

    r1 = JOIN z BY tmpWs, u BY tmpWs;
    $Y = FOREACH r1 GENERATE z::dt AS dt, z::user AS user, z::tmpWs AS ws, u::referrer AS referrer, u::factory AS factory,
        u::orgId AS orgId, u::affiliateId AS affiliateId, u::factoryId AS factoryId, z::tmpUser AS tmpUser;
};

---------------------------------------------------------------------------------------------
-- Calculates time between pairs of $startEvent and $finishEvent
-- @return {user : bytearray, ws: bytearray, dt: datetime, delta: long}
---------------------------------------------------------------------------------------------
DEFINE combineClosestEvents(X, startEvent, finishEvent) RETURNS Y {
    x1 = removeEmptyField($X, 'ws');
    x = removeEmptyField(x1, 'user');

    a1 = filterByEvent(x, '$startEvent');
    a = FOREACH a1 GENERATE ws, user, event, dt, id;
    
    b1 = filterByEvent(x, '$startEvent,$finishEvent');
    b = FOREACH b1 GENERATE ws, user, event, dt, id;

    -- joins $startEvent with all other events to figure out which event is mostly close to '$startEvent'
    c1 = JOIN a BY (ws, user), b BY (ws, user);
    c2 = FOREACH c1 GENERATE a::ws AS ws, a::user AS user, a::event AS event, a::dt AS dt, b::event AS secondEvent, b::dt AS secondDt, a::id AS id;

    -- @param delta: milliseconds between $startEvent and second event
    c3 = FOREACH c2 GENERATE *, MilliSecondsBetween(secondDt, dt) AS delta;

    -- removes cases when second event is preceded by $startEvent (before $startEvent in time line)
    c = FILTER c3 BY delta > 0;

    g1 = GROUP c BY (ws, user, event, dt, id);
    g2 = FOREACH g1 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, group.id AS id, FLATTEN(c), MIN(c.delta) AS minDelta;

    -- the desired closest event have to be $finishEvent anyway
    g = FILTER g2 BY delta == minDelta AND c::secondEvent == '$finishEvent';

    -- converts time into seconds
    $Y = FOREACH g GENERATE ws AS ws, user AS user, dt AS dt, delta AS delta, id AS id;
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
    
    a = FOREACH a2 GENERATE ws, user, event, dt, id, event_id;

    b1 = filterByEvent(x, '$finishEvent');
    b2 = extractParam(b1, 'ID', event_id);
    b3 = removeEmptyField(b2, 'event_id');
    b = FOREACH b3 GENERATE ws, user, event, dt, id, event_id;
    
    -- joins $startEvent with all other events to figure out which event is mostly close to '$startEvent'
    c1 = JOIN a BY (event_id), b BY (event_id);
    c2 = FOREACH c1 GENERATE a::ws AS ws, a::user AS user, a::dt AS dt, b::dt AS secondDt, a::id AS id;

    -- @param delta: milliseconds between $startEvent and second event
    c3 = FOREACH c2 GENERATE *, MilliSecondsBetween(secondDt, dt) AS delta;

    -- removes cases when second event is preceded by $startEvent (before $startEvent in time line)
    c4 = FILTER c3 BY delta > 0;
    $Y = FOREACH c4 GENERATE ws, user, dt, delta, id;
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

    $Y = FOREACH c GENERATE dt, ws, user, delta;
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
  x3 = GROUP x2 BY ($W::dt, $W::id);
  $Y = FOREACH x3 {
        t = LIMIT x2 1;
        GENERATE FLATTEN(t);
    }
};

---------------------------------------------------------------------------
-- Extract factory id from factory url
-- @return  {..., factoryId : bytearray}
---------------------------------------------------------------------------
DEFINE extractFactoryId(X) RETURNS Y {
  e1 = extractUrlParam($X, 'FACTORY-URL', 'factoryUrl');
  $Y = FOREACH e1 GENERATE *, (GetQueryValue(factoryUrl, 'id') == '' ? NULL : GetQueryValue(factoryUrl, 'id')) AS factoryId;
};


---------------------------------------------------------------------------
-- Adds logout interval
---------------------------------------------------------------------------
DEFINE addLogoutInterval(X, L, idleIntervalParam) RETURNS Y {
  z1 = filterByEvent($L, 'user-sso-logged-out');
  z = FOREACH z1 GENERATE dt, user;

  -- checking if logout event occurred after session
  x1 = JOIN $X BY user LEFT, z BY user;
  x2 = FOREACH x1 GENERATE *, (z::user IS NULL ? 0 : ToMilliSeconds(z::dt) - ($X::startTime + $X::usageTime)) AS delta;
  x3 = FOREACH x2 GENERATE *, (0 < delta AND delta < (long) $idleIntervalParam ? delta : 0) AS logoutInterval;
  x4 = FOREACH x3 GENERATE $X::dt AS dt,
                           $X::ws AS ws,
                           $X::user AS user,
                           $X::startTime AS startTime,
                           $X::usageTime AS usageTime,
                           $X::sessionID AS sessionID,
                           logoutInterval AS logoutInterval;
  $Y = firstUpdate(x4, 'logoutInterval', 'sessionID');
};


---------------------------------------------------------------------------
-- @return first update
---------------------------------------------------------------------------
DEFINE firstUpdate(X, dtField, idField) RETURNS Y {
  y1 = GROUP $X BY $idField;
  y2 = FOREACH y1 GENERATE group AS $idField, MIN($X.$dtField) AS minDt, FLATTEN($X);
  y3 = FILTER y2 BY $dtField == minDt;
  $Y = FOREACH y3 GENERATE *;
};


---------------------------------------------------------------------------
-- @return list of sessions
---------------------------------------------------------------------------
DEFINE getSessions(X, eventParam) RETURNS Y {
    a1 = filterByEvent($X, '$eventParam');
    a2 = removeEmptyField(a1, 'user');
    a3 = extractParam(a2, 'SESSION-ID', sessionID);
    a = FOREACH a3 GENERATE dt, ws, user, sessionID;

    -- gets the very FIRST event to figure out the session start time
    s1 = firstUpdate(a, 'dt', 'sessionID');
    s2 = FOREACH s1 GENERATE a::dt AS dt,
                              a::ws AS ws,
                              a::user AS user,
                              a::sessionID AS sessionID,
                              GetSessionStartTime(a::sessionID) AS startTime;
    s = FOREACH s2 GENERATE ws,
                             user,
                             sessionID,
                             (startTime IS NULL ? ToMilliSeconds(dt) : startTime) AS startTime;

    -- gets the very LAST event to figure out the session start end time
    b1 = lastUpdate(a, 'sessionID');
    b2 = FOREACH b1 GENERATE a::dt AS dt,
                              ToMilliSeconds(a::dt) AS endTime,
                              a::ws AS ws,
                              a::user AS user,
                              a::sessionID AS sessionID;

    b3 = JOIN b2 BY sessionID, s BY sessionID;
    b = FOREACH b3 GENERATE b2::dt AS dt,
                             b2::ws AS ws,
                             b2::user AS user,
                             b2::sessionID AS sessionID,
                             s::startTime AS startTime,
                             (b2::endTime - s::startTime) AS usageTime;

    c = addLogoutInterval(b, $X, '600000');
    $Y = FOREACH c GENERATE ws,
                            user,
                            sessionID,
                            startTime,
                            (usageTime + logoutInterval) AS usageTime,
                            (startTime + usageTime + logoutInterval) AS endTime,
                            logoutInterval;

};

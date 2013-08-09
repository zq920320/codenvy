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

%DEFAULT inactiveInterval '10';  -- in minutes

DEFINE extractEventsWithSessionId(X, eventParam) RETURNS Y {
    x1 = filterByEvent($X, '$eventParam');
    x2 = extractParam(x1, 'SESSION-ID', sId);
    $Y = FOREACH x2 GENERATE user, ws, sId, dt;
}

----------------------------------------------
-- Usecase when there is no sesson-finished event.
-- Finds the most latest event for specific ws and user and treats it
-- as session-finished event. If such event was not find, then 10 mins is used
-- as session length
---------------------------------------------
DEFINE calculateForNoSF(X, Z, inactiveIntervalParam) RETURNS Y {
    x1 = JOIN $X BY (ws, user) LEFT, $Z BY (ws, user);
    x3 = FOREACH x1 GENERATE $X::ws AS ws, $X::user AS user, $X::dt AS dt, SecondsBetween($Z::dt, $X::dt) AS delta;
    x4 = GROUP x3 BY (ws, user, dt);
    x5 = FOREACH x4 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, MAX(x3.delta) AS delta;
    $Y = FOREACH x5 GENERATE ws, user, dt, (delta == 0 ? (int) $inactiveIntervalParam * 60 : delta) AS delta;
}

----------------------------------------------
-- Usecase when there is no sesson-started event.
-- Finds the most earlestt event for specific ws and user and treats it
-- as session-started event. If such event was not find, then 10 mins is used
-- as session length
---------------------------------------------
DEFINE calculateForNoSS(X, Z, inactiveIntervalParam) RETURNS Y {
    x1 = JOIN $X BY (ws, user) LEFT, $Z BY (ws, user);
    x3 = FOREACH x1 GENERATE $X::ws AS ws, $X::user AS user, $X::dt AS dt, SecondsBetween($X::dt, $Z::dt) AS delta;
    x4 = GROUP x3 BY (ws, user, dt);
    x5 = FOREACH x4 GENERATE group.ws AS ws, group.user AS user, group.dt AS dt, MAX(x3.delta) AS delta;
    $Y = FOREACH x5 GENERATE ws, user, dt, (delta == 0 ? (int) $inactiveIntervalParam * 60 : delta) AS delta;
}

t1 = loadResources('$log');
t2 = filterByDate(t1, '$FROM_DATE', '$TO_DATE');
t3 = extractUser(t2);
t = extractWs(t3);

SS1 = extractEventsWithSessionId(t, 'session-started');
SF1 = extractEventsWithSessionId(t, 'session-finished');
SS2 = extractEventsWithSessionId(t, 'session-factory-started');
SF2 = extractEventsWithSessionId(t, 'session-factory-finished');

j1 = JOIN SS1 BY sId FULL, SF1 BY sId;
j2 = JOIN SS2 BY sId FULL, SF2 BY sId;

SPLIT j1 INTO noSS1 IF SS1::sId IS NULL, noSF1 IF SF1::sId IS NULL, SSSF1 OTHERWISE;
SPLIT j2 INTO noSS2 IF SS2::sId IS NULL, noSF2 IF SF2::sId IS NULL, SSSF2 OTHERWISE;

A1 = FOREACH SSSF1 GENERATE SS1::ws AS ws, SS1::user AS user, SS1::dt AS dt, SecondsBetween(SF1::dt, SS1::dt) AS delta;
A2 = FOREACH SSSF2 GENERATE SS2::ws AS ws, SS2::user AS user, SS2::dt AS dt, SecondsBetween(SF2::dt, SS2::dt) AS delta;

w1 = FOREACH noSF1 GENERATE SS1::ws AS ws, SS1::user AS user, SS1::dt AS dt;
w2 = FOREACH noSF2 GENERATE SS2::ws AS ws, SS2::user AS user, SS2::dt AS dt;
w3 = FOREACH noSS1 GENERATE SF1::ws AS ws, SF1::user AS user, SF1::dt AS dt;
w4 = FOREACH noSS2 GENERATE SF2::ws AS ws, SF2::user AS user, SF2::dt AS dt;

B1 = calculateForNoSF(w1, t, '$inactiveInterval');
B2 = calculateForNoSF(w2, t, '$inactiveInterval');

C1 = calculateForNoSS(w3, t, '$inactiveInterval');
C2 = calculateForNoSS(w4, t, '$inactiveInterval');

--R = UNION A1, A2, B1, B2, C1, C2;
R = UNION A1, B1, C1;

-- calculation using old way
d1 = productUsageTimeList(t, '$inactiveInterval');

-- find user which were not taken in accout by previous calcuation
d2 = JOIN d1 BY (ws, user) LEFT, R BY (ws, user);
d3 = FILTER d2 BY R::ws IS NULL;
D = FOREACH d3 GENERATE d1::ws AS ws, d1::user AS user, d1::dt AS dt, d1::delta AS delta;

r1 = UNION R, D;
r4 = GROUP r1 BY user;
result = FOREACH r4 {
    r5 = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, r5;
}





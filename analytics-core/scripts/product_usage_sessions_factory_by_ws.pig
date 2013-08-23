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

t1 = loadResources('$LOG', '$USER', '$WS');
t = filterByDate(t1, '$FROM_DATE', '$TO_DATE');

SS = extractEventsWithSessionId(t, 'session-factory-started');
SF = extractEventsWithSessionId(t, 'session-factory-stopped');

j = JOIN SS BY sId FULL, SF BY sId;
SPLIT j INTO noSS IF SS::sId IS NULL, noSF IF SF::sId IS NULL, SSSF OTHERWISE;

A = FOREACH SSSF GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS dt, SecondsBetween(SF::dt, SS::dt) AS delta;
B = FOREACH noSS GENERATE SF::ws AS ws, SF::user AS user, SubtractDuration(SF::dt, 'PT10M') AS dt, 60 * (long) $inactiveInterval AS delta;
C = FOREACH noSF GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS dt, 60 * (long) $inactiveInterval AS delta;

R = UNION A, B, C;
r1 = GROUP R BY ws;
result = FOREACH r1 {
    r2 = FOREACH R GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, r2;
}



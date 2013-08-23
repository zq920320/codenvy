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

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

SS = extractEventsWithSessionId(t, 'session-factory-started');
SF = extractEventsWithSessionId(t, 'session-factory-stopped');

j = JOIN SS BY sId FULL, SF BY sId;
SPLIT j INTO noSS IF SS::sId IS NULL, noSF IF SF::sId IS NULL, SSSF OTHERWISE;

/*
 * List of all factory sessions
 */
A = FOREACH SSSF GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS ssDT, SF::dt AS sfDT;
B = FOREACH noSS GENERATE SF::ws AS ws, SF::user AS user, SubtractDuration(SF::dt, 'PT10M') AS ssDT, SF::dt AS sfDT;
C = FOREACH noSF GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS ssDT, AddDuration(SS::dt, 'PT10M') AS sfDT;
R = UNION A, B, C;

/*
 * List of interesting events.
 */
b1 = filterByEvent(t, '$EVENT');
b = FOREACH b1 GENERATE ws, user, dt;

c1 = JOIN b BY (ws, user) LEFT, R BY (ws, user);
c2 = FILTER c1 BY R::ws IS NOT NULL;
SPLIT c2 INTO d1 IF MilliSecondsBetween(R::sfDT, b::dt) > 0 AND MilliSecondsBetween(b::dt, R::ssDT) > 0, e1 OTHERWISE;

/*
 * Calculates session with events inside
 */
d2 = FOREACH d1 GENERATE  R::ws AS ws, R::user AS user, R::ssDT as dt;
d = DISTINCT d2;

/*
 * Calculates session with events outside
 */
e = FOREACH e1 GENERATE  R::ws AS ws, R::user AS user, R::ssDT as dt;

f = UNION d, e;
result = countAll(f);


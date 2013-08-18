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

t1 = loadResources('$LOG');
t2 = filterByDate(t1, '$FROM_DATE', '$TO_DATE');
t3 = extractUser(t2);
t = extractWs(t3);

SS = extractEventsWithSessionId(t, 'session-started');
SF = extractEventsWithSessionId(t, 'session-finished');

j1 = JOIN SS BY sId FULL, SF BY sId;
j2 = FILTER j1 BY SS::sId IS NOT NULL AND SF::sId IS NOT NULL;
j3 = FOREACH j2 GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS ssDt, SF::dt AS sfDt;
A = FOREACH j3 GENERATE ws, user, ssDt AS dt, SecondsBetween(sfDt, ssDt) AS delta;

--
-- The rest of the sessions
--
k1 = FOREACH t GENERATE ws, user, dt;
k2 = JOIN k1 BY (ws, user) LEFT, j3 BY (ws, user);
k3 = FILTER k2 BY (j3::ws IS NULL) OR MilliSecondsBetween(j3::ssDt, k1::dt) > 0 OR MilliSecondsBetween(j3::sfDt, k1::dt) < 0;
k4 = FOREACH k3 GENERATE k1::ws AS ws, k1::user AS user, k1::dt AS dt;
B = productUsageTimeList(k4, '$inactiveInterval');

d1 = UNION A, B;
d2 = FOREACH d1 GENERATE ws, REGEX_EXTRACT(user, '.*@(.*)', 1) AS domain, dt, delta;
R1 = FILTER d2 BY domain != '';

R2 = GROUP R1 BY domain;
result = FOREACH R2 {
    R3 = FOREACH R1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(domain), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, R3;
}

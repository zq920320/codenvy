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

t = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

--SS = extractEventsWithSessionId(t, 'session-started');
--SF = extractEventsWithSessionId(t, 'session-finished');
--
--j1 = JOIN SS BY id FULL, SF BY id;
--j2 = FILTER j1 BY SS::id IS NOT NULL AND SF::id IS NOT NULL;
--j3 = FOREACH j2 GENERATE SS::ws AS ws, SS::user AS user, SS::dt AS ssDt, SF::dt AS sfDt;
--A = FOREACH j3 GENERATE ws, user, ssDt AS dt, SecondsBetween(sfDt, ssDt) AS delta;
--
----
---- The rest of the sessions
----
--k1 = FOREACH t GENERATE ws, user, dt;
--k2 = JOIN k1 BY (ws, user) LEFT, j3 BY (ws, user);
--k3 = FILTER k2 BY (j3::ws IS NULL) OR MilliSecondsBetween(j3::ssDt, k1::dt) > 0 OR MilliSecondsBetween(j3::sfDt, k1::dt) < 0;
--k4 = FOREACH k3 GENERATE k1::ws AS ws, k1::user AS user, k1::dt AS dt;
R = productUsageTimeList(t, '10');

--R = UNION A, B;

result = FOREACH R GENERATE ToMilliSeconds(dt), TOTUPLE('user', user), TOTUPLE('value', delta);
STORE result INTO '$STORAGE_URL.$STORAGE_DST' USING MongoStorage();

r1 = FOREACH R GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, delta;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('value', delta);
STORE r INTO '$STORAGE_URL.$STORAGE_DST-raw' USING MongoStorage();
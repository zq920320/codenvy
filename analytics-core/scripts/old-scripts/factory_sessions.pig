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

%DEFAULT inactiveInterval '10';

r = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

u1 = LOAD '$LOAD_DIR' USING PigStorage() AS (tmpWs : chararray, referrer : chararray, factoryUrl : chararray);
u = FOREACH u1 GENERATE tmpWs, (referrer IS NULL ? '' : referrer) AS referrer, factoryUrl;

-- founds out all imported projects
i1 = filterByEvent(r, 'factory-project-imported');
i = FOREACH i1 GENERATE ws AS tmpWs, user, dt;

s1 = combineSmallSessions(r, 'session-factory-started', 'session-factory-stopped');
s2 = FOREACH s1 GENERATE ws AS tmpWs, user AS user, dt AS dt, delta AS delta, (INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) == 0 ? 'false' : 'true') AS auth;

-- founds out the corresponding referrer and factoryUrl
s3 = JOIN s2 BY tmpWs, u BY tmpWs;
s4 = FOREACH s3 GENERATE s2::dt AS dt, s2::tmpWs AS tmpWs, s2::user AS user, s2::delta AS delta, u::factoryUrl AS factoryUrl, u::referrer AS referrer, s2::auth AS auth;

-- founds out if factory session was converted or not
-- (if importing operation was inside a session)
s5 = JOIN s4 BY (tmpWs, user) LEFT, i BY (tmpWs, user);

s = FOREACH s5 GENERATE s4::delta AS delta, s4::factoryUrl AS factoryUrl, s4::referrer AS referrer, s4::auth AS auth,
			(i::tmpWs IS NULL ? 'false' : (SecondsBetween(s4::dt, i::dt) < 0 AND SecondsBetween(s4::dt, i::dt) + s4::delta + (long) $inactiveInterval * 60  > 0 ? 'true' : 'false' )) AS conv;

result = FOREACH s GENERATE TOTUPLE(TOTUPLE(delta / 60), TOTUPLE(factoryUrl), TOTUPLE(referrer), TOTUPLE(auth), TOTUPLE(conv));


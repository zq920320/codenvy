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
j = combineSmallSessions(t, 'session-factory-started', 'session-factory-stopped');


u1 = LOAD '$LOAD_DIR' USING PigStorage() AS (tmpWs : chararray, referrer : chararray, factoryUrl : chararray);
u = removeEmptyField(u1, 'referrer');

-- founds out the corresponding referrer and factoryUrl
s1 = JOIN j BY ws LEFT, u BY tmpWs;

s1 = FOREACH s1 GENERATE u::referrer AS referrer, j::ws AS ws, j::user AS user, j::dt AS dt, j::delta AS delta;
s = removeEmptyField(s1, 'referrer');

r1 = GROUP s BY referrer;
result = FOREACH r1 {
    r2 = FOREACH s GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt), TOTUPLE(delta));
    GENERATE group, r2;
}


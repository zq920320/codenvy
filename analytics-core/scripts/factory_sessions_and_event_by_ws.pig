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

/*
 * List of interesting events.
 */
b1 = filterByEvent(t, '$EVENT');
b = FOREACH b1 GENERATE ws, user, dt;

c1 = JOIN j BY (ws, user) LEFT, b BY (ws, user);
c2 = removeEmptyField(c1, 'b::ws');
c3 = FILTER c2 BY MilliSecondsBetween(b::dt, j::dt) > 0 AND SecondsBetween(b::dt, j::dt) <= delta;
c4 = FOREACH c3 GENERATE b::ws AS ws, b::user AS user, j::dt AS dt;
c = DISTINCT c4;


result = countByField(c, 'ws');


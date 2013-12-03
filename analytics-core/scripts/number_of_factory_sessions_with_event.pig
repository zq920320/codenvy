/*
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

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

-- List of interesting events.
a1 = filterByEvent(l, '$EVENT');
a = FOREACH a1 GENERATE ws, user, dt;

b = combineSmallSessions(l, 'session-factory-started', 'session-factory-stopped');

c1 = JOIN b BY (ws, user), a BY (ws, user);
c2 = FILTER c1 BY MilliSecondsBetween(a::dt, b::dt) > 0 AND SecondsBetween(a::dt, b::dt) <= delta;
c3 = FOREACH c2 GENERATE b::dt AS dt, a::ws AS ws, a::user AS user;
c = DISTINCT c3;

d = countAll(c);

result = FOREACH d GENERATE ToMilliSeconds(ToDate('$TO_DATE', 'yyyyMMdd')), TOTUPLE('value', countAll);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage();

r1 = FOREACH c GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('value', 1);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE-raw' USING MongoStorage();


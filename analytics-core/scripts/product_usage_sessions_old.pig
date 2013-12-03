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
f = productUsageTimeList(t, '10');

result = FOREACH f GENERATE ToMilliSeconds(dt), TOTUPLE('user', user), TOTUPLE('value', delta);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage();

r1 = FOREACH f GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, delta;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('value', delta);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE-raw' USING MongoStorage();

-- loads existed statistics
s1 = LOAD '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoLoader('id: chararray, time: Long');
s = FOREACH s1 GENERATE id, (time IS NULL ? 0 : time) AS time;

-- calculate total user's time being in product
t1 = GROUP f BY user;
t2 = FOREACH t1 GENERATE group AS id, SUM(f.delta) AS time;
t = FILTER t2 BY INDEXOF(UPPER(id), 'ANONYMOUSUSER_', 0) != 0 AND id != 'default';

--combine and store result
x1 = JOIN t BY id LEFT, s BY id;
x2 = FOREACH x1 GENERATE t::id AS id, (t::time + (s::time IS NULL ? 0 : s::time)) AS time;
x = FOREACH x2 GENERATE id, TOTUPLE('user_email', id), TOTUPLE('time', time);
STORE x INTO '$STORAGE_URL.$STORAGE_TABLE_USERS_STATISTICS' USING MongoStorage();

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

l1 = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
l = FOREACH l1 GENERATE *, '' AS id; -- it requires 'id' field to be in scheme

f = combineClosestEvents(l, '$EVENT-started', '$EVENT-finished');

a = GROUP f ALL;

result = FOREACH a GENERATE ToMilliSeconds(ToDate('$TO_DATE', 'yyyyMMdd')), TOTUPLE('value', SUM(f.delta));
STORE result INTO '$STORAGE_URL.$STORAGE_DST' USING MongoStorage();

r1 = FOREACH f GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, delta;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('value', delta);
STORE r INTO '$STORAGE_URL.$STORAGE_DST-raw' USING MongoStorage();
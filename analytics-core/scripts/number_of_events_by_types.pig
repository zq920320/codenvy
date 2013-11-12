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

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
f = filterByEvent(l, '$EVENT');

a1 = extractParam(f, '$PARAM', param);
a2 = FOREACH a1 GENERATE LOWER(param), event;
a3 = GROUP a2 BY param;
a = FOREACH a3 GENERATE group, COUNT(a2) AS countAll;

result = FOREACH a GENERATE ToMilliSeconds(ToDate('$TO_DATE', 'yyyyMMdd')), TOTUPLE('type', group), TOTUPLE('value', countAll);
STORE result INTO '$STORAGE_URL.$METRIC' USING MongoStorage();

r1 = FOREACH f GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, LOWER(param);
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('type', param), TOTUPLE('value', 1L);
STORE r INTO '$STORAGE_URL.$METRIC-raw' USING MongoStorage();

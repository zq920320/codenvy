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
f = usersCreatedFromFactory(l);

a1 = FOREACH f GENERATE user;
a2 = DISTINCT a1;
a = countAll(a2);

result = FOREACH a GENERATE ToMilliSeconds(ToDate('$TO_DATE', 'yyyyMMdd')), TOTUPLE('value', countAll);
STORE result INTO '$STORAGE_URL.$STORAGE_DST' USING MongoStorage();

r1 = FOREACH f GENERATE dt, tmpWs AS ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain), TOTUPLE('value', 1L);
STORE r INTO '$STORAGE_URL.$STORAGE_DST-raw' USING MongoStorage();
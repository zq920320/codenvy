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

u1 = filterByEvent(l, 'factory-url-accepted');
u2 = extractUrlParam(u1, 'REFERRER', 'referrer');
u3 = extractUrlParam(u2, 'FACTORY-URL', 'factory');
u4 = extractUrlParam(u3, 'ORG-ID', 'orgId');
u5 = extractUrlParam(u4, 'AFFILIATE-ID', 'affiliateId');
u = FOREACH u5 GENERATE ws AS tmpWs, referrer, factory, orgId, affiliateId;

 -- finds in which temporary workspaces anonymous users have worked
x1 = filterByEvent(l, 'user-added-to-ws');
x2 = FOREACH x1 GENERATE dt, ws AS tmpWs, UPPER(user) AS tmpUser;
x = FILTER x2 BY INDEXOF(tmpUser, 'ANONYMOUSUSER_', 0) == 0 AND INDEXOF(UPPER(tmpWs), 'TMP-', 0) == 0;

-- finds all anonymous users have become registered (created their accounts or just logged in)
t1 = filterByEvent(l, 'user-changed-name');
t2 = extractParam(t1, 'OLD-USER', 'old');
t3 = extractParam(t2, 'NEW-USER', 'new');
t4 = FILTER t3 BY INDEXOF(UPPER(old), 'ANONYMOUSUSER_', 0) == 0 AND INDEXOF(UPPER(new), 'ANONYMOUSUSER_', 0) < 0;
t = FOREACH t4 GENERATE dt, UPPER(old) AS tmpUser, new AS user;

-- finds created users
k1 = filterByEvent(l, 'user-created');
k2 = FILTER k1 BY INDEXOF(UPPER(user), 'ANONYMOUSUSER_', 0) < 0;
k = FOREACH k2 GENERATE dt, user;

-- finds which created users worked as anonymous
y1 = JOIN k BY user, t BY user;
y = FOREACH y1 GENERATE k::dt AS dt, k::user AS user, t::tmpUser AS tmpUser;

-- finds in which temporary workspaces registered users have worked
z1 = JOIN y BY tmpUser, x BY tmpUser;
z2 = FILTER z1 BY MilliSecondsBetween(y::dt, x::dt) >= 0;
z = FOREACH z2 GENERATE y::dt AS dt, y::user AS user, x::tmpWs AS tmpWs;


r1 = JOIN z BY tmpWs, u BY tmpWs;
r2 = FOREACH r1 GENERATE z::dt AS dt, z::user AS user,
        z::tmpWs AS ws, u::referrer AS referrer, u::factory AS factory, u::orgId AS orgId, u::affiliateId AS affiliateId;
result = FOREACH r2 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user),
            TOTUPLE('referrer', referrer), TOTUPLE('factory', factory), TOTUPLE('org_id', orgId),
            TOTUPLE('affiliate_id', affiliateId), TOTUPLE('value', 1L);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');
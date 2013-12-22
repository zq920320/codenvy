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

-- created temporary workspaces
w1 = filterByEvent(l, 'tenant-created');
w2 = FOREACH w1 GENERATE dt, ws AS tmpWs, user;
w3 = JOIN w2 BY tmpWs, u BY tmpWs;
w = FOREACH w3 GENERATE w2::dt AS dt, w2::tmpWs AS ws, w2::user AS user, u::referrer AS referrer, u::factory AS factory,
                u::orgId AS orgId, u::affiliateId AS affiliateId;

r1 = FOREACH w GENERATE dt, ws, user, orgId, affiliateId, factory, referrer;
result = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user),
                    TOTUPLE('org_id', orgId), TOTUPLE('affiliate_id', affiliateId),
                    TOTUPLE('referrer', referrer), TOTUPLE('factory', factory), TOTUPLE('value', 1);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');


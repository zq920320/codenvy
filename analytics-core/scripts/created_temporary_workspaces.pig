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

DEFINE MongoStorage com.codenvy.analytics.pig.udf.MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');
DEFINE UUID com.codenvy.analytics.pig.udf.UUID;

IMPORT 'macros.pig';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
r = createdTemporaryWorkspaces(l);

result = FOREACH r GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('ws', ws), TOTUPLE('user', user),
                    TOTUPLE('org_id', orgId), TOTUPLE('affiliate_id', affiliateId),
                    TOTUPLE('referrer', referrer), TOTUPLE('factory', factory), TOTUPLE('value', 1);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;


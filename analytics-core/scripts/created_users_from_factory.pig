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

r1 = usersCreatedFromFactory(l);
r = FOREACH r1 GENERATE *, (INDEXOF(factory, 'factory?id=', 0) > 0 ? 1 : 0) AS encodedFactory;

result = FOREACH r GENERATE UUID(), TOTUPLE('date', ToMilliSeconds(dt)), TOTUPLE('ws', ws), TOTUPLE('user', user),
            TOTUPLE('referrer', referrer), TOTUPLE('factory', factory), TOTUPLE('org_id', orgId),
            TOTUPLE('affiliate_id', affiliateId), TOTUPLE('value', 1L), TOTUPLE('encoded_factory', encodedFactory);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

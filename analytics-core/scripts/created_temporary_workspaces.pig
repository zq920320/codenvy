/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

r = createdTemporaryWorkspaces(l);
r = FOREACH r GENERATE *, (factoryId IS NULL ? 0 : 1) AS encodedFactory;

r = FOREACH r GENERATE UUID(),
                        TOTUPLE('date', ToMilliSeconds(dt)),
                        TOTUPLE('ws', ws),
                        TOTUPLE('user', user),
                        TOTUPLE('org_id', orgId),
                        TOTUPLE('affiliate_id', affiliateId),
                        TOTUPLE('referrer', referrer),
                        TOTUPLE('factory', factory),
                        TOTUPLE('factory_id', factoryId),
                        TOTUPLE('value', 1),
                        TOTUPLE('encoded_factory', encodedFactory);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;


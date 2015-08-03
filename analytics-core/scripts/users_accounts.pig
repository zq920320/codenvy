/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2015] Codenvy, S.A.
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

-----------------------------------------------------------------------------------
------------------------------ add user into account ------------------------------
-----------------------------------------------------------------------------------
a1 = filterByEvent(l, 'account-add-member');
a2 = extractParam(a1, 'ACCOUNT-ID', 'account');
a = extractParam(a2, 'ROLES', 'roles');

resultA = FOREACH a GENERATE UUIDFrom(CONCAT(account, user)),
                             TOTUPLE('date', ToMilliSeconds(dt)),
                             TOTUPLE('user', user),
                             TOTUPLE('account', account),
                             TOTUPLE('roles', UnionAccountRoles(account, user, roles)),
                             TOTUPLE('removed', 0);
STORE resultA INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

-----------------------------------------------------------------------------------
------------------------------ remove user from accont ----------------------------
-----------------------------------------------------------------------------------
b1 = filterByEvent(l, 'account-remove-member');
b = extractParam(b1, 'ACCOUNT-ID', 'account');

resultB = FOREACH b GENERATE UUIDFrom(CONCAT(account, user)),
                             TOTUPLE('user', user),
                             TOTUPLE('account', account),
                             TOTUPLE('removed_date', ToMilliSeconds(dt)),
                             TOTUPLE('removed', 1);
STORE resultB INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;

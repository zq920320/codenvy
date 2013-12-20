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

a1 = filterByEvent(l, 'factory-created');
a2 = extractUrlParam(a1, 'FACTORY-URL', 'factory');
a3 = extractParam(a2, 'TYPE', 'projectType');
a4 = extractUrlParam(a3, 'REPO-URL', 'repository');
a5 = extractUrlParam(a4, 'ORG-ID', 'orgId');
a6 = extractUrlParam(a5, 'AFFILIATE-ID', 'affiliateId');

a = FOREACH a6 GENERATE dt, ws, user, factory, repository, (orgId == '}' ? '' : orgId) AS orgId,
                (affiliateId == '}' ? '' : affiliateId) AS affiliateId, projectType;

result = FOREACH a GENERATE ToMilliSeconds(dt), TOTUPLE('factory', factory);
STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');

r1 = FOREACH a GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, factory, repository,
                orgId, affiliateId, projectType;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain),
                    TOTUPLE('orgId', orgId), TOTUPLE('affiliateId', affiliateId),
                    TOTUPLE('repository', repository), TOTUPLE('project_type', projectType), TOTUPLE('factory', factory);
STORE r INTO '$STORAGE_URL.$STORAGE_TABLE-raw' USING MongoStorage('$STORAGE_USER', '$STORAGE_PASSWORD');


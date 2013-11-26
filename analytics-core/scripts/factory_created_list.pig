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
a2 = extractUrlParam(a1, 'FACTORY-URL', 'factoryUrl');
a3 = extractParam(a2, 'TYPE', 'projectType');
a4 = extractUrlParam(a3, 'REPO-URL', 'repoUrl');
a = FOREACH a4 GENERATE dt, ws, user, factoryUrl, repoUrl, projectType;

result = FOREACH a GENERATE ToMilliSeconds(dt), TOTUPLE('value', factoryUrl);
STORE result INTO '$STORAGE_URL.$STORAGE_DST' USING MongoStorage();

r1 = FOREACH a GENERATE dt, ws, user, LOWER(REGEX_EXTRACT(user, '.*@(.*)', 1)) AS domain, factoryUrl, repoUrl, projectType;
r = FOREACH r1 GENERATE ToMilliSeconds(dt), TOTUPLE('ws', ws), TOTUPLE('user', user), TOTUPLE('domain', domain),
                    TOTUPLE('repo_url', repoUrl), TOTUPLE('project_type', projectType), TOTUPLE('value', factoryUrl);
STORE r INTO '$STORAGE_URL.$STORAGE_DST-raw' USING MongoStorage();


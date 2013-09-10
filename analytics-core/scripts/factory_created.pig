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

r = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

-- list of factories
a1 = filterByEvent(r, 'factory-created');
a2 = extractParam(a1, 'PROJECT', 'project');
a3 = extractParam(a2, 'TYPE', 'type');
a4 = extractParam(a3, 'REPO-URL', 'repoUrl');
a5 = extractParam(a4, 'FACTORY-URL', 'factoryUrl');
a = FOREACH a5 GENERATE ws, user, project, type, repoUrl, factoryUrl;

l = LOAD '$LOAD_DIR' USING PigStorage() AS (ws : chararray, user : chararray, project : chararray, type : chararray, repoUrl : chararray, factoryUrl : chararray);

-- store whole data
c1 = UNION a, l;
c = DISTINCT c1;

STORE c INTO '$STORE_DIR' USING PigStorage();

result = countAll(a);

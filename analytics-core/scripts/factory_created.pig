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

a1 = loadResources('$log');
a2 = filterByDate(a1, '$FROM_DATE', '$TO_DATE');
a3 = filterByEvent(a2, 'factory-created');
a4 = extractWs(a3);
a6 = extractUser(a4);
a7 = extractParam(a6, 'PROJECT', 'project');
a8 = extractParam(a7, 'TYPE', 'type');
a9 = extractParam(a8, 'REPO-URL', 'repoUrl');
a10 = extractParam(a9, 'FACTORY-URL', 'factoryUrl');

a = FOREACH a10 GENERATE ws, user, project, type, repoUrl, factoryUrl;
b = LOAD '$LOAD_DIR' USING PigStorage() AS (ws : chararray, user : chararray, project : chararray, type : chararray, repoUrl : chararray, factoryUrl : chararray);

c1 = UNION a, b;
c = DISTINCT c1;

STORE c INTO '$STORE_DIR' USING PigStorage();


result = countAll(a);

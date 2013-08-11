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

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
f = filterByEvent(f2, 'project-deployed,application-created');

t1 = extractWs(f);
t2 = extractUser(t1);
t3 = extractParam(t2, 'TYPE', 'type');
t4 = extractParam(t3, 'PROJECT', 'project');
t = extractParam(t4, 'PAAS', 'paas');

r1 = FOREACH t GENERATE ws, user, project, type, paas;
r2 = DISTINCT r1;
r3 = GROUP r2 BY paas;
result = FOREACH r3 GENERATE group, COUNT(r2);


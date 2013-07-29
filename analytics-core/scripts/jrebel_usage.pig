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

 ---------------------------------------------------------------------------
-- Finds total number of 'jrebel-usage' events.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
f2 = filterByDate(f1, '$FROM_DATE', '$TO_DATE');
fR = filterByEvent(f2, 'jrebel-usage');

t1 = extractWs(fR);
t2 = extractUser(t1);
t3 = extractParam(t2, 'TYPE', 'type');
t4 = extractParam(t3, 'PROJECT', 'project');
t5 = extractParam(t4, 'JREBEL', 'jrebel');
tR = DISTINCT t5;

r1 = FOREACH tR GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(project), TOTUPLE(type), TOTUPLE(jrebel));
result = DISTINCT r1;



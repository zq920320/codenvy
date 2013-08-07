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

%DEFAULT inactiveInterval '10';  -- in minutes

f1 = loadResources('$log');
fR = filterByDate(f1, '$FROM_DATE', '$TO_DATE');

t1 = extractUser(fR);
tR = extractWs(t1);

r1 = productUsageTimeList(tR, '$inactiveInterval');
dump r1;
--result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(ws), TOTUPLE(user), TOTUPLE(dt), TOTUPLE(delta));

